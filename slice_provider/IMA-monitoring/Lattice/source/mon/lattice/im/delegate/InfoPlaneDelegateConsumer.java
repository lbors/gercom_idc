/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import java.io.IOException;
import mon.lattice.core.ID;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateConsumer {
    public boolean containsDataSource(ID id);
        
    public boolean containsDataConsumer(ID id);
    
    public boolean containsControllerAgent(ID id);
    
    public JSONArray getDataSources() throws JSONException;
    
    public JSONArray getDataConsumers() throws JSONException;
    
    public JSONArray getControllerAgents() throws JSONException;
    
    public ControlEndPointMetaData getDSAddressFromProbeID(ID probe) throws ProbeNotFoundException, DSNotFoundException, IOException;
    
    public ControlEndPointMetaData getDSAddressFromID(ID dataSource) throws DSNotFoundException, IOException;
    
    public ControlEndPointMetaData getControllerAgentAddressFromID(ID controllerAgent) throws ControllerAgentNotFoundException, IOException;
    
    public String getDSIDFromName(String dsName) throws DSNotFoundException;
    
    public ControlEndPointMetaData getDCAddressFromID(ID dataConsumer) throws DCNotFoundException, IOException;
    
    public ControlEndPointMetaData getDCAddressFromReporterID(ID reporter) throws ReporterNotFoundException, DCNotFoundException, IOException;
    
    public int getDSPIDFromID(ID dataSource) throws DSNotFoundException;
    
    public int getDCPIDFromID(ID dataConsumer) throws DCNotFoundException;  
    
    public int getControllerAgentPIDFromID(ID controllerAgent) throws ControllerAgentNotFoundException;
    
    public JSONArray getProbesOnDS(ID dataSource) throws DSNotFoundException; 
}