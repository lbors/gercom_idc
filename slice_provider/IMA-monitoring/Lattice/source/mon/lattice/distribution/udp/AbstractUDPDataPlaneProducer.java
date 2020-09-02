// AbstractUDPDataPlaneProducer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package mon.lattice.distribution.udp;

import mon.lattice.distribution.TransmittingData;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DataSourceDelegateInteracter;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * An AbstractUDPDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by UDP.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractUDPDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    // The address we are sending to
    InetSocketAddress address;

    // The UDPTransmitter
    UDPTransmitter udpTransmitter;

    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;

    /**
     * Construct an AbstractUDPDataPlaneProducer.
     */
    public AbstractUDPDataPlaneProducer(InetSocketAddress addr) {
	// sending address
	address = addr;
    }


    /**
     * Connect to a delivery mechansim.
     */
     public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (udpTransmitter == null) {
		// Now connect to the IP address
		UDPTransmitter tt = new UDPTransmitter(this, address);

		tt.connect();
		
		udpTransmitter = tt;

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

//     public boolean connect() {
// 	return producer.connect();
//     }

    /**
     * Disconnect from a delivery mechansim.
     */
    public synchronized boolean disconnect() {
	try {
	    udpTransmitter.end();
	    udpTransmitter = null;
	    return true;
	} catch (IOException ieo) {
	    udpTransmitter = null;
	    return false;
	}
    }

//     public boolean disconnect() {
// 	return producer.disconnect();
//     }

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
     * Send a message onto the address.
     * The message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    public abstract int transmit(DataPlaneMessage dsp) throws Exception;


    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id) {
	sentData(id);
	return true;
    }

    /**
     * Send a message.
     */
    public synchronized int sendData(DataPlaneMessage dpm) throws Exception {
        if (udpTransmitter != null) {
            return transmit(dpm);
        } else {
            return 0;
        }
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id) {
	return true;
    }

    /**
     * Receiver of a measurment, with an extra object that has context info
     */
    public Measurement report(Measurement m) {
	// currently do nothing
	return null;
    }

    /**
     * Get the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    /**
     * Set the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	dataSourceDelegate = ds;
	return ds;
    }

}
