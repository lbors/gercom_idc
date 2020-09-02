/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.control.ControlPlaneConsumerException;
import mon.lattice.control.ControlServiceException;
import mon.lattice.control.zmq.ZMQControlMetaData;
import mon.lattice.control.zmq.ZMQControlPlaneXDRProducer;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;
import mon.lattice.core.plane.ControlOperation;
import mon.lattice.core.plane.ControlPlaneMessage;
import mon.lattice.core.plane.ControllerControlPlaneWithAgents;
import mon.lattice.distribution.MetaData;
import mon.lattice.im.delegate.ControllerAgentNotFoundException;
import mon.lattice.im.delegate.ZMQControlEndPointMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class ZMQControlPlaneXDRProducerWithControlAgents extends ZMQControlPlaneXDRProducer implements ControllerControlPlaneWithAgents { //ControllerAgentService {

    private static Logger LOGGER = LoggerFactory.getLogger(ZMQControlPlaneXDRProducerWithControlAgents.class);
    
    public ZMQControlPlaneXDRProducerWithControlAgents(int maxPoolSize, int port) {
        super(maxPoolSize, port);
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
    public boolean setMonitoringReportingEndpoint(ID controllerAgentID, String address, int port) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(controllerAgentID);
        args.add(address);
        args.add(port);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_MONITORING_ENDPOINT, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getControllerAgentAddressFromID(controllerAgentID);
            
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) synchronousTransmit(m, mData);
            
        } catch (IOException | ControllerAgentNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing set Monitoring Reporting Endpoint command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        
        return result;
    }
    
    
    
}
