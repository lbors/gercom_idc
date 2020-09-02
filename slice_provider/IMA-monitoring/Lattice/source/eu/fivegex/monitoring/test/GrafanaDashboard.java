/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.Resty;
import us.monoid.json.JSONObject;
import static us.monoid.web.Resty.content;

/**
 *
 * @author uceeftu
 */
public class GrafanaDashboard {
    
    private String host;
    private int port;
    private String title;

    private String username = "admin";
    private String password = "admin";
    
    private JSONObject request;
    private Resty rest;
    
    public GrafanaDashboard(String host, int port, String title) {
       this.host = host;
       this.port = port;
       this.title = title; 
       this.request = new JSONObject();
       this.rest = new Resty();
    }
    
    public JSONObject initializeRequest(List<String> entities, List<String> metrics, List<String> units) {       
        
        try {
            JSONObject dashboard = new JSONObject();
            dashboard.put("id", JSONObject.NULL);
            dashboard.put("title", title);
            dashboard.put("timezone", "browser");
            
            JSONArray rows = new JSONArray();
            
            int panelID = 0;
            Iterator<String> unitsIterator = units.iterator();
            for (String metric : metrics) {
                rows.put(this.addPanel(metric, entities, panelID, unitsIterator.next()));
                panelID++;
            }
            dashboard.put("rows", rows);
            
            dashboard.put("schemaVersion", 6);
            dashboard.put("version", 0);
            
            request.put("dashboard", dashboard);
            request.put("overwrite", false);
            
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        
        return request;
    }
    
    
    private JSONObject addPanel(String metric, List<String> entities, int panelID, String unit) {
        JSONObject panel = new JSONObject();
        try {
            panel.put("collapse", false);
            panel.put("height", "250px");
                JSONArray panelArray = new JSONArray();
                JSONObject panelDetails = new JSONObject();
                panelDetails.put("bars", false);
                panelDetails.put("datasource", "5GEx"); // may be a parameter
                panelDetails.put("fill", 0); //check
                panelDetails.put("id", panelID);
                    JSONObject legend = new JSONObject();
                    legend.put("avg", false);
                    legend.put("current", false);
                    legend.put("max", false);
                    legend.put("min", false);
                    legend.put("show", true);
                    legend.put("total", false);
                    legend.put("values", false);
                    panelDetails.put("legend", legend);
                panelDetails.put("lines", true);
                panelDetails.put("linewidth", 1);
                panelDetails.put("links", new JSONArray());
                panelDetails.put("nullPointMode", JSONObject.NULL);
                panelDetails.put("percentage", false);
                panelDetails.put("pointradius", 1);
                panelDetails.put("points", false);
                panelDetails.put("renderer", "flot");
                //panelDetails.put("seriesOverrides", new JSONArray());
                panelDetails.put("span", 12);
                panelDetails.put("stack", true); // check
                panelDetails.put("steppedLine", false);
                
                    JSONArray targets = new JSONArray();
                    // add all targets (i.e., NFs and links)
                    for (String entity : entities) {
                        targets.put(this.addTarget(entity, metric));
                    }
                    panelDetails.put("targets", targets);
        
                panelDetails.put("thresholds", new JSONArray());
                panelDetails.put("timeFrom", JSONObject.NULL);
                panelDetails.put("timeShift", JSONObject.NULL);
                panelDetails.put("title", metric);
                
                    JSONObject tooltip = new JSONObject();
                    tooltip.put("shared", true);
                    tooltip.put("sort", 0);
                    tooltip.put("value_type", "individual");
                    panelDetails.put("tooltip", tooltip);
                    
                panelDetails.put("type", "graph");
                
                    JSONObject xAxis = new JSONObject();
                    xAxis.put("mode", "time");
                    xAxis.put("name", JSONObject.NULL);
                    xAxis.put("show", true);
                    xAxis.put("values", new JSONArray());
                    panelDetails.put("xaxis", xAxis);
                    
                    JSONObject yAxes = new JSONObject();
                    yAxes.put("format", unit); // correct y axes format
                    yAxes.put("label", JSONObject.NULL);
                    yAxes.put("logBase", 1);
                    yAxes.put("max", JSONObject.NULL);
                    yAxes.put("min", JSONObject.NULL);
                    yAxes.put("show", true);
                    
                    JSONArray yAxesArray = new JSONArray();
                    yAxesArray.put(yAxes); //left
                    yAxesArray.put(yAxes); //right
                    panelDetails.put("yaxes", yAxesArray);
                    
                    
                panelArray.put(panelDetails);
            panel.put("panels", panelArray);
            
            panel.put("repeat", JSONObject.NULL);
            panel.put("repeatIteration", JSONObject.NULL);
            panel.put("repeatRowId", JSONObject.NULL);
            panel.put("showTitle", false);
            panel.put("title", "Dashboard Row");
            panel.put("titleSize", "h6");
            
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        
        return panel;   
    }
    
    
    private JSONObject addTarget(String entityName, String metric) {
        JSONObject target = new JSONObject();
        
        try {
            target.put("alias", entityName);
            target.put("dsType", "influxdb");
            
                JSONObject gBy1 = new JSONObject();
                JSONArray params = new JSONArray();
                params.put("$interval");
                gBy1.put("params", params);
                gBy1.put("type", "time");

                JSONObject gBy2 = new JSONObject();
                JSONArray params2 = new JSONArray();
                params2.put(JSONObject.NULL);
                gBy2.put("params", params2);
                gBy2.put("type", "fill");

                JSONArray groupBy = new JSONArray();
                groupBy.put(gBy1);
                groupBy.put(gBy2);
                
            target.put("groupBy", groupBy);    
                
            target.put("hide", false);
            target.put("policy", "default");
            target.put("query", "SELECT value FROM " + 
                                                    "\"" + metric + "\""
                                                    + " WHERE resourceid=" 
                                                    + "'" + entityName + "'");
            target.put("rawQuery", true);
            target.put("resultFormat", "time_series");
            
                JSONObject select = new JSONObject();
                JSONArray params2_1 = new JSONArray();
                params2_1.put("value");
                select.put("params", params2_1);
                select.put("type", "field");

                JSONObject select2 = new JSONObject();
                JSONArray params2_2 = new JSONArray();
                select2.put("params", params2_2);
                select2.put("type", "mean");
                
                JSONArray selectGroup = new JSONArray();
                selectGroup.put(select);
                selectGroup.put(select2);
                
            target.put("select", (new JSONArray()).put(selectGroup));
            target.put("tags", new JSONArray());
            
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        
        return target;
    }
    
    
    
    private void printRequest() {
        try {
            System.out.println(request.toString(2));
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
    }
    
    
    public void issueRequest() {
        try {
        String url = "http://" + username + ":" + password + "@" + host + ":" + port + "/api/dashboards/db/";
        
        String encodedString = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("UTF-8"));
        rest.alwaysSend("Authorization", "Basic " + encodedString);
        
        System.out.println(rest.json(url, content(request)).toObject());
        
        } catch (IOException | JSONException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static void main(String [] args) {
        
        String grafanaHost = args[0];
        int grafanaPort = Integer.valueOf(args[1]);
        String serviceName = args[2];
        String vnfName = args[3];
        
        GrafanaDashboard g = new GrafanaDashboard(grafanaHost, grafanaPort, serviceName);
        
        List<String> NFs = new ArrayList<>();
        List<String> KPIs = new ArrayList<>();
        List<String> units = new ArrayList<>();
        
        NFs.add("host1ID");
        NFs.add("host2ID");
        NFs.add(vnfName);
        
        KPIs.add("cpu.percent");
        KPIs.add("mem.percent");
        KPIs.add("mem.used");
        
        units.add("percent");
        units.add("percent");
        units.add("bytes");
        
        g.initializeRequest(NFs, KPIs, units); // should also look for the correct service instance ID
        g.printRequest();
        g.issueRequest();
    }
    
}
