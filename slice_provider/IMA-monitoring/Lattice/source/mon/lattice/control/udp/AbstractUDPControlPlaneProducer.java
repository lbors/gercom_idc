/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.udp;

import mon.lattice.control.SynchronousTransmitting;
import mon.lattice.control.ControlPlaneConsumerException;
import mon.lattice.im.delegate.InfoPlaneDelegate;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.core.plane.ControllerControlPlane;
import mon.lattice.core.plane.ControlPlaneMessage;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.distribution.udp.UDPReceiver;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP based request-reply protocol to send control messages to Data Sources
 * connected to the control plane.
 * It also allows listeners to be added and called back when an announce message
 * is received from a Data Source on this plane (useful when the info plane 
 * implementation does not provide that functionality)
 * @author uceeftu
 */
public abstract class AbstractUDPControlPlaneProducer implements 
        ControllerControlPlane, SynchronousTransmitting, Receiving, InfoPlaneDelegateInteracter  {
    
    UDPReceiver AnnounceListener;
    UDPTransmitterPool controlTransmittersPool;
    int maxPoolSize;
    int announceListenerPort;
    
    InfoPlaneDelegate infoPlaneDelegate;
    AnnounceEventListener listener;
    
    static Logger LOGGER = LoggerFactory.getLogger("UDPControlPlaneProducer");
    
    public AbstractUDPControlPlaneProducer(int maxPoolSize) {
        this.announceListenerPort = -1;
        this.maxPoolSize = maxPoolSize;
    }
    
    
    public AbstractUDPControlPlaneProducer(int port, int maxPoolSize) {
        this.announceListenerPort = port;
        this.maxPoolSize = maxPoolSize;
    }
    

    @Override
    public boolean connect() {
	try {
	    // Creating listener for Announce Messages - only connect if we're not already connected
	    if (AnnounceListener == null && announceListenerPort != -1) {
                AnnounceListener = new UDPReceiver(this, announceListenerPort, "AnnounceListener");
                AnnounceListener.listen();
            }
            
            if (controlTransmittersPool == null) {
                // creating a pool for Control Messages transmission
                // 8 seems to match the max size of the threadPool created by the RestConsole
                controlTransmittersPool = new UDPTransmitterPool(this, maxPoolSize); 
            }       
            return true;
            
	} catch (IOException ioe) {
	    LOGGER.error("Error while connecting " + ioe.getMessage());
	    return false;
	}
    }

    @Override
    public boolean disconnect() {
        try {
	    AnnounceListener.end();
	    AnnounceListener = null;
            controlTransmittersPool.disconnect();
	    return true;
	} catch (IOException ieo) {
	    AnnounceListener = null;
	    return false;
	}
    }

    
    @Override
    public boolean announce() {
        // sending announce messages is not expected for a Control Plane Producer
	return false;
    }

    @Override
    public boolean dennounce() {
        // sending deannounce messages is not expected for a Control Plane Producer
	return false;
    }

    @Override
    public abstract Object synchronousTransmit(ControlPlaneMessage dpm, MetaData metaData) throws IOException, ControlPlaneConsumerException;

    
    @Override
    public abstract Object receivedReply(ByteArrayInputStream bis, MetaData metaData, int seqNo) throws IOException;
    
    
    @Override
    public Map getControlEndPoint() {
        throw new UnsupportedOperationException("Abstract UDP Control Plane Producer: getting control endpoint is not supported");
    }
    

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return infoPlaneDelegate;
    }

    @Override
    public void setInfoPlaneDelegate(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }
    
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}
