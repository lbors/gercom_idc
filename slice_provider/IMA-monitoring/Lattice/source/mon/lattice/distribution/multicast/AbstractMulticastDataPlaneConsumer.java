// AbstractMulticastDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package mon.lattice.distribution.multicast;

import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * An AbstractMulticastDataPlaneConsumer is a DataPlane implementation
 * that receives Measurements by multicast.
 */
public abstract class AbstractMulticastDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {
    // The address we are sending to
    MulticastAddress address;

     // The MulticastReceiver
     MulticastReceiver mcastReceiver;


    // the MeasurementReceiver
    MeasurementReceiver measurementReceiver;

    // This keeps the last seqNo from each DataSource that is seen
    HashMap<ID, Integer> seqNoMap;


    /**
     * Construct an AbstractMulticastDataPlaneConsumer.
     */
    public AbstractMulticastDataPlaneConsumer(MulticastAddress addr) {
	// sending address
	address = addr;

	seqNoMap = new HashMap<ID, Integer>();
    }

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (mcastReceiver == null) {
		MulticastReceiver rr = new MulticastReceiver(this, address);

		rr.join();
		
		mcastReceiver = rr;

		return true;
	    } else {
		return true;
	    }

	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    ioe.printStackTrace();

	    return false;
	}

    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	try {
	    mcastReceiver.leave();
	    mcastReceiver = null;
	    return true;
	} catch (IOException ieo) {
	    mcastReceiver = null;
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
     * at a particular multicast address.
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
	System.err.println("DataConsumer: notified of error " + e.getMessage());
	System.err.println("Stack Trace:");
	e.printStackTrace(System.err);
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
     * Receiver of a measurment, with an extra object that has context info
     */
    public Measurement report(Measurement m) {
	//System.err.println("MulticastDataPlaneConsumer: got " + m);
	measurementReceiver.report(m);
	return m;
    }

    /**
     * Set the object that will recieve the measurements.
     */
    public Object setMeasurementReceiver(MeasurementReceiver mr) {
	Object old = measurementReceiver;
	measurementReceiver = mr;
	return old;
    }


}
