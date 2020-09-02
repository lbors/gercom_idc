/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.controller.json;

import cc.clayman.console.ManagementConsole;
import mon.lattice.control.console.JSONControllerManagementConsole;
import mon.lattice.control.console.RestConsoleInterface;
import mon.lattice.control.deployment.DataConsumerInfo;
import mon.lattice.control.deployment.DataSourceInfo;
import mon.lattice.control.deployment.DeploymentException;
import mon.lattice.control.deployment.DeploymentInterface;
import mon.lattice.control.deployment.EntityDeploymentDelegate;
import mon.lattice.control.deployment.ssh.SSHDeploymentManager;
import mon.lattice.control.deployment.ssh.SSHServerEntityInfo;
import mon.lattice.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Extends the AbstractController and implements
 * the deployment functions and a JSON based REST API
 * @author uceeftu
 */
public abstract class AbstractJSONRestController extends AbstractJSONController implements DeploymentInterface<JSONObject>, RestConsoleInterface {
    protected EntityDeploymentDelegate deploymentManager;
    protected Boolean usingDeploymentManager = true;
    
    protected String localJarPath;
    protected String jarFileName;
    protected String remoteJarPath;
    
    protected String dsClassName;
    protected String dcClassName;
    
    protected int restConsolePort;
    protected ManagementConsole JSONManagementConsole = null;
    
    
    
    private static Logger LOGGER;
    
    
    protected AbstractJSONRestController() {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        LOGGER = LoggerFactory.getLogger(AbstractJSONRestController.class);
    }
    
    
    @Override
    abstract public void initPlanes();
    
    
    @Override
    public void initRESTConsole() {
        restConsolePort = Integer.parseInt(pr.getProperty("restconsole.localport"));
        JSONManagementConsole=new JSONControllerManagementConsole(this, restConsolePort);
        JSONManagementConsole.start();  
    }
    
    
    @Override
    public void initDeployment() {
        
        localJarPath = pr.getProperty("deployment.localJarPath");
        jarFileName = pr.getProperty("deployment.jarFileName");
        remoteJarPath = pr.getProperty("deployment.remoteJarPath");
        
        dsClassName = pr.getProperty("deployment.ds.className");
        dcClassName = pr.getProperty("deployment.dc.className");
        
        if (localJarPath != null && jarFileName != null && remoteJarPath != null) {
            if (this.usingDeploymentManager) {
                deploymentManager = new SSHDeploymentManager(localJarPath, jarFileName, remoteJarPath, controlInformationManager);
                LOGGER.info("Deployment Manager has been activated");
            }
            else {
                LOGGER.warn("Deployment Manager has not been activated");
                this.usingDeploymentManager = false;
            }
        }
        
    }
    
    
    @Override
    public JSONObject startDataSource(String endPoint, String port, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDsID;
        
        result.put("operation", "startDataSource");
        result.put("endpoint",endPoint);

        if (this.usingDeploymentManager) {
            try {
                startedDsID = this.deploymentManager.startDataSource(new SSHServerEntityInfo(endPoint, Integer.valueOf(port), userName), 
                                                                       new DataSourceInfo(dsClassName, args));

                if (startedDsID == null) {
                    result.put("msg", "en error occured while starting the Data Source on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDsID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "DeploymentException while performing startDataSource operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    @Override
    public JSONObject stopDataSource(String dataSourceID) throws JSONException {
            
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDataSource");
        result.put("ID", dataSourceID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataSource(ID.fromString(dataSourceID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "DeploymentException while performing stopDataSource operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    @Override
    public JSONObject startDataConsumer(String endPoint, String port, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDcID;
        
        result.put("operation", "startDataConsumer");
        result.put("endpoint",endPoint);

        if (this.usingDeploymentManager) {
            try {
                startedDcID = this.deploymentManager.startDataConsumer(new SSHServerEntityInfo(endPoint, Integer.valueOf(port), userName), 
                                                                         new DataConsumerInfo(dcClassName, args));

                if (startedDcID == null) {
                    result.put("msg", "en error occured while starting the Data Consumer on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDcID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "DeploymentException while performing startDataConsumer operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    } 
    

    @Override
    public JSONObject stopDataConsumer(String dataConsumerID) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDataConsumer");
        result.put("ID", dataConsumerID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataConsumer(ID.fromString(dataConsumerID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "DeploymentException while performing stopDataConsumer operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }

    @Override
    public JSONObject startControllerAgent(String endPoint, String port, String userName, String className, String args) throws Exception {
        JSONObject result = new JSONObject();
        result.put("operation", "startControllerAgent");
        result.put("success", false);
        result.put("msg", "Not supported by this controller " + this.getClass().getName());
        return result;
    }

    @Override
    public JSONObject stopControllerAgent(String id) throws Exception {
        JSONObject result = new JSONObject();
        result.put("operation", "stopControllerAgent");
        result.put("success", false);
        result.put("msg", "Not supported by this controller " + this.getClass().getName());
        return result;
    }
    
}
