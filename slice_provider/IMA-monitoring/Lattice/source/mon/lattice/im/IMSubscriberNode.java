/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im;

import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public interface IMSubscriberNode extends IMBasicNode {
    
    /**
     * Lookup information
     */
    public Object getDataSourceInfo(ID dsID, String info);

    public Object getProbeInfo(ID probeID, String info);

    public Object getProbeAttributeInfo(ID probeID, Integer field, String info);
    
    public Object getProbesOnDataSource(ID dsID);

    public Object getDataConsumerInfo(ID dcID, String info);

    public Object getReporterInfo(ID reporterID, String info);
    
    public boolean containsDataSource(ID dataSourceID, int timeout);
    
    public boolean containsDataConsumer(ID dataConsumerID, int timeout);
    
    public boolean containsControllerAgent(ID controllerAgentID, int timeout);
    
}
