package mon.lattice.control.deployment.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import mon.lattice.control.deployment.DeploymentException;
import mon.lattice.control.deployment.EntityDeploymentDelegate;
import mon.lattice.control.deployment.DataConsumerInfo;
import mon.lattice.control.deployment.DataSourceInfo;
import mon.lattice.control.deployment.ResourceEntityInfo;
import mon.lattice.im.delegate.DCNotFoundException;
import mon.lattice.im.delegate.DSNotFoundException;
import mon.lattice.im.delegate.InfoPlaneDelegate;
import mon.lattice.core.ID;
import mon.lattice.core.EntityType;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.control.deployment.ControllerAgentInfo;
import mon.lattice.im.delegate.ControllerAgentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class SSHDeploymentManager implements EntityDeploymentDelegate {
    final String localJarFilePath;
    final String remoteJarFilePath;
    final String jarFileName;
    final JSch jsch;
    
    final Map<InetSocketAddress, ResourceEntityInfo> resources;
    final Map<InetSocketAddress, Session> sessions;
    
    final Map<ID, InetSocketAddress> dataSourcesResources;
    final Map<ID, InetSocketAddress> dataConsumersResources;
    final Map<ID, InetSocketAddress> controllerAgentsResources;
    
    final Map<ID, DataSourceInfo> dataSources;
    final Map<ID, DataConsumerInfo> dataConsumers;
    final Map<ID, ControllerAgentInfo> controllerAgents;
    
    final InfoPlaneDelegate infoPlaneDelegate;
    
    String identityFile = System.getProperty("user.home") + "/.ssh/id_rsa";
    
    Logger LOGGER = LoggerFactory.getLogger(SSHDeploymentManager.class);
    
    
    public SSHDeploymentManager(String localJarFilePath, String jarFileName, String remoteJarFilePath, InfoPlaneDelegate info) {
        this.localJarFilePath = localJarFilePath;
        this.remoteJarFilePath = remoteJarFilePath;
        this.jarFileName = jarFileName;
        this.jsch = new JSch();
        
        this.resources = new ConcurrentHashMap();
        this.sessions = new ConcurrentHashMap();
        
        this.dataSourcesResources = new ConcurrentHashMap<>(); 
        this.dataConsumersResources = new ConcurrentHashMap<>();
        this.controllerAgentsResources = new ConcurrentHashMap();
        
        this.dataSources = new ConcurrentHashMap<>();
        this.dataConsumers = new ConcurrentHashMap<>();
        this.controllerAgents = new ConcurrentHashMap<>();
        
        this.infoPlaneDelegate = info;
    }
    
    
    public SSHDeploymentManager(String identityFile, String localJarFilePath, String jarFileName, String remoteJarFilePath, String entityFileName, EntityType entityType, InfoPlaneDelegate info) {
        this(localJarFilePath, jarFileName, remoteJarFilePath, info);
        this.identityFile = identityFile;
    }
    
   
    
    private String parseDeps(List<String> depJars) {
        StringBuilder s = new StringBuilder(); 
        for (String path : depJars) {
            s.append(':');
            s.append(path);
        }
        
        return s.toString();
    }

    
    
    @Override
    public ID startDataSource(ResourceEntityInfo requestedResource, DataSourceInfo dataSource) throws DeploymentException {
        Session session = null;
        Channel channel = null;
        
        if (!resources.containsKey(requestedResource.getAddress()))
            resources.put(requestedResource.getAddress(), requestedResource);
        
        ResourceEntityInfo resource = resources.get(requestedResource.getAddress());
        
        dataSources.putIfAbsent(dataSource.getId(), dataSource);
        
        synchronized(dataSource)
            {
             try {
                if (dataSource.isRunning())
                    return dataSource.getId(); // a DS is already up and running - exit immediately

                if (sessions.get(resource.getAddress()) == null) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                else
                    session = sessions.get(resource.getAddress());
                
                if (!session.isConnected()) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                    
                this.deployJarOnResource(resource, session);

                LOGGER.debug("Future " + dataSource.getEntityType() + " ID: " + dataSource.getId());

                String jvm = "java"; //we assume the executable is in the PATH
                String command = jvm + 
                                 " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + 
                                 dataSource.getEntityClassName() + " " +   
                                 dataSource.getId() + " " +
                                 dataSource.getArguments();

                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);

                // we are supposed to wait here until either the announce message sent by the DS 
                // is received from the Announcelistener thread or the timeout is reached (5 secs)
                infoPlaneDelegate.addDataSource(dataSource, resource, 20000);

                // if there is no Exception before we can now try to get the Data Source PID
                dataSource.setpID(infoPlaneDelegate.getDSPIDFromID(dataSource.getId()));
                dataSource.setRunning();
                dataSource.setStartedTime();
                
                dataSourcesResources.put(dataSource.getId(), resource.getAddress());

                // has to catch DeploymentException    
                } catch (JSchException | DSNotFoundException e) {
                    // we are here if there was an error while starting the remote Data Source
                    String errorMessage = "Error while starting " + dataSource.getEntityType() + " on " + resource.getAddress() + " " + e.getMessage();
                    if (channel != null) {
                        if (!channel.isClosed())
                            errorMessage += ". The SSH remote channel is still open - the DS may be up and running. ";
                        else
                            errorMessage += "Remote process exit-status " + channel.getExitStatus();
                    }

                    // TODO we may now collect the error log file to report back the issue 
                    throw new DeploymentException(errorMessage);

                } catch (InterruptedException ie) {
                    LOGGER.info("Interrupted " + ie.getMessage());
                }
                  catch (DeploymentException de) {
                    throw de;
                  }
             
                  finally {
                    // as the command was started without a pty when we close the channel 
                    // and session the remote command will continue to run
                    if (channel != null && session != null) {
                        channel.disconnect();
                        // we keep the session open as we reuse it
                    }
                  }
            return dataSource.getId();
            }
    }
    

    @Override
    public boolean stopDataSource(ID dataSourceID) throws DeploymentException {
        Session session = null;
        Channel channel = null;
       
        InetSocketAddress resourceAddressFromDSID;
        DataSourceInfo dataSource = null;
        
        resourceAddressFromDSID = dataSourcesResources.get(dataSourceID);
        
        try {
            if (resourceAddressFromDSID == null)
                if (infoPlaneDelegate.containsDataSource(dataSourceID))
                    throw new DSNotFoundException("Data Source with ID " + dataSourceID + " cannot be undeployed as it was not started via the API");
                else
                    throw new DSNotFoundException("Data Source with ID " + dataSourceID + " was not found");
        
            dataSource = dataSources.get(dataSourceID);
            
            synchronized (dataSource) {
                if (dataSource == null) {
                    return false;
                }
                
                session = sessions.get(resourceAddressFromDSID);
                
                if (!session.isConnected()) {
                    ResourceEntityInfo resource = resources.get(resourceAddressFromDSID);
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                
                LOGGER.debug("Stopping " + dataSource.getEntityType());
                String command = "kill " + dataSource.getpID();
                LOGGER.debug(command);
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);
                while (true) {
                    if (channel.isClosed()) {
                        if (channel.getExitStatus() == 0) {
                            this.dataSourcesResources.remove(dataSourceID);
                            this.dataSources.remove(dataSourceID);
                            //this.dsIDsAddresses.remove(dataSourceID);
                            break;
                        } else {
                            // the process is likely to be already stopped: removing from the map
                            this.dataSourcesResources.remove(dataSourceID);
                            this.dataSources.remove(dataSourceID);
                            //this.dsIDsAddresses.remove(dataSourceID);
                            throw new DeploymentException("exit-status: " + channel.getExitStatus());
                        }
                    }
                    Thread.sleep(500);
                }
            }
        } catch (JSchException | DSNotFoundException e) {
                throw new DeploymentException("Error while stopping DataSource, " + e.getMessage());
        } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
        } finally {
                if (session != null && channel != null) {
                    channel.disconnect();
                    //session.disconnect();
                }
            }
        return true;
    }
    
    
    @Override
    public ID startDataConsumer(ResourceEntityInfo requestedResource, DataConsumerInfo dataConsumer) throws DeploymentException {
        Session session = null;
        Channel channel = null;
         
        if (!resources.containsKey(requestedResource.getAddress()))
            resources.put(requestedResource.getAddress(), requestedResource);
        
        ResourceEntityInfo resource = resources.get(requestedResource.getAddress());
        
        dataConsumers.putIfAbsent(dataConsumer.getId(), dataConsumer);
        
        synchronized(dataConsumer)
            {
             try {
                if (dataConsumer.isRunning())
                    return dataConsumer.getId(); // a DS is already up and running - exit immediately

                if (sessions.get(resource.getAddress()) == null) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                else
                    session = sessions.get(resource.getAddress());
                
                if (!session.isConnected()) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                
                this.deployJarOnResource(resource, session);

                LOGGER.debug("Future " + dataConsumer.getEntityType() + " ID: " + dataConsumer.getId());

                String jvm = "java"; //we assume the executable is in the PATH
                String command = jvm + 
                                 " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + 
                                 dataConsumer.getEntityClassName() + " " +   
                                 dataConsumer.getId() + " " +
                                 dataConsumer.getArguments();

                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);

                // we are supposed to wait here until either the announce message sent by the DS 
                // is received from the Announcelistener thread or the timeout is reached (5 secs)
                infoPlaneDelegate.addDataConsumer(dataConsumer, resource, 20000);

                // if there is no Exception before we can now try to get the Data Source PID
                dataConsumer.setpID(infoPlaneDelegate.getDCPIDFromID(dataConsumer.getId()));
                dataConsumer.setRunning();
                dataConsumer.setStartedTime();
                
                dataConsumersResources.put(dataConsumer.getId(), resource.getAddress());

                // has to catch DeploymentException    
                } catch (JSchException | DCNotFoundException e) {
                    // we are here if there was an error while starting the remote Data Source
                    String errorMessage = "Error while starting " + dataConsumer.getEntityType() + " on " + resource.getAddress() + " " + e.getMessage();
                    if (channel != null) {
                        if (!channel.isClosed())
                            errorMessage += ". The SSH remote channel is still open - the DS may be up and running. ";
                        else
                            errorMessage += "Remote process exit-status " + channel.getExitStatus();
                    }

                    // TODO we may now collect the error log file to report back the issue 
                    throw new DeploymentException(errorMessage);

                } catch (InterruptedException ie) {
                    LOGGER.info("Interrupted " + ie.getMessage());
                }
                  catch (DeploymentException de) {
                    throw de;
                  }
             
                  finally {
                    // as the command was started without a pty when we close the channel 
                    // and session the remote command will continue to run
                    if (channel != null && session != null) {
                        channel.disconnect();
                    }
                  }
            return dataConsumer.getId();
            }
    }
    

    @Override
    public boolean stopDataConsumer(ID dataConsumerID) throws DeploymentException {
        Session session = null;
        Channel channel = null;
       
        InetSocketAddress resourceAddressFromDCID;
        DataConsumerInfo dataConsumer = null;    
        
        resourceAddressFromDCID = dataConsumersResources.get(dataConsumerID); 
        
        try {
            if (resourceAddressFromDCID == null)
                if (infoPlaneDelegate.containsDataConsumer(dataConsumerID))
                    throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID + " cannot be undeployed as it was not started via the API");
                else
                    throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID + " was not found");
            
            //dataConsumer = this.resourcesDataConsumers.get(resourceAddressFromDCID);
            dataConsumer = this.dataConsumers.get(dataConsumerID);
            
            synchronized (dataConsumer) {
                if (dataConsumer == null) {
                    return false;
                }

                session = sessions.get(resourceAddressFromDCID);
                
                if (!session.isConnected()) {
                    ResourceEntityInfo resource = resources.get(resourceAddressFromDCID);
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                
                LOGGER.debug("Stopping " + dataConsumer.getEntityType());
                String command = "kill " + dataConsumer.getpID();
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);
                while (true) {
                    if (channel.isClosed()) {
                        if (channel.getExitStatus() == 0) {
                            this.dataConsumers.remove(dataConsumerID);
                            this.dataConsumersResources.remove(dataConsumerID);
                            //this.dcIDsAddresses.remove(dataConsumerID);
                            break;
                        } else {
                            // the process is likely to be already stopped: removing from the map
                            this.dataConsumers.remove(dataConsumerID);
                            this.dataConsumersResources.remove(dataConsumerID);
                            //this.dcIDsAddresses.remove(dataConsumerID);
                            throw new DeploymentException("exit-status: " + channel.getExitStatus());
                        }
                    }
                    Thread.sleep(500);
                }
            }
        } catch (JSchException | DCNotFoundException  e) {
                throw new DeploymentException("Error while stopping DataConsumer, " + e.getMessage());
        } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
        } finally {
                if (session != null && channel != null) {
                    channel.disconnect();
                    session.disconnect();
                }
            }
        return true;
    }
    
    
    @Override
    public ID startControllerAgent(ResourceEntityInfo requestedResource, ControllerAgentInfo controllerAgent) throws DeploymentException {
        LOGGER.info("Starting Controller Agent: " + controllerAgent.getEntityClassName());
        
        Session session = null;
        Channel channel = null;
         
        if (!resources.containsKey(requestedResource.getAddress()))
            resources.put(requestedResource.getAddress(), requestedResource);
        
        ResourceEntityInfo resource = resources.get(requestedResource.getAddress());
        
        controllerAgents.putIfAbsent(controllerAgent.getId(), controllerAgent);
        
        synchronized(controllerAgent)
            {
             try {
                if (sessions.get(resource.getAddress()) == null) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                else
                    session = sessions.get(resource.getAddress());
                
                if (!session.isConnected()) {
                    session = this.connectWithKey(resource);
                    sessions.put(resource.getAddress(), session);
                }
                
                this.deployJarOnResource(resource, session);

                LOGGER.debug("Future " + controllerAgent.getEntityType() + " ID: " + controllerAgent.getId());

                String jvm = "java"; //we assume the executable is in the PATH
                String command = jvm + 
                                 " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + 
                                 controllerAgent.getEntityClassName() + " " +   
                                 controllerAgent.getId() + " " +
                                 controllerAgent.getArguments();
                
                LOGGER.debug(command);

                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);

                // we are supposed to wait here until either the announce message sent by the MM 
                // is received by the Announcelistener thread or the timeout is reached (20 secs)
                infoPlaneDelegate.addControllerAgent(controllerAgent, resource, 20000);

                // if there is no Exception before we can now try to get the Data Source PID
                controllerAgent.setpID(infoPlaneDelegate.getControllerAgentPIDFromID(controllerAgent.getId()));
                controllerAgent.setRunning();
                controllerAgent.setStartedTime();
                
                controllerAgentsResources.put(controllerAgent.getId(), resource.getAddress());

                // has to catch DeploymentException    
                } catch (JSchException | ControllerAgentNotFoundException e) {
                    // we are here if there was an error while starting the remote Data Source
                    String errorMessage = "Error while starting " + controllerAgent.getEntityType() + " on " + resource.getAddress() + " " + e.getMessage();
                    if (channel != null) {
                        if (!channel.isClosed())
                            errorMessage += ". The SSH remote channel is still open - the Controller Agent may be up and running. ";
                        else
                            errorMessage += "Remote process exit-status " + channel.getExitStatus();
                    }

                    // TODO we may now collect the error log file to report back the issue 
                    throw new DeploymentException(errorMessage);

                } catch (InterruptedException ie) {
                    LOGGER.info("Interrupted " + ie.getMessage());
                }
                  catch (DeploymentException de) {
                    throw de;
                  }
             
                  finally {
                    // as the command was started without a pty when we close the channel 
                    // and session the remote command will continue to run
                    if (channel != null && session != null) {
                        channel.disconnect();
                        //session.disconnect();
                    }
                  }
            return controllerAgent.getId();
            }
    }
    

    @Override
    public boolean stopControllerAgent(ID controllerAgentID) throws DeploymentException {
        Session session = null;
        Channel channel = null;
       
        InetSocketAddress resourceAddress;
        ControllerAgentInfo controllerAgent = null;
        
        resourceAddress = controllerAgentsResources.get(controllerAgentID);
        
        try {
            if (resourceAddress == null)
                if (infoPlaneDelegate.containsControllerAgent(controllerAgentID))
                    throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID + " cannot be undeployed as it was not started via the API");
                else
                    throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID + " was not found");
        
            
            controllerAgent = controllerAgents.get(controllerAgentID);
            synchronized (controllerAgent) {
                if (controllerAgent == null) {
                    return false;
                }

                session = this.connectWithKey(resources.get(resourceAddress));
                LOGGER.debug("Stopping " + controllerAgent.getEntityType());
                String command = "kill " + controllerAgent.getpID();
                LOGGER.debug(command);
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);
                while (true) {
                    if (channel.isClosed()) {
                        if (channel.getExitStatus() == 0) {
                            controllerAgents.remove(controllerAgentID);
                            controllerAgentsResources.remove(controllerAgentID);
                            break;
                        } else {
                            // the process is likely to be already stopped: removing from the map
                            controllerAgents.remove(controllerAgentID);
                            controllerAgentsResources.remove(controllerAgentID);
                            throw new DeploymentException("exit-status: " + channel.getExitStatus());
                        }
                    }
                    Thread.sleep(500);
                }
            }
        } catch (JSchException | ControllerAgentNotFoundException e) {
                throw new DeploymentException("Error while stopping Controller Agent, " + e.getMessage());
        } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
        } finally {
                if (session != null && channel != null) {
                    channel.disconnect();
                    session.disconnect();
                }
            }
        return true;
    }

    
    boolean deployJarOnResource(ResourceEntityInfo resource, Session session) throws DeploymentException {
        synchronized (resource) 
            {
            File jarFile = new File(this.localJarFilePath + "/" + this.jarFileName);
                if (!jarFile.exists()) {
                    throw new DeploymentException("Error: file " + this.localJarFilePath + "/" + this.jarFileName + " does not exist");
                }

            if (resource.isJarDeployed() && jarFile.lastModified() <= resource.getJarDeploymentDate()) {
                return false;
            }
            
            LOGGER.debug("Deploying " + jarFile.getName() + " on" + resource.getAddress());
            
            ChannelSftp channelSftp = null;
            
            try {
                Channel channel = session.openChannel("sftp");
                channel.connect(3000);
                channelSftp = (ChannelSftp) channel;
                channelSftp.put(this.localJarFilePath + "/" + this.jarFileName, this.remoteJarFilePath + "/" + this.jarFileName, ChannelSftp.OVERWRITE);
                
                LOGGER.debug("Copying: " + this.localJarFilePath + "/" + this.jarFileName 
                                         + "to: " + this.remoteJarFilePath + "/" + this.jarFileName);
                
                resource.setJarDeploymentDate(jarFile.lastModified());
                resource.setJarDeployed();
                
            } catch (JSchException | SftpException e) {
                throw new DeploymentException("Error while deploying " + jarFile.getName() + " on " + resource.getAddress() + ", " + e.getMessage());
            } finally {
                if (channelSftp != null)
                    channelSftp.disconnect();
            }
        }
        return true;
    }
    
    
    
    Session connectWithKey(ResourceEntityInfo resource) throws JSchException {
        LOGGER.debug("Using identity from file: " + identityFile);
        jsch.addIdentity(identityFile);
        Session session = jsch.getSession(resource.getCredentials(), resource.getAddress().getHostName(), resource.getAddress().getPort());
        session.setConfig("PreferredAuthentications", "publickey");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(3000);
        return session;
    }
}
