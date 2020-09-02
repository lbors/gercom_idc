/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.controller.json;

import mon.lattice.control.AbstractController;
import mon.lattice.control.ControlInterface;
import mon.lattice.control.ControlServiceException;
import mon.lattice.core.ID;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.delegate.DSNotFoundException;
import mon.lattice.core.Rational;
import us.monoid.json.JSONArray;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractJSONController extends AbstractController<JSONObject> implements ControlInterface<JSONObject> {
    protected int controlLocalPort;
    protected int transmitterPoolSize;
    protected int infoPlanePort;
    
    
    @Override
    public JSONObject getDataSourceInfo(String dsID) throws JSONException {
        JSONObject result = new JSONObject();
         
        String dsName;
        
        result.put("operation", "getDataSourceInfo");
        result.put("ID", dsID);
        try {
            dsName = (String) this.getControlHandle().getDataSourceInfo(ID.fromString(dsID));
            result.put("name", dsName);
            
            JSONArray info = this.controlInformationManager.getProbesOnDS(ID.fromString(dsID));
            result.put("probes", info);
            
            result.put("success", true);
        } catch (ControlServiceException | DSNotFoundException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        return result;
     }
    
    
    @Override
    public JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdProbeID;
        
        result.put("operation", "loadProbe");
        result.put("probeClassName",probeClassName);
        
        String [] probeArgsAsStrings;
        Object [] probeArgsAsObjects = (Object []) null;
        
        if (!probeArgs.isEmpty()) {
            probeArgsAsStrings = probeArgs.split(" ");
            probeArgsAsObjects = new Object[probeArgsAsStrings.length];
            for (int i=0; i<probeArgsAsStrings.length; i++) {
                if (probeArgsAsStrings[i].contains("[")) {
                    String trimmedListArg = probeArgsAsStrings[i].substring(1, probeArgsAsStrings[i].length()-2);
                    String [] listArg = trimmedListArg.split(",");
                    probeArgsAsObjects[i] = new Object[listArg.length];
                    probeArgsAsObjects[i] = (Object[])listArg;
                }
                else
                    probeArgsAsObjects[i] = probeArgsAsStrings[i];
            }
        }
        
        try {
            createdProbeID = this.getControlHandle().loadProbe(ID.fromString(id), probeClassName, probeArgsAsObjects);
            result.put("createdProbeID", createdProbeID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing loadProbe operation: " + ex.getMessage());
        }
        return result;
        }
    
    @Override
    public JSONObject unloadProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "unlodProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().unloadProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    
    @Override
    public JSONObject turnOffProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        Boolean invocationResult;
        
        result.put("operation", "turnOffProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOffProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject turnOnProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "turnOnProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOnProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    
     
    @Override
    public JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "setProbeServiceID");
        result.put("probeID",probeID);
        result.put("serviceID",serviceID);
        
        try {
            invocationResult = this.getControlHandle().setProbeServiceID(ID.fromString(probeID), ID.fromString(serviceID));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject setProbeGroupID(String probeID, String groupID) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "setProbeGroupID");
        result.put("probeID",probeID);
        result.put("sliceID",groupID);
        
        try {
            invocationResult = this.getControlHandle().setProbeGroupID(ID.fromString(probeID), ID.fromString(groupID));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }

    
    @Override
    public JSONObject setProbeDataRate(String probeID, String dataRate) throws JSONException {
        JSONObject result = new JSONObject();
        Boolean invocationResult;
        
        result.put("operation", "setProbeDataRate");
        
        result.put("probeID",probeID);
        result.put("rate",dataRate);
        
        try {
            Rational actualRate = (new Rational(dataRate)).multiply(60); // convert from measurements/min to measurements/hour
            invocationResult = this.getControlHandle().setProbeDataRate(ID.fromString(probeID), actualRate);
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject getProbeDataRate(String probeID) throws JSONException {
        JSONObject result = new JSONObject();
        Rational dataRate;
        
        result.put("operation", "getProbeDataRate");
        result.put("probeID",probeID);
        
        try {
            dataRate = this.getControlHandle().getProbeDataRate(ID.fromString(probeID));
            result.put("rate", dataRate.div(60)); //convert from measurements/hour to measurements/min
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    
    @Override
    public JSONObject getProbeServiceID(String probeID) throws JSONException {
        JSONObject result = new JSONObject();
        ID serviceID;
        
        result.put("operation", "getProbeServiceID");
        result.put("probeID",probeID);
        
        try {
            serviceID = this.getControlHandle().getProbeServiceID(ID.fromString(probeID));
            result.put("serviceID", serviceID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject getDataConsumerMeasurementRate(String dcID) throws JSONException {
        JSONObject result = new JSONObject();
        Rational rate;
        
        result.put("operation", "getDataConsumerMeasurementRate");
        
        result.put("ID",dcID);
        
        try {
            rate = this.getControlHandle().getDCMeasurementsRate(ID.fromString(dcID));
            result.put("rate", rate.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing getDataConsumerMeasurementRate operation: " + ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdReporterID;
        
        result.put("operation", "loadReporter");
        result.put("reporterClassName",reporterClassName);
        
        Object [] reporterArgsAsObjects = (Object[]) null; //= new Object[0];
        
        if (!reporterArgs.isEmpty()) {
            reporterArgsAsObjects = (Object[])reporterArgs.split(" ");
        }
        
        try {
            createdReporterID = this.getControlHandle().loadReporter(ID.fromString(id), reporterClassName, reporterArgsAsObjects);
            result.put("createdReporterID", createdReporterID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing loadReporter operation: " + ex.getMessage());
        }
        return result;
        }
    
    
    @Override
    public JSONObject unloadReporter(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "unlodReporter");
        result.put("reporterID",id);
        
        try {
            invocationResult = this.getControlHandle().unloadReporter(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing unloadReporter operation: " + ex.getMessage());
        }
        return result;
    }
    
    
    
    @Override
    public JSONObject getDataSources() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataSources");
        
        try {
            JSONArray dataSources = this.controlInformationManager.getDataSources();
            result.put("datasources", dataSources);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataSources operation: " + ex.getMessage());
          }
        return result;  
    }
    
    
    @Override
    public JSONObject getDataConsumers() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataConsumers");
        
        try {
            JSONArray dataConsumers = this.controlInformationManager.getDataConsumers();
            result.put("dataconsumers", dataConsumers);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataConsumers operation: " + ex.getMessage());
          }
        return result;  
    }
}
