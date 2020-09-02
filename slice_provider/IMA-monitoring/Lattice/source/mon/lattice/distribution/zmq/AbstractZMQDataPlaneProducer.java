package mon.lattice.distribution.zmq;

import mon.lattice.distribution.TransmittingData;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DataSourceDelegateInteracter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AbstractUDPDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by UDP.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractZMQDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {
    String remoteHost;
    int remotePort = 0;

    ZMQDataPublisher publisher;
    
    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;
    
    static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQDataPlaneProducer.class);

    /**
     * Construct an AbstractUDPDataPlaneProducer.
     */
    public AbstractZMQDataPlaneProducer(String remoteHost, int remotePort) {
	this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }


    /**
     * Connect to a delivery mechansim.
     */
     public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (publisher == null) {
		// Now connect to the IP address
		publisher = new ZMQDataPublisher(this, remoteHost, remotePort);

		publisher.connect();

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
	    publisher.end();
	    publisher = null;
	    return true;
	} catch (IOException ieo) {
	    publisher = null;
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
        if (publisher != null) {
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
