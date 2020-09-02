/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents.appl;

import java.net.UnknownHostException;
import mon.lattice.control.ControlServiceException;
import mon.lattice.control.agents.AbstractZMQControllerAgent;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;

/**
 *
 * @author uceeftu
 */
public class PrometheusControllerAgent extends AbstractZMQControllerAgent {

    public PrometheusControllerAgent(String id, String controlHostAddress, int controlHostPort, String remoteInfoHost, int remoteInfoPort) throws UnknownHostException {
        super(id, controlHostAddress, controlHostPort, remoteInfoHost, remoteInfoPort);
    }

    @Override
    public boolean setCollectionRate(Rational dataRate) throws ControlServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rational getConnectionRate() throws ControlServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setMonitoringReportingEndpoint(ID id, String address, int port) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
