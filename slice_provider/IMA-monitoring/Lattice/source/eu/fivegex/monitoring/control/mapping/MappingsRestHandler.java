/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.mapping;

import cc.clayman.console.BasicRequestHandler;
import mon.lattice.control.ControlInterface;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;

/**
 *
 * @author uceeftu
 */
public class MappingsRestHandler extends BasicRequestHandler {
    //ControlInterface<JSONObject, JSONException> controllerInterface;
    
    private DOServiceMapping mapping;
    
    private Logger LOGGER = LoggerFactory.getLogger(MappingsRestHandler.class);
    
    public MappingsRestHandler() {
    }
    
    @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        //controllerInterface = (ControlInterface<JSONObject, JSONException>) getManagementConsole().getAssociated();
        
        LOGGER.debug("-------- REQUEST RECEIVED --------\n" + request.getMethod() + " " +  request.getTarget());
        
        
        long time = System.currentTimeMillis();
        
        response.set("Content-Type", "application/json");
        response.set("Server", "LatticeController/1.0 (SimpleFramework 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        // get the path
        Path path = request.getPath();
        path.getDirectory();
        String name = path.getName();
        String[] segments = path.getSegments();

        // Get the method
        String method = request.getMethod();

        /*
        request.getQuery();
        
        System.out.println("method: " + request.getMethod());
        System.out.println("target: " + request.getTarget());
        System.out.println("path: " + request.getPath());
        System.out.println("directory: " + request.getPath().getDirectory());
        System.out.println("name: " + request.getPath().getName());
        System.out.println("segments: " + java.util.Arrays.asList(request.getPath().getSegments()));
        System.out.println("query: " + request.getQuery());
        System.out.println("keys: " + request.getQuery().keySet());
        */
        
        try {/*
            if (method.equals("PUT")) {
                if (name == null && segments.length == 2)
                    reporterOperation(request, response);   
                else
                    notFound(response, "PUT bad request");
            }
            
            else if (method.equals("DELETE")) {
                    if (name != null && segments.length == 2) {
                        // looks like a delete
                        deleteReporter(request, response);
                    } else
                        notFound(response, "DELETE bad request");  
            }
            
            else */ if (method.equals("POST")) {
                        if (name == null && segments.length == 1)
                            getServiceMappings(request, response);
                        else 
                            notFound(response, "POST bad request");  
            }
            
            else 
                badRequest(response, "Unknown method: " + method);
            
            return true;
            
            } catch (IOException ex) {
                LOGGER.error("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                LOGGER.error("JSONException" + jex.getMessage());
            } finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                LOGGER.error("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }
    





    private void getServiceMappings(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = new JSONObject();
        
        Query query = request.getQuery();
        
        String domain;
        int port;
        String slice;
        
        if (!request.getContentType().getSecondary().equals("json")) {
            badRequest(response, "wrong content-type");
            response.close();
            return;
        }
        
        if (query.containsKey("domain")) {
            domain = query.get("domain");
        } else {
            badRequest(response, "missing domain arg");
            response.close();
            return;
        }
        
        if (query.containsKey("port")) {
            Scanner scanner = new Scanner (query.get("port"));
            if (scanner.hasNextInt()) {
                port = scanner.nextInt();
            }
            else {
                LOGGER.error("Port is not valid");
                scanner.close();
                complain(response, "Port is not valid: " + query.get("port"));
                return;
            }

        } else {
            badRequest(response, "missing port arg");
            response.close();
            return;
        }
        
        if (query.containsKey("slice")) {
            slice = query.get("slice");

        } else {
            badRequest(response, "missing slice arg");
            response.close();
            return;
        }
        
        LOGGER.debug("Received request body:\n" + request.getContent());
        
        JSONObject DOMappingInfo;
        try {
            JSONObject mappings = new JSONObject(request.getContent());
            this.mapping = new DOServiceMapping(domain, port, slice, mappings);
            DOMappingInfo = mapping.getServiceMapping();
            jsobj.put("success", true);
            jsobj.put("domain", domain);
            jsobj.put("mapping", DOMappingInfo.getJSONObject("mapping"));
        } catch (IOException | JSONException e) {
            jsobj.put("success", false);
            jsobj.put("msg", e.getMessage());
          }
        
        LOGGER.debug(jsobj.toString(1));
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getServiceMappings: failure detected: " + failMessage);
            success = false;   
        }   
    
        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
    }
}
