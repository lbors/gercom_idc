package mon.lattice.im.zmq;

import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.Reporter;

import java.io.IOException;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.core.plane.InfoPlane;

/**
 * A ZMQDataSourceInfoPlane is an InfoPlane implementation
 that sends the Information Model data for a Data Source.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class ZMQDataSourceInfoPlane extends AbstractZMQInfoPlane implements InfoPlane, DataSourceDelegateInteracter {
    DataSourceDelegate dataSourceDelegate;
    
    // The hostname of the Subscriber.
    String remoteHost;

    // The port of the Subscriber
    int remotePort;
    
    /**
     * Construct a ZMQDataSourceInfoPlane.
     * Connect to the Proxy Subscriber at hostname on port,
     * and start here on localPort.
     */
    
    public ZMQDataSourceInfoPlane(String remoteHostname, int remotePort) {
	remoteHost = remoteHostname;
	this.remotePort = remotePort;

	zmqPublisher = new ZMQPublisher(remoteHost, this.remotePort);
    }
     
     
    /**
     * Connect to a delivery mechanism.
     */
    public boolean connect() {
	return zmqPublisher.connect();
    }

    /**
     * Disconnect from a delivery mechanism.
     */
    public boolean disconnect() {
	zmqPublisher.disconnect();
        zmqPublisher.destroyZMQContext();
        return true;
    }

    @Override
    public String getInfoRootHostname() {
        return zmqPublisher.getRootHostname();
    }
    
    /**
     * Announce that the Data Source is up and running
     */
    public boolean announce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("Announced this Data Source " + dataSource.getID());
        return addDataSourceInfo(dataSource);
    }

    /**
     * Un-announce that the Data Source is up and running
     */
    public boolean dennounce() {
        DataSource dataSource = dataSourceDelegate.getDataSource();
        LOGGER.info("Deannouncing Data Source " + dataSource.getID());
        return removeDataSourceInfo(dataSource);
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

    /**
     * Add a DataSource
     */
    public boolean addDataSourceInfo(DataSource ds) {
        try {
            zmqPublisher.addDataSource(ds);
            LOGGER.info("just added Data Source " + ds);
            return true;
        } catch (IOException e) 
            {
            return false;
            }
    }

    /**
     * Add a Probe
     */
    public boolean addProbeInfo(Probe p) {
	try {
	    zmqPublisher.addProbe(p);

	    LOGGER.info("just added Probe " + p.getClass());
            LOGGER.debug(p.toString());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }



    /**
     * Add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    zmqPublisher.addProbeAttribute(p, pa);

	    LOGGER.debug("just added ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Modify a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
//	try {
//	    zmqPublisher.modifyDataSource(ds);
//
//	    LOGGER.info("just modified DataSource " + ds);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Modify a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
//	try {
//	    zmqPublisher.modifyProbe(p);
//
//	    LOGGER.info("just modified Probe " + p.getClass());
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }

    /**
     * Modify a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
//	try {
//	    zmqPublisher.modifyProbeAttribute(p, pa);
//
//	    LOGGER.debug("just modified ProbeAttribute " + p + "." + pa);
//	    return true;
//	} catch (IOException ioe) {
//	    return false;
//	}
        return false;
    }


    /**
     * Remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	try {
	    zmqPublisher.removeDataSource(ds);

	    LOGGER.info("just removed Data Source " + ds);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
	try {
	    zmqPublisher.removeProbe(p);

	    LOGGER.info("just removed Probe " + p.getClass());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    zmqPublisher.removeProbeAttribute(p, pa);

	    LOGGER.debug("just removed ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /* these methods always return false here as they are meant to be used by 
     * a Data Consumer */
    
    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        return false;
    }
    
    @Override
    public boolean addControllerAgentInfo(ControllerAgent agent) {
        return false;
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        return false;
    }   

    @Override
    public boolean removeControllerAgentInfo(ControllerAgent agent) {
        return false;
    }
}