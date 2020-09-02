/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;


import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.ControlPlaneReplyMessage;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.ReceivingAndReplying;
import mon.lattice.distribution.Transmitting;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractZMQControlPlaneConsumer implements ControlPlane, ReceivingAndReplying, Transmitting {
    protected ZMQReceiver zmqReceiver;
    protected String routerAddress;
    protected int routerPort;
    
    static Logger LOGGER = LoggerFactory.getLogger("ControlPlaneConsumer");

    public AbstractZMQControlPlaneConsumer(InetSocketAddress router) {
        this.routerAddress = router.getAddress().getHostName();
        this.routerPort = router.getPort();
    }
   
    @Override
    public abstract boolean announce();
    

    @Override
    public abstract boolean dennounce();

    
    @Override
    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, ReflectiveOperationException;

    
    @Override
    public abstract int transmitReply(ControlPlaneReplyMessage answer, MetaData metadata) throws IOException;
    
    
    @Override
    public abstract boolean connect(); 
    

    @Override
    public boolean disconnect() {
        try {
	    zmqReceiver.end();
	    zmqReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    zmqReceiver = null;
	    return false;
	}
    }
    
    @Override
    public void eof() {
        disconnect();
    }
    
    @Override
    public void error(Exception e) {
        LOGGER.error("Error: " + e.getMessage());
    }

    @Override
    public boolean transmitted(int id) {
        LOGGER.info("Just sent Announce/Deannounce message");
        return true;
    }
    
    @Override
    public Map getControlEndPoint() {
        Map<String, String> controlEndPoint = new HashMap<>();
        controlEndPoint.put("type", "zmq");
        
        LOGGER.debug("Getting Control Endpoint");
        return controlEndPoint;
    }
}
