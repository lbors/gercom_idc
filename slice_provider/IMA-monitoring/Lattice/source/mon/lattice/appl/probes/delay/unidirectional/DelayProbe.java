/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.unidirectional;

import eu.fivegex.monitoring.test.LatticeTest;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public final class DelayProbe {
    
    String username;
    Integer port;
    
    String controllerAddress;
    Integer controllerAPIPort;
    Integer controllerControlPort;
    Integer controllerInfoPort;
    
    String dcAddress;
    Integer dcPort;
    
    Integer mgmPackets;
    Integer mgmTimeout;
    Integer mgmInterval;
    
    Integer dataPackets;
    Integer dataTimeout;
    Integer dataInterval;
    
    String sourceProbeMgmAddress;
    Integer sourceProbeMgmPort;
    String sourceProbeDataAddress;
    Integer sourceProbeDataPort;
    
    String destProbeMgmAddress;
    Integer destProbeMgmPort;
    String destProbeDataAddress;
    Integer destProbeDataPort;

    LatticeTest rest;
    
    Map<String, String> uuids;
    
    JSONObject configuration;
    
    
    public DelayProbe(String filename) throws IOException {
        loadConfiguration(filename);
        parseConfiguration();
        init();
    }
   
    
    private void init() throws IOException {
        rest = new LatticeTest(controllerAddress, controllerAPIPort);
        uuids = new HashMap<>();
    }
    
    private void loadConfiguration(String filename) throws IOException {
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(filename));
            
            String line;
            StringBuilder content = new StringBuilder();
        
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            configuration = new JSONObject(content.toString());
            
        } catch (FileNotFoundException e) {
            throw e;
        } catch (JSONException je) {
            throw new IOException(je);
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }
    
    
    private void parseConfiguration() throws IOException {
        JSONObject latticeConf;
        JSONObject probes;
        JSONObject mgm;
        JSONObject data;
        
        try {
            latticeConf = configuration.getJSONObject("lattice");
            System.out.println(latticeConf);
            probes = latticeConf.getJSONObject("probes");
        } catch (JSONException je) {
            throw new IOException("Error while parsing configuration:" + je.getMessage());
        }    
        
        
        try {
            mgm = probes.getJSONObject("mgm");
            data = probes.getJSONObject("data");
            
            mgmPackets = mgm.getInt("packets");
            mgmTimeout = mgm.getInt("timeout");
            mgmInterval = mgm.getInt("interval");
            
            dataPackets = data.getInt("packets");
            dataTimeout = data.getInt("timeout");
            dataInterval = data.getInt("interval");
            
        } catch (JSONException je) {
            System.out.println("Using default values for packets, timeout and rate parameters (mgm and data)" + je.getMessage());
            mgmPackets = 5;
            mgmTimeout = 1000;
            mgmInterval = 120;
            
            dataPackets = 5;
            dataTimeout = 1000;
            dataInterval = 30;
        }
        
        
        try {
            username = configuration.getJSONObject("ssh").getString("username");
            port = configuration.getJSONObject("ssh").getInt("port");

            controllerAddress = latticeConf.getJSONObject("controller").getString("address");
            controllerAPIPort = latticeConf.getJSONObject("controller").getInt("apiPort");
            controllerControlPort = latticeConf.getJSONObject("controller").getInt("controlPort");
            controllerInfoPort = latticeConf.getJSONObject("controller").getInt("infoPort");

            dcAddress = latticeConf.getJSONObject("dataConsumer").getString("address");
            dcPort = latticeConf.getJSONObject("dataConsumer").getInt("port");

            sourceProbeMgmAddress = probes.getJSONObject("source").getString("mgmAddress");
            sourceProbeMgmPort = probes.getJSONObject("source").getInt("mgmPort");
            sourceProbeDataAddress = probes.getJSONObject("source").getString("dataAddress");
            sourceProbeDataPort = probes.getJSONObject("source").getInt("dataPort");

            destProbeMgmAddress = probes.getJSONObject("destination").getString("mgmAddress");
            destProbeMgmPort = probes.getJSONObject("destination").getInt("mgmPort");
            destProbeDataAddress = probes.getJSONObject("destination").getString("dataAddress");
            destProbeDataPort = probes.getJSONObject("destination").getInt("dataPort");
        } catch (JSONException je) {
            throw new IOException("Error while parsing configuration:" + je.getMessage());
        }
    }
    
    
    public void deployComponents() throws JSONException {
        System.out.println("Deploying Lattice Components");
        String dcArgs = dcPort + "+" + controllerAddress + "+" + controllerInfoPort + "+" + controllerControlPort;
        String dsArgs = dcAddress + "+" + dcPort + "+" + controllerAddress + "+" + controllerInfoPort + "+" + controllerControlPort;
  
        uuids.put("dc", rest.startDataConsumer(dcAddress, "22", "lattice", dcArgs).getString("ID"));
        uuids.put("dssource", rest.startDataSource(sourceProbeMgmAddress, "22", "lattice", dsArgs).getString("ID"));
        uuids.put("dsdest", rest.startDataSource(destProbeMgmAddress, "22", "lattice", dsArgs).getString("ID"));
    }
    
    
    public void undeployComponents() throws JSONException {
        System.out.println("Undeploying Lattice Components");

        rest.stopDataSource(uuids.get("dssource"));
        rest.stopDataSource(uuids.get("dsdest"));
        rest.stopDataConsumer(uuids.get("dc"));
    }
    
    
    
    public void  loadProbes() throws JSONException {
        System.out.println("Loading Delay monitoring probes");
        String sourceProbeArgs = "DelaySource" + "+" + 
                                 sourceProbeMgmAddress + "+" + 
                                 sourceProbeMgmPort + "+" + 
                                 sourceProbeDataAddress + "+" + 
                                 sourceProbeDataPort + "+" + 
                                 destProbeDataAddress + "+" + 
                                 destProbeDataPort + "+" +
                                 mgmPackets + "+" +
                                 mgmTimeout + "+" +
                                 dataPackets + "+" +
                                 dataTimeout + "+" +
                                 dataInterval; 
        
        String destProbeArgs = "DelayDestination" + "+" + 
                               destProbeMgmAddress + "+" + 
                               destProbeMgmPort + "+" + 
                               destProbeDataAddress + "+" + 
                               destProbeDataPort + "+" + 
                               sourceProbeMgmAddress + "+" + 
                               sourceProbeMgmPort + "+" +
                               mgmPackets + "+" +
                               mgmTimeout + "+" +
                               mgmInterval + "+" +
                               dataPackets + "+" +
                               dataTimeout + "+" +
                               dataInterval; 

        uuids.put("sourceprobe", rest.loadProbe(uuids.get("dssource"), "eu.fivegex.monitoring.appl.probes.delay.unidirectional.DelaySourceProbe", sourceProbeArgs).getString("createdProbeID"));
        uuids.put("destprobe", rest.loadProbe(uuids.get("dsdest"), "eu.fivegex.monitoring.appl.probes.delay.unidirectional.DelayDestProbe", destProbeArgs).getString("createdProbeID"));
    }
    
    
    public void  unloadProbes() throws JSONException {
        System.out.println("Unloading monitoring probes");
        rest.unloadProbe(uuids.get("sourceprobe"));
        rest.unloadProbe(uuids.get("destprobe"));
    }
    
    
    public void loadReporters() throws JSONException {
        System.out.println("Loading reporters");
        uuids.put("loggerreporter", rest.loadReporter(uuids.get("dc"), "eu.fivegex.monitoring.appl.reporters.LoggerReporter", "logger-reporter").getString("createdReporterID"));
    }
    
    
    public void unloadReporters() throws JSONException {
        System.out.println("Unloading reporters");
        rest.unloadReporter(uuids.get("loggerreporter"));
    }
    
    
    public void activateProbes() throws JSONException {
        System.out.println("Activating monitoring probes");
        rest.turnOnProbe(uuids.get("destprobe"));
        rest.turnOnProbe(uuids.get("sourceprobe"));
    }
    
    
    
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: configuration file must be provided at startup");
            System.exit(1);
        }
        
        String filename = args[0];
        
        try {
            DelayProbe rttTest = new DelayProbe(filename);
            
            rttTest.deployComponents();
            rttTest.loadReporters();
            rttTest.loadProbes();
            rttTest.activateProbes();
            
            System.in.read();
            
            rttTest.unloadProbes();
            rttTest.unloadReporters();
            rttTest.undeployComponents();
        } catch (IOException ioe) {
            System.err.println("Error while initializing the environment: " + ioe.getMessage());
        } catch (JSONException je) {
            System.err.println("Error while performing the test: " + je.getMessage());
        }
    }  
    
}
