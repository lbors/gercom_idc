/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import java.io.IOException;
import java.net.InetSocketAddress;
import mon.lattice.control.ControlServiceException;
import mon.lattice.control.zmq.ZMQDataConsumerControlPlaneXDRConsumer;
import mon.lattice.control.zmq.ZMQReceiver;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;

/**
 *
 * @author uceeftu
 */
public class ZMQControlPlaneXDRConsumerWithControlAgents extends ZMQDataConsumerControlPlaneXDRConsumer implements ControllerAgentInteracter, ControllerAgentService {

    ControllerAgent controllerAgent;
    
    public ZMQControlPlaneXDRConsumerWithControlAgents(InetSocketAddress router) {
        super(router);
    }

    @Override
    public boolean announce() {
        return true;
    }

    @Override
    public boolean dennounce() {
        return true;
    }

    @Override
    public boolean connect() {
        try {
	    // only connect if we're not already connected
	    if (zmqReceiver == null) {
		zmqReceiver  = new ZMQReceiver(this, routerAddress, routerPort);
                zmqReceiver.setIdentity(controllerAgent.getID().toString());
                zmqReceiver.connect();
		zmqReceiver.listen();
		return true;
	    } else {
		return true;
	    }

	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}
    }

    @Override
    public ControllerAgent getControllerAgent() {
        return controllerAgent;
    }

    @Override
    public void setControllerAgent(ControllerAgent controllerAgent) {
        this.controllerAgent = controllerAgent;
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
    public boolean setMonitoringReportingEndpoint(ID id, String address, int port) throws ControlServiceException {
        return controllerAgent.setMonitoringReportingEndpoint(id, address, port);
    }
    
}
