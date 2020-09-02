package eu.fivegex.monitoring.test;

import mon.lattice.control.ControlInterface;
import mon.lattice.core.ID;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Properties;
import mon.lattice.control.deployment.DeploymentInterface;
import us.monoid.json.JSONArray;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.delete;
import static us.monoid.web.Resty.put;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbesCatalogue;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.form;

/**
 * Makes REST calls to the Lattice Controller through the REST API using Resty
 **/
public class LatticeControllerClient implements ControlInterface<JSONObject>, DeploymentInterface<JSONObject>, JSONProbesCatalogue {
    // A URI for a Lattice Controller to interact with
    String vimURI;
    Resty rest;
    int port;
    
    //general attributes
    String DSEndPointAddress;
    String DSEndPointName;
    String DSEndPointPort;
    String DSEndPointUserName;
    
    String DCEndPointAddress;
    String DCEndPointName;
    String DCEndPointPort;
    String DCEndPointUserName;

    String DCDataPlaneAddress;
    String DCDataPlanePort;
    
    String controllerInfoPlaneAddress;
    String controllerInfoPlanePort;
    
    String DSInfoPlanePort;
    String DSControlPlanePort;
    
    String DCInfoPlanePort;
    String DCControlPlanePort;
    
    //docker related attributes
    String dockerHost;
    String dockerPort;
    String dockerContainerID;
    String dockerContainerName;
    
    String mongoAddress;
    String mongoPort;
    String mongoDBName;
    String mongoCollection;
    
    
    
    /**
     * Construct a LatticeTest
     * using defaults of localhost and port 6666
     */
    public LatticeControllerClient(Properties configuration) throws UnknownHostException, IOException {
        this(configuration.getProperty("controller.infoplane.address"), Integer.valueOf(configuration.getProperty("controller.rest.port")));
        
        controllerInfoPlaneAddress = configuration.getProperty("controller.infoplane.address");
        controllerInfoPlanePort = configuration.getProperty("controller.infoplane.port");
        
        DSEndPointAddress = configuration.getProperty("ds.endpoint.address");
        DSEndPointName = configuration.getProperty("ds.endpoint.name");
        DSEndPointUserName = configuration.getProperty("ds.endpoint.user");
        DSInfoPlanePort = configuration.getProperty("ds.infoplane.port");
        DSControlPlanePort = configuration.getProperty("ds.controlplane.port");
        
        DCEndPointAddress = configuration.getProperty("dc.endpoint.address");
        DCEndPointName = configuration.getProperty("dc.endpoint.name");
        DCEndPointUserName = configuration.getProperty("dc.endpoint.user");

        DCDataPlaneAddress = configuration.getProperty("dc.dataplane.address");
        DCDataPlanePort = configuration.getProperty("dc.dataplane.port");
        
        DCInfoPlanePort = configuration.getProperty("dc.infoplane.port");
        DCControlPlanePort = configuration.getProperty("dc.controlplane.port");
        
        dockerHost = configuration.getProperty("dockerHost");
        dockerPort = configuration.getProperty("dockerPort");
        dockerContainerID = configuration.getProperty("dockerContainerID");
        dockerContainerName = configuration.getProperty("dockerContainerName");   
        
        mongoAddress = configuration.getProperty("mongodb.address");
        mongoPort = "27017";
        mongoDBName = "test";
        mongoCollection = "cs";
        
        DSEndPointPort = "22";
        DCEndPointPort= "22";
    }

 
    public LatticeControllerClient(String addr, int port) throws UnknownHostException, IOException  {
        initialize(InetAddress.getByName(addr), port);
    }


    public LatticeControllerClient(InetAddress addr, int port) throws UnknownHostException, IOException  {
        initialize(addr, port);
    }

    /**
     * Initialize
     */
    private synchronized void initialize(InetAddress addr, int port) {
        this.port = port;
        vimURI = "http://" + addr.getHostName() + ":" + Integer.toString(port);
        rest = new Resty();
    }

    /**
     * Get the port this VimClient is connecting to
     */
    public int getPort() {
        return port;
    }

    
    //curl -X POST http://localhost:6666/datasource/?endpoint=<endpoint>\&username=<username>\&args=arg1+arg2+argN
    @Override
    public JSONObject startDataSource(String endPoint, String port, String userName, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/?endpoint=" + endPoint + "&port=" + port + "&username=" + userName + "&args=" + args;
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deployDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X DELETE http://localhost:6666/datasource/?endpoint=<endpoint>\&username=<username>
    @Override
    public JSONObject stopDataSource(String dataSourceID) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + dataSourceID;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    @Override
    public JSONObject getDataSourceInfo(String dsID) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    //curl -X POST http://localhost:6666/datasource/<dsUUID>/probe/?className=<probeClassName>\&args=<arg1>+<arg2>+<argN>
    @Override
    public JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + id + "/probe/?className=" + probeClassName + "&args=" + java.net.URLEncoder.encode(probeArgs, "UTF-8");
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;

        } catch (IOException ioe) {
            throw new JSONException("loadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X DELETE http://localhost:6666/probe/<probeUUID>
    @Override
    public JSONObject unloadProbe(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("unloadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=off
    @Override
    public JSONObject turnOffProbe(String id) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + id + "/?status=off";
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnOffProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
     //curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=on
    @Override
    public JSONObject turnOnProbe(String id) throws JSONException {
         try {
            String uri = vimURI + "/probe/" + id + "/?status=on";
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnProbeOn FAILED" + " IOException: " + ioe.getMessage());
            }
    }
    
    
    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?serviceid=<serviceUUID>
    @Override
    public JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?serviceid=" + serviceID;
            
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeServiceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X PUT http://localhost:6666/probe/<probeUUID>/?sliceid=<sliceUUID>
    @Override
    public JSONObject setProbeGroupID(String probeID, String groupID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?sliceid=" + groupID;
            
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeSliceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    

    @Override
    public JSONObject setProbeDataRate(String probeID, String dataRate) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject startDataConsumer(String endPoint, String port, String userName, String args) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/?endpoint=" + endPoint + "&port=" + port +  "&username=" + userName + "&args=" + args;
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deployDC FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    
    @Override
    public JSONObject stopDataConsumer(String dataConsumerID) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/" + dataConsumerID;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopDC FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    @Override
    public JSONObject getDataConsumerMeasurementRate(String dcID) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException {
        try {
            String uri = vimURI + "/dataconsumer/" + id + "/reporter/?className=" + reporterClassName + "&args=" + java.net.URLEncoder.encode(reporterArgs, "UTF-8");
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;

        } catch (IOException ioe) {
            throw new JSONException("loadReporter FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject unloadReporter(String id) throws JSONException {
        try {
            String uri = vimURI + "/reporter/" + id;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("unloadReporter FAILED" + " IOException: " + ioe.getMessage());
        }
    }

    @Override
    public JSONObject getDataSources() throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getDataConsumers() throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // curl -X GET http://localhost:6666/probe/catalogue/
    @Override
    public JSONObject getProbesCatalogue() throws JSONException {
        try {
            String uri = vimURI + "/probe/catalogue/";
            
            JSONObject jsobj = rest.json(uri).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getProbesCatalogue FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    private void testMemoryInfoProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception {
        String probeClassName = "eu.fivegex.monitoring.appl.probes.MemoryInfoProbe";
        JSONObject out = new JSONObject();
        
        try {
            System.out.println("Creating probe on endpoint: " + DSEndPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbe(dsID, probeClassName, probeName);
            
            String probeID = out.getString("createdProbeID");

            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = setProbeServiceID(probeID, serviceID);

            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = setProbeGroupID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            out = turnOnProbe(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + DSEndPointName + " - DS id: " + dsID);
            out = turnOffProbe(probeID);
        }
        catch (InterruptedException ex) {
            return;
        } 
        catch (JSONException ex) {
            throw new Exception("Test Case MemoryInfoProbe Failed! " + "\nReason: " + out.getString("msg"));
        }  
    }
    
    private void testDockerProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception { 
        String probeClassName = "eu.fivegex.monitoring.appl.probes.docker.DockerProbe";
        JSONObject out;
        
        try {
            System.out.println("Creating probe on endpoint: " + DSEndPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbe(dsID, probeClassName, dockerHost + "+" +
                                     dockerPort + "+" +
                                     probeName + "+" +
                                     dockerContainerID + "+" +
                                     dockerContainerName
                                    );
            
            String probeID = out.getString("createdProbeID");
            
            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            setProbeServiceID(probeID, serviceID);
            
            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            setProbeGroupID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + DSEndPointName  + " - DS id: " + dsID);
            turnOnProbe(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + DSEndPointName + " - DS id: " + dsID);
            turnOffProbe(probeID);   
        }
        catch (InterruptedException ex) {
            return;
        }
        catch (JSONException ex) {
            throw new Exception("Test Case DockerProbe Failed! " + "\nReason: " + ex.getMessage());
        }
    }
    
    
    private String instantiateDS() throws Exception { // we should create a Lattice test exception 
        JSONObject out = new JSONObject();
        
        System.out.println("Deploying DS on endpoint: " + DSEndPointName);
        
        try {
            out = startDataSource(DSEndPointAddress, DSEndPointPort, DSEndPointUserName, 
                                                                                 DCDataPlaneAddress + "+" + 
                                                                                 DCDataPlanePort + "+" +
                                                                                 controllerInfoPlaneAddress + "+" +
                                                                                 controllerInfoPlanePort + "+" +
                                                                                 DSInfoPlanePort + "+" +
                                                                                 DSControlPlanePort
                         );
            
            return out.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DS\n" + out.getString("msg"));
        }
    }
    
    
    private void unloadDS(String dsID) throws Exception {
        JSONObject out;
        System.out.println("Stopping DS on endpoint: "  + DSEndPointAddress + " - DS id: " + dsID);
        try {
            out = stopDataSource(dsID);  

            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping DS: " + dsID + out.getString("msg")); 
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading DS: " + e.getMessage());
        }
    }
    
    
    private String instantiateDC() throws Exception { // we should create a Lattice test exception 
        JSONObject out = new JSONObject();
        
        System.out.println("Deploying DC on endpoint: " + DCEndPointName);
        
        try {
            out = startDataConsumer(DCEndPointAddress, DCEndPointPort, DCEndPointUserName, DCDataPlanePort + "+" +
                                                                 controllerInfoPlaneAddress + "+" +
                                                                 controllerInfoPlanePort + "+" +
                                                                 DCInfoPlanePort + "+" +
                                                                 DCControlPlanePort
                         );
            
            return out.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DC\n" + out.getString("msg"));
        }
    }
    
    
    private void unloadDC(String dcID) throws Exception {
        JSONObject out;
        System.out.println("Stopping DC on endpoint: "  + DCEndPointAddress + " - DC id: " + dcID);
        try {
            out = stopDataConsumer(dcID);  

            if (!out.getBoolean("success"))
                throw new Exception("Error while stopping DC: " + dcID + out.getString("msg")); 
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading DC: " + e.getMessage());
        }
    }
    
    
    private String loadMongoDBReporter(String dcID) throws Exception {
        String reporterClassName = "eu.fivegex.monitoring.appl.reporters.MongoDBReporter";
        
        JSONObject out;
        
        try {
            System.out.println("Starting reporter on endpoint: " + DCEndPointName + " - DC id: " + dcID);
            System.out.println("Dinamically loading reporter class: " + reporterClassName);
            
            out = loadReporter(dcID, reporterClassName, mongoAddress + "+" +
                                     mongoPort + "+" +
                                     mongoDBName + "+" +
                                     mongoCollection
                                    );
            
            String reporterID = out.getString("createdReporterID");  
            return reporterID;
        }

        catch (JSONException ex) {
            throw new Exception("Test Case loadMongoDBReporter Failed! " + "\nReason: " + ex.getMessage());
        }
        
        
    }
    
    
    private MongoDBInteracter createMongoDBEntry(String serviceID, String probeName) throws JSONException, ParseException, IOException {
        
        JSONObject obj = new JSONObject();
        obj.put("agreementId", serviceID);
        obj.put("name", "Lattice Test");
        obj.put("maxResult", 10);
        obj.put("kpiList", new JSONArray().put(probeName));
        
        MongoDBInteracter mongo = new MongoDBInteracter(mongoAddress, Integer.valueOf(mongoPort), mongoCollection);
        mongo.createMongoDBEntry(obj);
        return mongo; //just a bad thing
    }
    
    
    
    
    public static void main(String[] args) {
        LatticeControllerClient client = null;
        String dsID = null;
        String dcID = null;
        String reporterID = null;
        
        boolean errorStatus = false;
        
        try {
            Properties configuration = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            if (args.length == 0)
                propertiesFile = System.getProperty("user.home") + "/latticeTest.properties";
            else if (args.length == 1)
                propertiesFile = args[0];
            else {
                System.out.println("Please use: java LatticeTest [file.properties]");
                System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            configuration.load(input);
            
            client = new LatticeControllerClient(configuration);

            // instantiating a new DS on the endpoint as per configuration (field DSEndPointAddress)
            dsID = client.instantiateDS();
            
            dcID = client.instantiateDC();
            
            
            //reporterID = client.loadMongoDBReporter(dcID);
            
            // generating service/slice IDs to be associated to all the test probes
            String serviceID = ID.generate().toString();
            String sliceID = ID.generate().toString();
            String probeName = "testMemoryProbe";
            
            // creating entry in DB
            //MongoDBInteracter m = client.createMongoDBEntry(serviceID, probeName);
            
            // instantiating some test probes on the previous DS
            client.testMemoryInfoProbe(probeName, dsID, serviceID, sliceID);
            //client.testDockerProbe("testDockerProbe", dsID, serviceID, sliceID);

            //Document mongoDBEntry = m.getMongoDBEntry(serviceID, probeName); // TODO: incorrect this should also check id the ProbeName element is not empty
            System.out.println("Reading data (1 measurement) from the DB related to the previous service " + serviceID + " and " + probeName);
            //if (mongoDBEntry != null)
            //    System.out.println(mongoDBEntry.toJson(new JsonWriterSettings(true)));
            //else
            //    throw new Exception("Cannot find any entries with service ID " + serviceID + " in the DB");
           
        }
        catch (Exception e) {
            System.out.println("\n************************************************** TEST FAILED **************************************************\n" + 
                               e.getMessage() + 
                               "\n*****************************************************************************************************************\n");
            errorStatus = true;
        }
        finally {
            // trying to stop the previous instantiated DS/DC anyway
            try {
                if (client != null) {
                    if (dsID != null)
                        client.unloadDS(dsID);
                    if (dcID != null)  {
                        if (reporterID != null) {
                            System.out.println("Unloading Reporter " + reporterID);
                            client.unloadReporter(reporterID);
                        }
                        client.unloadDC(dcID);
                    }
                }
            }
            catch (Exception e) { // the DS/DC was either already stopped or not running
            }
        }
    if (errorStatus)
        System.exit(1);
    }

    @Override
    public JSONObject getProbeDataRate(String probeID) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getProbeServiceID(String probeID) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initDeployment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initCatalogue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject startControllerAgent(String endPoint, String port, String userName, String className, String args) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject stopControllerAgent(String mmID) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
