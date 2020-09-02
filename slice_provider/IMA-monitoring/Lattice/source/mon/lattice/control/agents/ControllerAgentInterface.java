/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import mon.lattice.core.Rational;

/**
 *
 * @author uceeftu
 */
public interface ControllerAgentInterface <ReturnType> {
    ReturnType setCollectionRate(Rational collectionRate) throws Exception;
    
    ReturnType getConnectionRate() throws Exception;
    
    ReturnType setMonitoringReportingEndpoint(String id, String destinationAddress, String destinationPort) throws Exception;
    
    ReturnType getControllerAgents() throws Exception;   
}
