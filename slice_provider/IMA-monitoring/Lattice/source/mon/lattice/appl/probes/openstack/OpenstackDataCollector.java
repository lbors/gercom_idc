/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.openstack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 *
 * @author uceeftu
 */


public class OpenstackDataCollector {    
    float cpuPercent;
    float memUsed;
    float memTotal;
    
    private final String username;
    private final String password;
    
    String ceilometerURI;
    String keystoneURI;
    
    String instanceUUID;
    
    Resty dataRequest;
    
    String authToken;
    
    
    public OpenstackDataCollector(String ceilometerAddress, int ceilometerPort, String keystoneAddress, int keystonePort, String username, String password, String instanceUUID) throws UnknownHostException {
        this.username = username;
        this.password = password;
        
        this.ceilometerURI = "http://" + InetAddress.getByName(ceilometerAddress).getHostName() + ":" + ceilometerPort;
        this.keystoneURI = "http://" + InetAddress.getByName(keystoneAddress).getHostName() + ":" + keystonePort;
        
        this.instanceUUID = instanceUUID;
        
        this.dataRequest = new Resty();
        dataRequest.withHeader("Content-Type", "application/json");
        
        this.authenticate();
    }    

    
    private String generateJSONRequest(String user, String password) {        
        JSONObject request = new JSONObject();
        try {
            request.put("auth", new JSONObject()
                        .put("identity", new JSONObject()
                                .put("methods", new JSONArray().put("password"))
                                .put("password", new JSONObject()
                                        .put("user", new JSONObject()
                                                .put("domain", new JSONObject()
                                                        .put("name", "default"))
                                                .put("name", user)
                                                .put("password", password))))
                        .put("scope", new JSONObject()
                                .put("project", new JSONObject()
                                        .put("domain", new JSONObject()
                                                .put("name", "default"))
                                        .put("name", "dev")))
                        );
        }
        catch (JSONException jsEx) {
            System.out.println("Error while generating token request: " + jsEx.getMessage());
        }
        return request.toString();
    }
    
    private void authenticate() {
        Resty auth = new Resty();
        try {
            String authURI =  this.keystoneURI + "/v3/auth/tokens";
            
            this.authToken = auth.json(authURI, new JSONFormContent(this.generateJSONRequest(this.username, this.password))).http().getHeaderField("X-Subject-Token");
            dataRequest.withHeader("X-Auth-Token", this.authToken);
            
        } catch (IOException ioE) {
            System.out.println("Error while authenticating: " + ioE.getMessage());
        }
    }
    
    
    void collectValues() {        
        try {
            String requestCpuURI = ceilometerURI + "/v2/meters/cpu_util?q.field=resource_id&q.value=" + instanceUUID + "&limit=1";
            String requestMemURI = ceilometerURI + "/v2/meters/memory.usage?q.field=resource_id&q.value=" + instanceUUID + "&limit=1";
            
            JSONObject jsobj = dataRequest.json(requestCpuURI).array().getJSONObject(0);
            this.cpuPercent = (float)jsobj.getDouble("counter_volume");
            
            jsobj = dataRequest.json(requestMemURI).array().getJSONObject(0);
            this.memUsed = (float)jsobj.getDouble("counter_volume");
            this.memTotal = (float)jsobj.getJSONObject("resource_metadata").getDouble("memory_mb");
            
            //System.out.println(cpuPercent);
            //System.out.println(memUsed);
            //System.out.println(memTotal);
            
        } catch (IOException ioEx) {
            System.out.println("Error while collecting measurements: " + ioEx.getMessage());
            // this is likely due to an expired auth token, re-authenticating
            this.authenticate();
        }
          catch (JSONException jsonEx) {
            System.out.println("Error while collecting measurements: " + jsonEx.getMessage());
          }  
    } 
}
