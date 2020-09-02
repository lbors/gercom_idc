// AbstractUDPDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package mon.lattice.distribution.zmq;

import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractZMQDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    ZMQDataSubscriber subscriber;
    
    int port;

    String remoteHost;
    
    // the MeasurementReceiver
    MeasurementReceiver measurementReceiver;

    // This keeps the last seqNo from each DataSource that is seen
    HashMap<ID, Integer> seqNoMap;

    
    
    /**
     * Construct a AbstractUDPDataPlaneConsumer.
     */
    public AbstractZMQDataPlaneConsumer(int port) {
        this.port = port;
	seqNoMap = new HashMap<ID, Integer>();
    }
    
    
    /**
     * Construct a AbstractUDPDataPlaneConsumer connecting to a remote host.
     */
    public AbstractZMQDataPlaneConsumer(String remoteHost, int port) {
        this.port = port;
        this.remoteHost = remoteHost;
	seqNoMap = new HashMap<ID, Integer>();
    }

    /**
     * Connect to a delivery mechanism.
     */
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (subscriber == null) {
                
                if (remoteHost != null) {
                    subscriber = new ZMQDataSubscriber(this, remoteHost, port);
                    subscriber.connect();
                }
                    
                else {
                    subscriber = new ZMQDataSubscriber(this, port);
                    subscriber.bind();
                }
                
                subscriber.listen();
                
		return true;
	    } else {
		return true;
	    }

	} catch (Exception ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}

    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	try {
	    subscriber.end();
	    subscriber = null;
	    return true;
	} catch (Exception ieo) {
	    subscriber = null;
	    return false;
	}
    }

    /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	// do nothing currenty
	return true;
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	// do nothing currenty
	return true;
    }


    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular address.
     * The expected message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    public abstract void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException;

    /**
     * This method is called just after there has been EOF
     * in received from some underlying transport.
     */
    public void eof() {
        disconnect();
    }


    /**
     * This method is called just after there has been an error
     * in received from some underlying transport.
     * This passes the exception into the Receiving object.
     */
    public void error(Exception e) {
	//System.err.println("DataConsumer: notified of error " + e.getMessage());
        /*
	System.err.println("Stack Trace:");
	e.printStackTrace(System.err);
        */
    }

    /**
     * Send a message.
     */
    public int sendData(DataPlaneMessage dpm) throws Exception {
	// currenty do nothing
	return -1;
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id) {
	return false;
    }

    /**
     * Receiver of a measurement, with an extra object that has context info
     */
    public Measurement report(Measurement m) {
	measurementReceiver.report(m);
	return m;
    }

    /**
     * Set the object that will receive the measurements.
     */
    public Object setMeasurementReceiver(MeasurementReceiver mr) {
	Object old = measurementReceiver;
	measurementReceiver = mr;
	return old;
    }


}
