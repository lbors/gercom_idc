/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.controller.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import mon.lattice.control.zmq.ZMQControlPlaneXDRProducer;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.im.zmq.ZMQControllerInfoPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ZMQ implementation of the Abstract Controller without REST API
 * @author uceeftu
 */
public class SimpleJSONController extends AbstractJSONController {
    
    private static final SimpleJSONController CONTROLLER = new SimpleJSONController();
    
    private static Logger LOGGER;
    

    protected SimpleJSONController() {}
    
    
    public static SimpleJSONController getInstance() {
        return CONTROLLER;
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
        ControlPlane controlPlane = new ZMQControlPlaneXDRProducer(transmitterPoolSize, controlLocalPort);
        
        // setting a reference to the InfoPlaneDelegate on the Control Plane
        ((InfoPlaneDelegateInteracter) controlPlane).setInfoPlaneDelegate(controlInformationManager);
        setControlPlane(controlPlane);
        
        connect();
    }
    
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(ZMQController.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/SimpleController.properties";
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
        
        SimpleJSONController myController = SimpleJSONController.getInstance();
        myController.setPropertyHandler(prop);
        myController.initPlanes();
    }
    
    
    
    
    
}
