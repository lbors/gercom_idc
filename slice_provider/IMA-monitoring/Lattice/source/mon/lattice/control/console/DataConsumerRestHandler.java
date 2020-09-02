/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import cc.clayman.console.BasicRequestHandler;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import mon.lattice.control.controller.json.AbstractJSONRestController;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
class DataConsumerRestHandler extends BasicRequestHandler {
    
    AbstractJSONRestController controllerInstance;
    
    private Logger LOGGER = LoggerFactory.getLogger(DataConsumerRestHandler.class);

    public DataConsumerRestHandler() {
    }

    
    @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        controllerInstance = (AbstractJSONRestController) getManagementConsole().getAssociated();
        
        
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
        
        try {
            switch (method) {
                case "POST":
                    if (name == null && segments.length == 3)
                        loadReporter(request, response);
                    else if (name == null && segments.length == 1) {
                        deployDC(request,response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name != null && segments.length == 2) {
                        stopDC(request,response);
                    }
                    break;    
                case "GET":
                    if (name == null && segments.length == 1)
                        getDataConsumers(request, response);
                    else
                        if (name == null && segments.length == 3) {
                            getDCMeasurementRate(request, response);
                        }
                        else
                            notFound(response, "GET bad request");
                    break;
                default:
                    badRequest(response, "Unknown method" + method);
                    return false;
            }
            
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
   
   
    
    private void deployDC(Request request, Response response) throws JSONException, IOException {
        Query query = request.getQuery();
        
        String endPoint;
        String port;
        String userName;
        String rawArgs="";
        
        if (query.containsKey("endpoint"))
            endPoint = query.get("endpoint");
        else {
            badRequest(response, "missing endpoint arg");
            response.close();
            return;
        }
        
        if (query.containsKey("port"))
            port = query.get("port");
        else {
            badRequest(response, "missing port arg");
            response.close();
            return;
        }
        
        if (query.containsKey("username"))
            userName = query.get("username");
        else {
            badRequest(response, "missing username args");
            response.close();
            return;
        }
        
        if (query.containsKey("args")) {
            rawArgs = query.get("args");
            rawArgs = rawArgs.trim();
            rawArgs = rawArgs.replaceAll("\\+", " ");
        }
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInstance.startDataConsumer(endPoint, port, userName, rawArgs);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("startDC: failure detected: " + failMessage);
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
    
    
    private void stopDC(Request request, Response response) throws JSONException, IOException {
        Path path = request.getPath();
        
        /*
        String endPoint;
        String port;
        String userName;
        
        if (query.containsKey("endpoint"))
            endPoint = query.get("endpoint");
        else {
            badRequest(response, "missing endpoint arg");
            response.close();
            return;
        }
        
        if (query.containsKey("port"))
            port = query.get("port");
        else {
            badRequest(response, "missing port arg");
            response.close();
            return;
        }
        
        if (query.containsKey("username"))
            userName = query.get("username");
        else {
            badRequest(response, "missing username args");
            response.close();
            return;
        }*/
        
        String dcID;
        
        Scanner scanner = new Scanner(path.getName());
        
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dcID = scanner.next();
            scanner.close();
        }
        else {
            LOGGER.error("dcID is not valid");
            scanner.close();
            complain(response, "data consumer ID is not a valid UUID: " + path.getName());
            return;
        }
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInstance.stopDataConsumer(dcID);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("stopDC: failure detected: " + failMessage);
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
    
    
    private void loadReporter(Request request, Response response) throws JSONException, IOException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String dcID;
        String className;
        //String rawArgs="";
        String rawArgs=null;
        
        if (query.containsKey("className")) {
            className = query.get("className");
        } else {
            badRequest(response, "missing arg className");
            response.close();
            return;
        }
        
        if (query.containsKey("args")) {
            rawArgs = query.get("args");
            rawArgs = rawArgs.trim();
            rawArgs = rawArgs.replaceAll("\\+", " ");
        }

        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}") && segments[2].equals("reporter")) {
            dcID = segments[1];
            jsobj = controllerInstance.loadReporter(dcID, className, rawArgs);
        }
        
        else {
            badRequest(response, "wrong path");
            response.close();
            return;
        }
        
        if (!jsobj.getBoolean("success")) {
            failMessage =(String)jsobj.get("msg");
            LOGGER.error("loadReporter failure: " + failMessage);
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
    
    
    private void getDCMeasurementRate(Request request, Response response) throws JSONException, IOException {
        Scanner scanner;
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        String dcID;
        
        scanner = new Scanner (segments[1]);
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dcID = scanner.next();
            scanner.close();
        }
        else {
            LOGGER.error("Data Consumer ID is not a valid UUID");
            scanner.close();
            complain(response, "Data Consumer ID is not a valid UUID: " + segments[1]);
            return;
        }
        
        if (!segments[2].equals("rate")) {
            badRequest(response, segments[2] + "is not a valid path");
            response.close();
            return;
        }
        
        jsobj = controllerInstance.getDataConsumerMeasurementRate(dcID);

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getDataConsumerMeasurementRate: failure detected: " + failMessage);
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
    
    private void getDataConsumers(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInstance.getDataConsumers();

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getDataConsumers: failure detected: " + failMessage);
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