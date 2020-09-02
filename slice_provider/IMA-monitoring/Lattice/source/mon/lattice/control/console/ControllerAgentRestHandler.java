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
import mon.lattice.control.deployment.DeploymentInterface;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.control.agents.ControllerAgentInterface;

/**
 *
 * @author uceeftu
 */
class ControllerAgentRestHandler extends BasicRequestHandler {
    ControllerAgentInterface<JSONObject> controllerInstance;
    DeploymentInterface<JSONObject> deploymentControllerInstance;
    private Logger LOGGER = LoggerFactory.getLogger(ControllerAgentRestHandler.class);
    
    public ControllerAgentRestHandler() {
    }
    
    
     @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        controllerInstance = (ControllerAgentInterface<JSONObject>) getManagementConsole().getAssociated();
        deploymentControllerInstance = (DeploymentInterface<JSONObject>) getManagementConsole().getAssociated();
        
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
                        setOperation(request, response);
                    else if (name == null && segments.length == 1) {
                        deploy(request,response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name != null && segments.length == 2) {
                        stop(request,response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "GET":
                    if (name == null && segments.length == 1)
                        getControllerAgents(request, response);
                    else
                        if (segments.length == 2 && name != null)
                            getControllerAgentInfo(request, response);
                        else
                            notFound(response, "GET bad request");
                    break;   
                default:
                    badRequest(response, "Unknown method" + method);
                    return false;
            }
            
            
            return true;
            
            } catch (Exception ex) {
                LOGGER.error("Exception: " + ex.getMessage());
            }
             finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                LOGGER.error("IOException: " + ex.getMessage());
                              }
                      }
     return false;
    }
    
    
    private void setOperation(Request request, Response response) throws Exception {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String agentID;
        String operationType;
        String monAddress;
        String monPort;

        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            agentID = segments[1];
            
            operationType = segments[2];
            if (operationType.contentEquals("reporter"))
                if (query.containsKey("address") && query.containsKey("port")){
                    monAddress = query.get("address");
                    monPort = query.get("port");
                    jsobj = controllerInstance.setMonitoringReportingEndpoint(agentID, monAddress, monPort);
                } 
                
            else {
                badRequest(response, "missing either address or port arg");
                response.close();
                return;
            }
        }
        
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("set report: failure detected: " + failMessage);
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
    
    private void deploy(Request request, Response response) throws Exception {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String endPoint;
        String port;
        String userName;
        String className;
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
            badRequest(response, "missing username arg");
            response.close();
            return;
        }
        
        if (query.containsKey("className"))
            className = query.get("className");
        else {
            badRequest(response, "missing className arg");
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
        
        jsobj = deploymentControllerInstance.startControllerAgent(endPoint, port, userName, className, rawArgs);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("controller agent deploy: failure detected: " + failMessage);
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
    
    
    private void stop(Request request, Response response) throws Exception {
        Path path = request.getPath();
        String monManagID;
        
        Scanner scanner = new Scanner(path.getName());
        
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            monManagID = scanner.next();
            scanner.close();
        }
        else {
            LOGGER.error("The controller agent ID is not valid");
            scanner.close();
            complain(response, "controller agent ID is not a valid UUID: " + path.getName());
            return;
        }
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = deploymentControllerInstance.stopControllerAgent(monManagID);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("stop controller agent: failure detected: " + failMessage);
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
    
    private void getControllerAgents(Request request, Response response) throws Exception {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInstance.getControllerAgents();

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getControllerAgents: failure detected: " + failMessage);
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

    private void getControllerAgentInfo(Request request, Response response) throws IOException, JSONException {
        Path path = request.getPath();
        
        boolean success = true;
        String failMessage;
        JSONObject jsobj = null;
        
        String dsID;
        
        Scanner scanner = new Scanner(path.getName());
        
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dsID = scanner.next();
            scanner.close();
        }
        else {
            LOGGER.error("controller agent ID is not valid");
            scanner.close();
            complain(response, "controller agent ID is not a valid UUID: " + path.getName());
            return;
        }
        
        //TODO expose method via proper interface
        //jsobj = controllerInstance.getControllerAgentInfo(monManagID);

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("get controller agent info failure: " + failMessage);
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