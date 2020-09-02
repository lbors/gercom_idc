/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import mon.lattice.control.ControlServiceException;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;

/**
 *
 * @author uceeftu
 */
public interface ControllerAgentService {
    public boolean setCollectionRate(Rational dataRate) throws ControlServiceException;
    
    public Rational getConnectionRate() throws ControlServiceException;
    
    public boolean setMonitoringReportingEndpoint(ID id, String address, int port) throws ControlServiceException;
    
    
}
