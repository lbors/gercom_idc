// DHTDataSourceInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht;

import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.Reporter;

import java.io.IOException;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.DataSource;
import mon.lattice.core.EntityType;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import mon.lattice.control.agents.ControllerAgent;

/**
 * A TomP2PDHTDataConsumerInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 */

public abstract class AbstractDHTDataConsumerInfoPlane extends AbstractDHTInfoPlane implements DataConsumerInteracter {
    protected ControllableDataConsumer dataConsumer;

    /**
     * Announce that the plane is up and running
     */
    @Override
    public boolean announce() {
	addDataConsumerInfo(dataConsumer);
        return true;
    }

    /**
     * Un-sendMessage that the plane is up and running
     */
    @Override
    public boolean dennounce() {
        try {
	    imNode.removeDataConsumer(dataConsumer);
            
            imNode.sendMessage(new DeannounceMessage(dataConsumer.getID(), EntityType.DATACONSUMER));
	    LOGGER.info("just deannounced this Data Consumer " + dataConsumer.getID());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}        
    }
    
    
    @Override
    public ControllableDataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public ControllableDataConsumer setDataConsumer(ControllableDataConsumer dc) {
        this.dataConsumer = dc;
        return dataConsumer;
    }
    

    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        try {
	    imNode.addDataConsumer(dc);
            imNode.addDataConsumerInfo(dc);
            
            imNode.sendMessage(new AnnounceMessage(dataConsumer.getID(), EntityType.DATACONSUMER));
	    LOGGER.info("just announced this Data Consumer " + dc.getID());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        try {
	    imNode.addReporter((ControllableReporter)r);
	    LOGGER.info("just added reporter " + ((ControllableReporter)r).getName());
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        try {
	    imNode.removeDataConsumer(dc);
	    LOGGER.info("just removed Data Consumer " + dc);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        try {
	    imNode.addReporter((ControllableReporter)r);
	    LOGGER.info("just removed reporter " + r);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }
    
    
    /* A Data Consumer cannot modify any Data Source related info plane items */
    
    @Override
    public boolean addDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean addProbeInfo(Probe p) {
        return false;
    }

    @Override
    public boolean modifyDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean modifyProbeInfo(Probe p) {
        return false;
    }

    @Override
    public boolean removeDataSourceInfo(DataSource ds) {
        return false;
    }

    @Override
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
        return false;
    }

    @Override
    public boolean removeProbeInfo(Probe p) {
        return false;
    }
    

    /* A Data Consumer cannot modify any Controller Agent related info plane items */
    
    @Override
    public boolean addControllerAgentInfo(ControllerAgent agent) {
        return false;
    }

    @Override
    public boolean removeControllerAgentInfo(ControllerAgent agent) {
        return false;
    }
}