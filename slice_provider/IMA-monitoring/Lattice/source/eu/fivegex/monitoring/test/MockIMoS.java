/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.content;

/**
 *
 * @author uceeftu
 */
public class MockIMoS {
    String address;
    int port;
    
    String MdOURI;
    String latticeControllerAddress = "localhost";
    int latticeControllerPort = 6666;
     
    String latticeControllerURI;
    Resty resty;
    
    String serviceID;
    
    private JSONObject MdOMapping;
    private Map<String, JSONObject> DOMappingRequests = new HashMap<>();
    private Map<String, JSONObject> DOMappingInfo = new HashMap<>();
    
    private JSONObject fakeDomainsInfo = new JSONObject();
    
    private LatticeControllerClient lattice;
    

    public MockIMoS(String address, int port, String serviceID) {
        this.address = address;
        this.port = port;
        MdOURI = "http://" + address + ":" + Integer.toString(port);
        
        this.serviceID = serviceID;
        
        // we assume only one controller instance exists
        latticeControllerURI = "http://" + latticeControllerAddress + ":" + Integer.toString(latticeControllerPort);
        try {
            lattice = new LatticeControllerClient(latticeControllerAddress, latticeControllerPort);
        } catch (IOException e) {
            System.out.println("Error while instantiating Lattice controller client: " + e.getMessage());
        }
        resty = new Resty();
        fillInFakeDomainsInfo();
    }
    
    private void fillInFakeLocalhostDomainsInfo() {
        JSONObject domain1 = new JSONObject();
        JSONObject domain2 = new JSONObject();
        
        try {
            domain1.put("url", "localhost:8888");
            domain1.put("prefix", "ro/v11");
            domain1.put("user", "lattice");
            domain1.put("ssh", "9025");
            fakeDomainsInfo.put("DOCKER-1", domain1);
            
            domain2.put("url", "localhost:8887");
            domain2.put("prefix", "ro/v11");
            domain2.put("user", "lattice");
            domain2.put("ssh", "9026");
            fakeDomainsInfo.put("DOCKER-2", domain2);
        } catch (JSONException e) {
            System.out.println("Error while filling in domain information: " + e.getMessage());
        }
    }
    
    
    private void fillInFakeDomainsInfo() {
        JSONObject domain1 = new JSONObject();
        JSONObject domain2 = new JSONObject();
        
        try {
            domain1.put("url", "docker:8888");
            domain1.put("prefix", "ro/v11");
            domain1.put("user", "lattice");
            domain1.put("ssh", "22");
            fakeDomainsInfo.put("DOCKER-1", domain1);
            
            domain2.put("url", "docker2:8888"); // does not exist yet
            domain2.put("prefix", "ro/v11");
            domain2.put("user", "lattice");
            domain2.put("ssh", "22");
            fakeDomainsInfo.put("DOCKER-2", domain2);
        } catch (JSONException e) {
            System.out.println("Error while filling in domain information: " + e.getMessage());
        }
    }
    
    public void getServiceMappingInfo() {
        try {
            String uri = MdOURI + "/escape/mapping-info/" + serviceID;
            
            JSONObject jsobj = resty.json(uri).toObject();

            MdOMapping = jsobj;
        } catch (IOException | JSONException e) {
            System.out.println("Error while performing getServiceMappingInfo: " + e.getMessage());
        }
    }
    
    // just a dummy implementation of resource type to probe resolution
    public String getProbeType(String domain) {
        switch (domain) {
            case "docker": 
                return "eu.fivegex.monitoring.appl.probes.docker.DockerProbe";
            case "openstack":
                return "eu.fivegex.monitoring.appl.probes.openstack.OpenstackProbe";
            default:
                return null;
        }
    }
    
    
    public void parseServiceMappingInfo() {
        JSONArray mappings;
        try {
            mappings = MdOMapping.getJSONArray("mapping");
            for (int i=0; i<mappings.length(); i++) {
                JSONObject inputMappingElement;
                JSONObject mappedEntry;
                String domain;
                String bisbisID;
                String nfID;
                
                inputMappingElement = mappings.getJSONObject(i);
                
                domain = inputMappingElement.getJSONObject("bisbis").getString("domain");
                bisbisID = inputMappingElement.getJSONObject("bisbis").getString("id");
                nfID = inputMappingElement.getJSONObject("nf").getString("id");
                
                mappedEntry = DOMappingRequests.get(domain);
                if (mappedEntry == null) {
                    mappedEntry = new JSONObject();
                    DOMappingRequests.put(domain, mappedEntry);
                }
                
                mappedEntry.append(bisbisID, nfID);
            }
        } catch (JSONException | NullPointerException e) {
            System.out.println("Error while parsing MdO mapping info: " + e.getMessage());
        }
    }
    
    
    public void printMappings() {
        try {
            for (String domain : DOMappingRequests.keySet()) {
                System.out.println("domain:" + domain);
                JSONObject domainMappings = DOMappingRequests.get(domain);
                System.out.println(domainMappings.toString(1));
            }
        } catch (JSONException e) {
            System.out.println("Error while printing Mappings" + e.getMessage());
        }
    }
    
    public void printDOMappings() {
        try {
            for (String domain : DOMappingInfo.keySet()) {
                System.out.println(domain);
                JSONObject domainMappings = DOMappingInfo.get(domain);
                System.out.println(domainMappings.toString(1));
            }
        } catch (JSONException e) {
            System.out.println("Error while printing DOs Mappings: " + e.getMessage());
        }
    }
    
    private JSONObject resolveDomainInfo(String domain) throws JSONException {
        return this.fakeDomainsInfo.getJSONObject(domain);
    }
    
    
    public JSONObject sendResourceMappingRequest(String DOurl, String slicePrefix, JSONObject requestBody) {
        String DOAddress;
        int DOPort;
        JSONObject jsobj=new JSONObject();
        
        DOAddress = DOurl.split(":")[0];
        DOPort = Integer.valueOf(DOurl.split(":")[1]);
        
        try {
            String uri = latticeControllerURI + "/mappings/?domain=" + DOAddress + "&port=" + DOPort + "&slice=" + slicePrefix;
            jsobj = resty.json(uri, content(requestBody)).toObject();
        } catch (JSONException | IOException e) {
            System.out.println("Error while sending resource mapping request to Controller: " + e.getMessage());
        }
        
        return jsobj;
    }
    
    
    private void instantiateProbes(JSONObject NFInstance, String dsId, String localMonitoringAddress) throws JSONException {
        String probeClassName = this.getProbeType(NFInstance.getString("type"));
        String localMonitoringPort = "4243"; // should be provided as capability from the DO
        String NFId = NFInstance.getString("id");
        String localId = NFInstance.getString("internalid");
        
        System.out.println("NF ID: " + NFId);
        System.out.println("Container ID: " + localId);
        
        JSONObject probeInfo = lattice.loadProbe(dsId, 
                                                 probeClassName, 
                                                 localMonitoringAddress + "+" + localMonitoringPort + "+" + NFId + "+" + localId + "+" + NFId
                                                );
        
        String probeId = probeInfo.getString("createdProbeID");
        System.out.println("Created probe ID: " + probeId);
        
        lattice.turnOnProbe(probeId);
        lattice.setProbeServiceID(probeId, serviceID);
    }
    
    
    
    public JSONObject instantiateMonitoringElements() {
        try {
            // start a DC where the MdO is running
            JSONObject dataConsumerInfo = lattice.startDataConsumer("mininet-vm", "22", "lattice", "22998+lattice-controller+6699+9999+5555");
            String dataConsumerID = dataConsumerInfo.getString("ID");
            
            // load a Logger Reporter
            lattice.loadReporter(dataConsumerID, "eu.fivegex.monitoring.appl.reporters.LoggerReporter", "logger-reporter");
            lattice.loadReporter(dataConsumerID, "eu.fivegex.monitoring.appl.reporters.InfluxDBReporter", "localhost+8086+fgx");
            
            // should check created reporter ID
            
            //String controllerAddress = InetAddress.getLocalHost().getHostAddress();
            
            for (String domain : DOMappingInfo.keySet()) { // should run in parallel
                /* start a DS on the Domains (should check if it already exists)
                   uses the same machine where the DO is running
                   assumes pkey authentication is setup from
                   the controller to DO machine
                */
                
                // getting Domain information
                String url = fakeDomainsInfo.getJSONObject(domain).getString("url");
                String DSHostAddress = url.split(":")[0];
                String DSSSHHostPort = fakeDomainsInfo.getJSONObject(domain).getString("ssh");
                String username = fakeDomainsInfo.getJSONObject(domain).getString("user");
                
                JSONObject dataSourceInfo = lattice.startDataSource(DSHostAddress, DSSSHHostPort, username, "mininet-vm+22998+lattice-controller+6699+9999+5555");
                String dataSourceId = dataSourceInfo.getString("ID");
                
                JSONArray mappings = DOMappingInfo.get(domain).getJSONObject("mapping").getJSONArray("instances");
                
                for (int i=0; i<mappings.length(); i++) {
                    // we are assuming all the probes will be loaded on the only
                    // single DS running in the Domain (i.e., on DSHostAddress)
                    
                    instantiateProbes(((JSONObject)mappings.get(i)), dataSourceId, DSHostAddress); 
                }
            }
        } catch (JSONException e) {
            System.out.println("Error while instantiating monitoring elements: " + e.getMessage());
        }
        return null;
    }
    
    
    public void generateResourceMappingRequests() {
        JSONObject domainInfo;
        String url;
        String prefix;
        
        try {
            for (String domain : DOMappingRequests.keySet()) {
                domainInfo = resolveDomainInfo(domain);
                url = domainInfo.getString("url");
                prefix = domainInfo.getString("prefix");
                DOMappingInfo.put(domain, sendResourceMappingRequest(url, prefix, DOMappingRequests.get(domain)));
            }
        } catch (JSONException e) {
            System.out.println("Error while sending resource mapping requests: " + e.getMessage());
        }
            
    }
    
    
    public static void main(String [] args) {
        MockIMoS imos;
        
        if (args.length != 3)
           imos = new MockIMoS("localhost", 8889, "3fa834b4-f781-11e6-8cf3-fa163ef7e9db");
        else {
            String resOrchHost = args[0];
            Scanner sc = new Scanner(args[1]);
            int resOrchPort = sc.nextInt();
            String serviceID = args[2];
            imos = new MockIMoS(resOrchHost, resOrchPort, serviceID);
        }
        
        imos.getServiceMappingInfo();
        imos.parseServiceMappingInfo();
        imos.printMappings();
        imos.generateResourceMappingRequests();
        imos.printDOMappings();
        
        imos.instantiateMonitoringElements();
    }
    
    
    
}
