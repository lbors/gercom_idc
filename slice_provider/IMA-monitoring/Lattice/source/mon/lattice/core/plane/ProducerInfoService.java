/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.Reporter;
import mon.lattice.control.agents.ControllerAgent;

/**
 *
 * @author uceeftu
 */
public interface ProducerInfoService {
    
    /** 
     * Data Source related methods.
     */
    
    boolean addDataSourceInfo(DataSource ds);

    boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa);

    boolean addProbeInfo(Probe p);
    
    
    boolean modifyDataSourceInfo(DataSource ds);

    boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa);

    boolean modifyProbeInfo(Probe p);

    
    boolean removeDataSourceInfo(DataSource ds);

    boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa);

    boolean removeProbeInfo(Probe p);
    
    
    /** 
     * Data Consumer related methods.
     */
    
    boolean addDataConsumerInfo(ControllableDataConsumer dc);

    boolean addReporterInfo(Reporter r);
    
    
    boolean removeDataConsumerInfo(ControllableDataConsumer dc);

    boolean removeReporterInfo(Reporter r);
    
    
    /** 
     * Controller Agent Related methods.
     */
    
    boolean addControllerAgentInfo(ControllerAgent agent);
    
    boolean removeControllerAgentInfo(ControllerAgent agent);    
    
}
