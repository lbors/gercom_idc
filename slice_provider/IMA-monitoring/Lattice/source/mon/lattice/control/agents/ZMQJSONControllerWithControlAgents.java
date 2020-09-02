/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import mon.lattice.control.ControlServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.control.controller.json.ZMQController;
import mon.lattice.control.deployment.ControllerAgentInfo;
import mon.lattice.control.deployment.DeploymentException;
import mon.lattice.control.deployment.ssh.SSHServerEntityInfo;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.im.zmq.ZMQControllerInfoPlane;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.core.plane.ControllerControlPlaneWithAgents;

/**
 *
 * @author uceeftu
 */
public class ZMQJSONControllerWithControlAgents extends ZMQController implements ControllerAgentInterface<JSONObject> {
    
    private static final ZMQJSONControllerWithControlAgents CONTROLLER = new ZMQJSONControllerWithControlAgents();
    
    private static Logger LOGGER;
    

    private ZMQJSONControllerWithControlAgents() {}
    
    
    public static ZMQJSONControllerWithControlAgents getInstance() {
        return CONTROLLER;
    }
    
    private ControllerControlPlaneWithAgents getControlAndManagementHandle() {
        return (ControllerControlPlaneWithAgents)getControlPlane();
    }
    
    @Override
    public void initPlanes() {
        controlLocalPort = Integer.parseInt(pr.getProperty("control.localport"));
        infoPlanePort = Integer.parseInt(pr.getProperty("info.localport"));
        
        transmitterPoolSize = Integer.parseInt(pr.getProperty("control.poolsize"));
        
        // ZMQController is the root of the infoPlane - other nodes use it to perform bootstrap
        InfoPlane infoPlane = new ZMQControllerInfoPlane(infoPlanePort);
        
        // we get the ControlInformationManager from the InfoPlane
        controlInformationManager = ((InfoPlaneDelegateInteracter) infoPlane).getInfoPlaneDelegate();
        
	setInfoPlane(infoPlane);
        
        // create a ZMQ control plane producer
        ControlPlane controlPlane = new ZMQControlPlaneXDRProducerWithControlAgents(transmitterPoolSize, controlLocalPort);
        
        // setting a reference to the InfoPlaneDelegate on the Control Plane
        ((InfoPlaneDelegateInteracter) controlPlane).setInfoPlaneDelegate(controlInformationManager);
        setControlPlane(controlPlane);
        
        connect();
    }

    @Override
    public JSONObject stopControllerAgent(String mmID) throws Exception {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopControllerAgent");
        result.put("ID", mmID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopControllerAgent(ID.fromString(mmID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "DeploymentException while performing stopControllerAgent operation: " + ex.getMessage());
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
        
        ID controllerAgentID;
        
        result.put("operation", "startControllerAgent");
        result.put("endpoint", endPoint);

        if (this.usingDeploymentManager) {
            try {
                controllerAgentID = this.deploymentManager.startControllerAgent(new SSHServerEntityInfo(endPoint, Integer.valueOf(port), userName), 
                                                                       new ControllerAgentInfo(className, args));

                if (controllerAgentID == null) {
                    result.put("msg", "en error occured while starting the Controller Agent on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", controllerAgentID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "DeploymentException while performing startControllerAgent operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    
    
    @Override
    public JSONObject setCollectionRate(Rational collectionRate) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getConnectionRate() throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject setMonitoringReportingEndpoint(String id, String address, String port) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean successfulInvocation;
        
        result.put("operation", "setMonitoringReportingEndpoint");
        result.put("address", address);
        result.put("port", port);
        
        try {
            successfulInvocation = this.getControlAndManagementHandle().setMonitoringReportingEndpoint(ID.fromString(id), address, Integer.valueOf(port));
            if (!successfulInvocation)
                result.put("msg", "There was an error while performing the operation on the external Monitoring System");
                
            result.put("success", successfulInvocation);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        LOGGER.info(result.toString());
        return result;
    }
    

    @Override
    public JSONObject getControllerAgents() throws Exception {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getControllerAgents");
        
        try {
            JSONArray controllerAgents = this.controlInformationManager.getControllerAgents();
            result.put("controlleragents", controllerAgents);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getControllerAgents operation: " + ex.getMessage());
          }
        return result; 
    }
    
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(ZMQJSONControllerWithControlAgents.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/ControllerWithAgents.properties";
                break;
            case 1:
                propertiesFile = args[0];
                break;
            default:
                LOGGER.error("Controller main: please use: java Controller [file.properties]");
                System.exit(1);
        }
        
	try {
            // loading properties file
            input = new FileInputStream(propertiesFile);
            prop.load(input);
            
	} catch (Exception ex) {
		LOGGER.error("Error while opening the property file: " + ex.getMessage());
                LOGGER.error("Falling back to default configuration values");
	} finally {        
            if (input != null) {
                try {
                    input.close();
                    } catch (IOException e) {        
                    }
            }
        }
        
        ZMQJSONControllerWithControlAgents myController = ZMQJSONControllerWithControlAgents.getInstance();
        myController.setPropertyHandler(prop);
        myController.initPlanes();
        myController.initDeployment();
        myController.initRESTConsole();
    }
}
