/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import java.io.IOException;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.MessageType;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mon.lattice.control.deployment.ControllerAgentInfo;
import mon.lattice.control.deployment.DataConsumerInfo;
import mon.lattice.control.deployment.DataSourceInfo;
import mon.lattice.control.deployment.ResourceEntityInfo;
import mon.lattice.core.plane.InfoPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class ControlInformationManager implements InfoPlaneDelegate {
    private final InfoPlane info;
    private final List<ID> dataSources;
    private final List<ID> dataConsumers;
    private final List<ID> controllerAgents;
    private final Map<ID, Object> pendingDataSources;
    private final Map<ID, Object> pendingDataConsumers;
    private final Map<ID, Object> pendingControllerAgents;
    
    private final Map<ID, ResourceEntityInfo> dataSourcesResourcesInfo;
    private final Map<ID, ResourceEntityInfo> dataConsumersResourcesInfo;
    private final Map<ID, ResourceEntityInfo> controllerAgentsResourcesInfo;
    
    private final Map<ID, DataSourceInfo> dataSourcesMap;
    private final Map<ID, DataConsumerInfo> dataConsumersMap;
    private final Map<ID, ControllerAgentInfo> controllerAgentsMap;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ControlInformationManager.class);
    
    
    public ControlInformationManager(InfoPlane info){
        this.info=info;
        dataSources = Collections.synchronizedList(new ArrayList());
        dataConsumers = Collections.synchronizedList(new ArrayList());
        controllerAgents = Collections.synchronizedList(new ArrayList());
        pendingDataSources = new ConcurrentHashMap<>();
        pendingDataConsumers = new ConcurrentHashMap<>();
        pendingControllerAgents = new ConcurrentHashMap<>();
        
        dataSourcesResourcesInfo = new ConcurrentHashMap<>();
        dataConsumersResourcesInfo = new ConcurrentHashMap<>();
        controllerAgentsResourcesInfo = new ConcurrentHashMap<>(); 
        
        dataSourcesMap = new ConcurrentHashMap<>();
        dataConsumersMap = new ConcurrentHashMap<>();
        controllerAgentsMap = new ConcurrentHashMap<>();
    }
    
    
    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        if (m.getMessageType() == MessageType.ANNOUNCE)
            addAnnouncedEntity(m.getEntityID(), m.getEntity());
        else if ((m.getMessageType() == MessageType.DEANNOUNCE))
                removeDeannouncedEntity(m.getEntityID(), m.getEntity());  
    }
    
    
    @Override
    public void addDataSource(DataSourceInfo dataSource, ResourceEntityInfo resource, int timeout) throws InterruptedException, DSNotFoundException {
        ID id = dataSource.getId();
        Object monitor = new Object(); 
        synchronized(monitor) {
            LOGGER.debug("Adding pending Data Source: " + id);
            pendingDataSources.put(id, monitor);
            monitor.wait(timeout);
        }
        if (pendingDataSources.containsKey(id)) //cleaning up
            pendingDataSources.remove(id);
        
        if (!containsDataSource(id)) {  
            if (!info.containsDataSource(id, 0)) //wait some more time
                throw new DSNotFoundException("Announce Message was not received by the ControlInformationManager");
            else
                addDataSource(id); //we may have lost the message but the DS might be up and running
        }
        
        dataSourcesResourcesInfo.put(id, resource);
        dataSourcesMap.put(id, dataSource);
    }
    
    
    @Override
    public void addDataConsumer(DataConsumerInfo dataConsumer, ResourceEntityInfo resource, int timeout) throws InterruptedException, DCNotFoundException {
        ID id = dataConsumer.getId();
        Object monitor = new Object(); 
        synchronized(monitor) {
            LOGGER.debug("Adding pending Data Consumer: " + id);
            pendingDataConsumers.put(id, monitor);
            monitor.wait(timeout);
        }
        if (pendingDataConsumers.containsKey(id)) //cleaning up
            pendingDataConsumers.remove(id);
        
        if (!containsDataConsumer(id)) {  
            if (!info.containsDataConsumer(id, 0))
                throw new DCNotFoundException("Announce Message was not received by the ControlInformationManager");
            else
                addDataConsumer(id); //we may have lost the message but the DC is up and running
        }
        dataConsumersResourcesInfo.put(id, resource);
        dataConsumersMap.put(id, dataConsumer);
    }
    

    @Override
    public void addControllerAgent(ControllerAgentInfo controllerAgent, ResourceEntityInfo resource, int timeout) throws InterruptedException, ControllerAgentNotFoundException {
        ID id = controllerAgent.getId();
        Object monitor = new Object(); 
        synchronized(monitor) {
            LOGGER.debug("Adding pending Controller Agent: " + id);
            pendingControllerAgents.put(id, monitor);
            monitor.wait(timeout);
        }
        if (pendingControllerAgents.containsKey(id)) //cleaning up
            pendingControllerAgents.remove(id);
        
        if (!containsControllerAgent(id)) {  
            if (!info.containsControllerAgent(id, 0))
                throw new ControllerAgentNotFoundException("Announce Message was not received by the ControlInformationManager");
            else
                addControllerAgent(id); //we may have lost the message but the DC is up and running
        }
        controllerAgentsResourcesInfo.put(id, resource);
        controllerAgentsMap.put(id, controllerAgent);
    }

    
    @Override
    public JSONArray getDataSources() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id: getDataSourcesList()) {
            JSONObject dsAddr = new JSONObject();
            JSONObject dataSourceInfo = new JSONObject();
            try {
                ControlEndPointMetaData dsInfo = getDSAddressFromID(id);
                if (dsInfo instanceof ZMQControlEndPointMetaData)
                    dsAddr.put("type", ((ZMQControlEndPointMetaData)dsInfo).getType());
                else if (dsInfo instanceof SocketControlEndPointMetaData) {
                    dsAddr.put("host", ((SocketControlEndPointMetaData)dsInfo).getHost().getHostAddress());
                    dsAddr.put("port", ((SocketControlEndPointMetaData)dsInfo).getPort());
                }
                dataSourceInfo.put("id", id.toString());
                dataSourceInfo.put("info", dsAddr);
                
                ResourceEntityInfo resource = dataSourcesResourcesInfo.get(id);
                DataSourceInfo dataSource = dataSourcesMap.get(id);
                
                JSONObject deployment = new JSONObject();
                
                if (resource != null && dataSource != null) {
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(dataSource.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                }
                else
                    deployment.put("type", "none");
                
                dataSourceInfo.put("deployment", deployment);
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
            }
              catch (DSNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                deleteDataSource(id);
              }
            obj.put(dataSourceInfo);
        }
        return obj;
    }
    
    @Override
    public JSONArray getDataConsumers() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id: getDataConsumersList()) {
            JSONObject dcAddr = new JSONObject();
            JSONObject dataConsumerInfo = new JSONObject();
            try {
                ControlEndPointMetaData dcInfo = getDCAddressFromID(id);
                if (dcInfo instanceof ZMQControlEndPointMetaData)
                    dcAddr.put("type", ((ZMQControlEndPointMetaData)dcInfo).getType());
                else if (dcInfo instanceof SocketControlEndPointMetaData) {
                    dcAddr.put("host", ((SocketControlEndPointMetaData)dcInfo).getHost().getHostAddress());
                    dcAddr.put("port", ((SocketControlEndPointMetaData)dcInfo).getPort());
                }
                dataConsumerInfo.put("id", id.toString());
                dataConsumerInfo.put("info", dcAddr);
                
                ResourceEntityInfo resource = dataConsumersResourcesInfo.get(id);
                DataConsumerInfo dataConsumer = dataConsumersMap.get(id);
                
                JSONObject deployment = new JSONObject();
                
                if (resource != null && dataConsumer != null) {
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(dataConsumer.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                }
                else
                    deployment.put("type", "none");
                
                dataConsumerInfo.put("deployment", deployment);
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
              }
              catch (DCNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                deleteDataConsumer(id);
              }
            obj.put(dataConsumerInfo);
            }
        return obj;
    }

    @Override
    public JSONArray getControllerAgents() throws JSONException {
        JSONArray obj = new JSONArray();
        for (ID id: getControllerAgentsList()) {
            JSONObject controllerAgentAddr = new JSONObject();
            JSONObject controllerAgentInfo = new JSONObject();
            try {
                ControlEndPointMetaData controllerAgentEndPointInfo = getControllerAgentAddressFromID(id);
                if (controllerAgentEndPointInfo instanceof ZMQControlEndPointMetaData)
                    controllerAgentAddr.put("type", ((ZMQControlEndPointMetaData)controllerAgentEndPointInfo).getType());
                else if (controllerAgentEndPointInfo instanceof SocketControlEndPointMetaData) {
                    controllerAgentAddr.put("host", ((SocketControlEndPointMetaData)controllerAgentEndPointInfo).getHost().getHostAddress());
                    controllerAgentAddr.put("port", ((SocketControlEndPointMetaData)controllerAgentEndPointInfo).getPort());
                }
                controllerAgentInfo.put("id", id.toString());
                controllerAgentInfo.put("info", controllerAgentAddr);
                
                ResourceEntityInfo resource = controllerAgentsResourcesInfo.get(id);
                ControllerAgentInfo controllerAgent = controllerAgentsMap.get(id);
                
                JSONObject deployment = new JSONObject();
                
                if (resource != null && controllerAgent != null) {
                    deployment.put("type", "ssh");
                    deployment.put("InetSocketAddress", resource.getAddress());
                    Date date = new Date(controllerAgent.getStartedTime());
                    deployment.put("date", date.toInstant().toString());
                }
                else
                    deployment.put("type", "none");
                
                controllerAgentInfo.put("deployment", deployment);
                
            } catch (IOException ioex) {
                throw new JSONException(ioex);
              }
              catch (ControllerAgentNotFoundException ex) {
                LOGGER.error(ex.getMessage());
                deleteControllerAgent(id);
              }
            obj.put(controllerAgentInfo);
            }
        return obj;
    }
    
    
    
    @Override
    public ControlEndPointMetaData getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException, IOException {
        String dsID = (String)info.lookupProbeInfo(probe, "datasource");
        
        if (dsID != null) {
            ID dataSourceID = ID.fromString(dsID);
            if (!containsDataSource(dataSourceID))
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " was de-announced");
            
            LOGGER.debug("Found this data source ID: " + dataSourceID);
            ControlEndPointMetaData dsAddress = getDSAddressFromID(dataSourceID);
            if (dsAddress != null)
                return dsAddress;
            else
                throw new DSNotFoundException("Data Source with ID " + dataSourceID.toString() + " not found in the infoplane");
        }
        else {
            LOGGER.error("Probe ID error");
            throw new ProbeNotFoundException("Probe with ID " + probe.toString() + " not found in the infoplane");
        }
    }
    
    @Override
    public ControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was not found in the infoplane");
        
        return this.fetchDataSourceControlEndPoint(dataSource);
        
    }
        
    @Override
    public String getDSIDFromName(String dsName) throws DSNotFoundException {
        //using generic getInfo method for getting DS ID from DS name
        String dsID = (String)info.getInfo("/datasource/name/" + dsName);
        if (dsID != null)
            if (!containsDataSource(ID.fromString(dsID)))
                throw new DSNotFoundException("Data Source with ID " + dsID + " was de-announced");
            else
                return dsID;
        else 
            throw new DSNotFoundException("Data Source with name " + dsName + " not found in the infoplane");
        }  
    
    @Override
    public ControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was not found in the infoplane");
        
        return this.fetchDataConsumerControlEndPoint(dataConsumer);    
    }
    
    @Override
    public ControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException {
        String dcID = (String)info.lookupReporterInfo(reporter, "dataconsumer");
        
        if (dcID != null) {
            ID dataConsumerID = ID.fromString(dcID);
            if (!containsDataConsumer(dataConsumerID))
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " was de-announced");
                
            LOGGER.debug("Found this data consumer ID: " + dataConsumerID);
            ControlEndPointMetaData dcAddress = getDCAddressFromID(dataConsumerID);
            if (dcAddress != null)
                return dcAddress;
            else
                throw new DCNotFoundException("Data Consumer with ID " + dataConsumerID.toString() + " not found in the infoplane");
        }
        else
            throw new ReporterNotFoundException("Probe with ID " + reporter.toString() + " not found in the infoplane");
    }
    
    @Override
    public int getDSPIDFromID(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataSourceInfo(dataSource, "pid");
        if (pID != null)
            return pID;
        else 
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " not found in the infoplane or missing pid entry"); 
    }
    
    @Override
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException {
        if (!containsDataConsumer(dataConsumer))
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupDataConsumerInfo(dataConsumer, "pid");
        if (pID != null)
            return pID;
        else
            throw new DCNotFoundException("Data Consumer with ID " + dataConsumer.toString() + " not found in the infoplane or missing pid entry");
    }

    @Override
    public int getControllerAgentPIDFromID(ID controllerAgent) throws ControllerAgentNotFoundException {
        if (!containsControllerAgent(controllerAgent))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " was de-announced");
        
        Integer pID = (Integer)info.lookupControllerAgentInfo(controllerAgent, "pid");
        if (pID != null)
            return pID;
        else
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgent.toString() + " not found in the infoplane or missing pid entry");
    }
    
    
    
    

    @Override
    public JSONArray getProbesOnDS(ID dataSource) throws DSNotFoundException {
        if (!containsDataSource(dataSource))
            throw new DSNotFoundException("Data Source with ID " + dataSource.toString() + " was de-announced");
        
        JSONArray probes = (JSONArray) info.lookupProbesOnDataSource(dataSource);
        return probes;
    }
    
    @Override
    public ControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgentID) throws ControllerAgentNotFoundException, IOException {
        if (!containsControllerAgent(controllerAgentID))
            throw new ControllerAgentNotFoundException("Controller Agent with ID " + controllerAgentID.toString() + " was not found in the infoplane");
        
        return this.fetchControllerAgentControlEndPoint(controllerAgentID);    
    }
    
    
    
    @Override
    public boolean containsDataSource(ID id) {
        return dataSources.contains(id);
    }
    
    @Override
    public boolean containsDataConsumer(ID id) {
        return dataConsumers.contains(id);
    }
    
    @Override
    public boolean containsControllerAgent(ID id) {
        return controllerAgents.contains(id);
    }
    
    void addDataSource(ID id) {
        dataSources.add(id);
    }
    
    void addDataConsumer(ID id) {
        dataConsumers.add(id);
    }
    
    void addControllerAgent(ID id) {
        controllerAgents.add(id);
    }
    
    void deleteDataSource(ID id) {    
        dataSources.remove(id);
        this.dataSourcesMap.remove(id);
        this.dataSourcesResourcesInfo.remove(id);
    }
    
    void deleteDataConsumer(ID id) {
        dataConsumers.remove(id);
    }
    
    void deleteControllerAgent(ID id) {
        controllerAgents.remove(id);
    }
    
    List<ID> getDataSourcesList() {
        synchronized(dataSources) {
            return dataSources;
        }
    }
    
    List<ID> getDataConsumersList() {
        synchronized(dataConsumers) {
            return dataConsumers;
        }
    }
    
    List<ID> getControllerAgentsList() {
        synchronized(controllerAgents) {
            return controllerAgents;
        }
    }
    
    
    private ControlEndPointMetaData fetchDataSourceControlEndPoint(ID dataSourceID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupDataSourceInfo(dataSourceID, "controlEndPoint");
        return parseControlEndPointInfo(rawControlEndPointInfo, dataSourceID);
    }
    
    
    private ControlEndPointMetaData fetchDataConsumerControlEndPoint(ID dataConsumerID) throws IOException {
        Object rawControlEndPointInfo = info.lookupDataConsumerInfo(dataConsumerID, "controlEndPoint");
        return parseControlEndPointInfo(rawControlEndPointInfo, dataConsumerID);
    }
    
    private ControlEndPointMetaData fetchControllerAgentControlEndPoint(ID controllerAgentID) throws IOException {        
        Object rawControlEndPointInfo = info.lookupControllerAgentInfo(controllerAgentID, "controlEndPoint");
        return parseControlEndPointInfo(rawControlEndPointInfo, controllerAgentID);
    }
     
    private ControlEndPointMetaData parseControlEndPointInfo(Object rawControlEndPointInfo, ID entityID) throws IOException {
        JSONObject controlEndPointInfo;
        
        try {
            if (rawControlEndPointInfo instanceof String) { 
                controlEndPointInfo = new JSONObject();
                
                //example -> type:zmq;address:localhost;port:2233
                String[] controlEndPointFields = ((String) rawControlEndPointInfo).split(";");
                
                String[] type;
                String[] address;
                String[] port;
                
                switch (controlEndPointFields.length) {
                    case 1:
                        type = controlEndPointFields[0].split(":");
                        controlEndPointInfo.put(type[0], type[1]);
                        break;
                    case 3:
                        type = controlEndPointFields[0].split(":");
                        controlEndPointInfo.put(type[0], type[1]);
                        
                        address = controlEndPointFields[1].split(":");
                        controlEndPointInfo.put(address[0], address[1]);
                            
                        port = controlEndPointFields[2].split(":");
                        controlEndPointInfo.put(port[0], port[1]);
                        break;
                    default:
                        //throw error
                        break;
                }
                
            }
            
            else
                controlEndPointInfo = (JSONObject)rawControlEndPointInfo;
       
            LOGGER.debug(controlEndPointInfo.toString());
            
            ControlEndPointMetaData controlEndPointMetaData = null;
            if (controlEndPointInfo.getString("type").equals("socket")) {
                controlEndPointMetaData = new SocketControlEndPointMetaData(controlEndPointInfo.getString("type"),
                                                        InetAddress.getByName(controlEndPointInfo.getString("address")),
                                                        Integer.valueOf(controlEndPointInfo.getString("port"))
                                                       );
            }
            
            else if (controlEndPointInfo.getString("type").equals("zmq")) {
                controlEndPointMetaData = new ZMQControlEndPointMetaData(controlEndPointInfo.getString("type"), entityID);
            }
            
            return controlEndPointMetaData;
        }
        
        catch(Exception e) {
            throw new IOException("error while parsing controlEndPoint information: " + e.getMessage());
        }
        
    } 
     
    
    
    void addAnnouncedEntity(ID id, EntityType type) {
        if (type == EntityType.DATASOURCE && !containsDataSource(id)) {
            LOGGER.info("Adding Data Source " + id.toString());
            addDataSource(id);
            notifyDataSource(id); // notify any pending deployment threads
        }
        else if (type == EntityType.DATACONSUMER && !containsDataConsumer(id)) {
                LOGGER.info("Adding Data Consumer " + id.toString());
                addDataConsumer(id);
                notifyDataConsumer(id);
        } else if (type == EntityType.CONTROLLERAGENT && !containsControllerAgent(id)) {
                LOGGER.info("Adding Controller Agent " + id.toString());
                addControllerAgent(id);
                notifyControllerAgent(id);
        }
    }
    
    void removeDeannouncedEntity(ID id, EntityType type) {
        if (type == EntityType.DATASOURCE && containsDataSource(id)) {
            LOGGER.info("Removing Data Source " + id.toString());
            deleteDataSource(id);
        }
        else if (type == EntityType.DATACONSUMER && containsDataConsumer(id)) {
            LOGGER.info("Removing Data Consumer " + id.toString());
            deleteDataConsumer(id);
        } else if (type == EntityType.CONTROLLERAGENT && containsControllerAgent(id)) {
              LOGGER.info("Removing Controller Agent " + id.toString());
              deleteControllerAgent(id);
          }
        
    }
    
    void notifyDataSource(ID id) {
        // checking if there is a pending deployment for that Data Source ID
        if (pendingDataSources.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Source: " + id);
            Object monitor = pendingDataSources.remove(id);
            synchronized (monitor) {
                monitor.notify();
            }
        }
        // else do nothing
    }
    
    
    void notifyDataConsumer(ID id) {
        // checking if there is a pending deployment for that Data Consumer ID
        if (pendingDataConsumers.containsKey(id)) {
            LOGGER.debug("Notifying pending Data Consumer: " + id);
            Object monitor = pendingDataConsumers.remove(id);
            synchronized (monitor) {
                monitor.notify();
            }
        }
        // else do nothing
    }
    
    void notifyControllerAgent(ID id) {
        // checking if there is a pending deployment for that Controller Agent ID
        if (pendingControllerAgents.containsKey(id)) {
            LOGGER.debug("Notifying pending Controller Agent: " + id);
            Object monitor = pendingControllerAgents.remove(id);
            synchronized (monitor) {
                monitor.notify();
            }
        }
        // else do nothing
    }
    
}
