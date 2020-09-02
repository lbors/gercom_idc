/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.controller.json;

import mon.lattice.core.plane.InfoPlane;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import mon.lattice.control.udp.UDPControlPlaneXDRProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.im.dht.tomp2p.TomP2PDHTRootInfoPlane;

/**
 *
 * @author uceeftu
 */
public class UDPController extends AbstractJSONRestController {
    
    private static final UDPController CONTROLLER = new UDPController();
    
    private static Logger LOGGER;
    

    private UDPController() {
        
    }
    
    
    @Override
    public void initPlanes() {
        controlLocalPort = Integer.parseInt(pr.getProperty("control.localport"));
        infoPlanePort = Integer.parseInt(pr.getProperty("info.localport"));
        
        usingDeploymentManager = Boolean.valueOf(pr.getProperty("deployment.enabled", "false"));
        
        //announceListeningPort = Integer.parseInt(pr.getProperty("control.announceport"));
        transmitterPoolSize = Integer.parseInt(pr.getProperty("control.poolsize"));
        
        InfoPlane infoPlane = new TomP2PDHTRootInfoPlane(infoPlanePort);
        
        // we get the ControlInformationManager from the InfoPlane
        controlInformationManager = ((InfoPlaneDelegateInteracter) infoPlane).getInfoPlaneDelegate();
        
        // setting the InfoPlane to send announce events to the ControlInformationManager
        ((TomP2PDHTRootInfoPlane) infoPlane).addAnnounceEventListener(controlInformationManager);
        
	setInfoPlane(infoPlane);
        
        // create a control plane producer 
        // announcePort to listen for announce Messages from DSs/DCs
        // maxPoolSize to instantiate a pool of UDP Transmitters (each transmitter is not connected to any specific DS)
        //ControlPlane controlPlane = new UDPControlPlaneXDRProducer(8888, transmitterPoolSize);
        
        // create a control plane producer without announce listening capabilities 
        // as this is implemented in the used info plane implementation
        ControlPlane controlPlane = new UDPControlPlaneXDRProducer(transmitterPoolSize);
        
        // setting a reference to the InfoPlaneDelegate on the Control Plane
        ((InfoPlaneDelegateInteracter) controlPlane).setInfoPlaneDelegate(controlInformationManager);
        ((UDPControlPlaneXDRProducer) controlPlane).addAnnounceEventListener(controlInformationManager);
        setControlPlane(controlPlane);
        
        connect();
    }
    
    
    public static UDPController getInstance() {
        return CONTROLLER;
    }
    
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(UDPController.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/UDPController.properties";
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
        
        UDPController myController = UDPController.getInstance();
        myController.setPropertyHandler(prop);
        myController.initPlanes();
        myController.initDeployment();
        myController.initRESTConsole();
    }
}
