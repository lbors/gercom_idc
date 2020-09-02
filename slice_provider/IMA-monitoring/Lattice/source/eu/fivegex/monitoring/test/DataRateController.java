package eu.fivegex.monitoring.test;

import mon.lattice.core.Rational;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import us.monoid.json.JSONArray;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


/**
 * Makes REST calls to the Lattice Controller through the REST API using Resty
 **/
public class DataRateController implements Runnable {
    LatticeTest restClient = null;
    String dataConsumerID;
    String dataSourceID;
    Rational maxRate = new Rational("1000");
    int nProbes = 100;
    
    Boolean threadRunning;
    
    Map<String, Rational> probesMap;
    
    PrintWriter outFile = new PrintWriter("measurements");
    Long tStart = 0L;
    
    
    public DataRateController(Properties configuration) throws UnknownHostException, IOException {
        restClient = new LatticeTest(configuration);
        threadRunning = true;
        probesMap = new HashMap<>();
    }

    
    private void loadRandomProbe(String dsID, String probeName, String probeAttributeName, String value) throws JSONException {
        String probeClassName = "eu.fivegex.monitoring.appl.probes.RandomProbe";
        
        JSONObject out = restClient.loadProbe(dsID, probeClassName, probeName + "+" + probeAttributeName + "+" + value);
        //System.out.println(out);
        String probeID = out.getString("createdProbeID");
        
        restClient.turnOnProbe(probeID);
    }
    
    
    private JSONArray getProbesOnDS(String dsID) throws JSONException {
        JSONObject out = restClient.getDataSourceInfo(dsID);
        return out.getJSONArray("probes");
    }
    
    
    private void checkProbesRate() throws JSONException {
        JSONArray probes = getProbesOnDS(dataSourceID);
        String probeID;
        Rational probeRate;
        JSONObject out;
        for (int i=0; i < probes.length(); i++) {
            probeID = (String) probes.get(i);
            out = restClient.getProbeDataRate(probeID);
            System.out.println(out);
            probeRate = new Rational((String) out.get("rate"));
            probesMap.put(probeID, probeRate);
            System.out.println("Probe: " + probeID + " - rate: " + probeRate);
            outFile.println((System.currentTimeMillis() - tStart)/1000 + " Probe: " + probeID + " - rate: " + probeRate);
            }
    }
    
    
    private void adjustProbesRate() {
        Rational scalingFactor;
        Rational rateSum = new Rational(0, 1);
        
        for (Rational probeRate : probesMap.values())
            rateSum = rateSum.plus(probeRate);
        
        System.out.println(rateSum);
        
        scalingFactor = maxRate.multiply(new Rational(8, 10)).div(rateSum); // scaling to 0.8 of max rate
        System.out.println("scalingFactor: " + scalingFactor);
        
        Rational newRate;
        for (String probeID : probesMap.keySet()) {
            newRate = scalingFactor.multiply(probesMap.get(probeID));
            System.out.println("newRate: " + newRate);
            
            try {
                restClient.setProbeDataRate(probeID, newRate.toString());
            } catch (JSONException e) {
                System.err.println("Error changing rate on probe " + probeID + " " + e.getMessage());
              }
        }
            
            
        
    }
    
    @Override
    public void run() {
        Rational currentRate;
        JSONObject out;
        
        tStart = System.currentTimeMillis();
        while (threadRunning) {
            try {
                
                System.out.println("Checking probes rate");
                checkProbesRate();
                
                System.out.println("Checking DC rate");
                out = restClient.getDataConsumerMeasurementRate(dataConsumerID);
                currentRate = new Rational((String) out.get("rate"));
                System.out.println("Current DC rate: " + currentRate);
                outFile.println((System.currentTimeMillis() - tStart)/1000 +  " DC rate: " + currentRate);
                outFile.println((System.currentTimeMillis() - tStart)/1000 + " Nprobes: " + probesMap.size());
                
                if (currentRate.compareTo(maxRate) > 0) {
                    // should change rate on probes
                    System.out.println("Changing probes rate");
                    adjustProbesRate();
                }
                
            } catch (JSONException e) {
                System.err.println(e.getMessage());
              }
            finally {
                try {
                    Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        threadRunning=false;
                        
                      } 
            }
        }
    }
   
    
    
    public static void main(String[] args) {
        DataRateController rateController = null;
        String dsID = null;
        String dcID = null;
        String reporterID = null;
        
        boolean errorStatus = false;
        
        try {
            Properties configuration = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            switch (args.length) {
                case 0:
                    propertiesFile = System.getProperty("user.home") + "/rate.properties";
                    break;
                case 1:
                    propertiesFile = args[0];
                    break;
                default:
                    System.out.println("Please use: java DataRateController [file.properties]");
                    System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            configuration.load(input);
            
            rateController = new DataRateController(configuration);

            // instantiating a new DS on the endpoint as per configuration (field DSEndPointAddress)
            dsID = rateController.restClient.instantiateDS();
            
            dcID = rateController.restClient.instantiateDC();
            
            rateController.dataConsumerID = dcID;
            rateController.dataSourceID= dsID;
            
            Thread t = new Thread(rateController);
            t.start();
            
            for (Integer i=0; i < rateController.nProbes; i++) {
                rateController.loadRandomProbe(dsID, "RandomProbe" + i, "RandomAttribute", i.toString());
                Thread.sleep(2000);
            }
               
            System.in.read();
            rateController.outFile.flush();
            rateController.threadRunning = false;
            t.interrupt();
        }
        catch (Exception e) {
            System.out.println("*TEST FAILED*\n" + e.getMessage());
            errorStatus = true;
        }
        finally {
            // trying to stop the previous instantiated DS/DC anyway
            try {
                if (rateController.restClient != null) {
                    if (dsID != null)
                        rateController.restClient.unloadDS(dsID);
                    if (dcID != null)  {
                        if (reporterID != null) {
                            System.out.println("Unloading Reporter " + reporterID);
                            rateController.restClient.unloadReporter(reporterID);
                        }
                        rateController.restClient.unloadDC(dcID);
                    }
                }
            }
            catch (Exception e) { // the DS/DC was either already stopped or not running
            }
        }
    if (errorStatus)
        System.exit(1);
    }
}