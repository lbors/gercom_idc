/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import mon.lattice.core.DataSource;
import mon.lattice.core.ID;
import mon.lattice.core.Probe;

/**
 *
 * @author uceeftu
 */
public interface ConsumerInfoService {
    
    /** 
     * Data Source related methods.
     */
    
    boolean containsDataSource(ID dataSourceID, int timeOut);
    
    
    Object lookupDataSourceInfo(DataSource dataSource, String info);

    Object lookupDataSourceInfo(ID dataSourceID, String info);

    Object lookupProbeAttributeInfo(Probe probe, int field, String info);

    Object lookupProbeAttributeInfo(ID probeID, int field, String info);

    Object lookupProbeInfo(Probe probe, String info);

    Object lookupProbeInfo(ID probeID, String info);

    Object lookupProbesOnDataSource(ID dataSourceID);
    
    
    
    /** 
     * Data Consumer related methods.
     */
    
    boolean containsDataConsumer(ID dataConsumerID, int timeOut);
    
    
    Object lookupDataConsumerInfo(ID dataConsumerID, String info);

    Object lookupReporterInfo(ID reporterID, String info);
    
    
    
    /** 
     * Controller Agents Related methods.
     */
    
    
    boolean containsControllerAgent(ID controllerAgentID, int timeOut);

    Object lookupControllerAgentInfo(ID controllerAgentID, String info);
    
}
