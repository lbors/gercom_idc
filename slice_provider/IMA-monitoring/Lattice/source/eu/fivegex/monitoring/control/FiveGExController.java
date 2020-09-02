/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control;

import eu.fivegex.monitoring.control.probescatalogue.CatalogueException;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbeCatalogue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbesCatalogue;
import mon.lattice.control.controller.json.ZMQController;

/**
 *
 * @author uceeftu
 */
public class FiveGExController extends ZMQController implements JSONProbesCatalogue {
    
    private static final FiveGExController CONTROLLER = new FiveGExController();
    
    private JSONProbeCatalogue probeCatalogue;
    protected String probesPackage; 
    protected String probesSuffix;
    
    private static Logger LOGGER;
    

    private FiveGExController() {}
    
    
    public static FiveGExController getInstance() {
        return CONTROLLER;
    }
    
    @Override
    public void initCatalogue() {
        probesPackage = pr.getProperty("probes.package");
        probesSuffix = pr.getProperty("probes.suffix");
        probeCatalogue = new JSONProbeCatalogue(probesPackage, probesSuffix);
    }
    
    
    @Override
    public JSONObject getProbesCatalogue() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getProbesCatalogue");
        
        try {
            JSONObject catalogue = this.probeCatalogue.getProbeCatalogue();
            result.put("probesCatalogue", catalogue);
            result.put("success", true);
        } catch (CatalogueException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
          }
        
        return result;   
    }
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(FiveGExController.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/5GExController.properties";
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
        
        FiveGExController myController = FiveGExController.getInstance();
        myController.setPropertyHandler(prop);
        myController.initPlanes();
        myController.initDeployment();
        myController.initRESTConsole();
        myController.initCatalogue();
    }
}
