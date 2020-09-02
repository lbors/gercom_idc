// DHTDataSourceInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht.planx;

import java.io.IOException;
import mon.lattice.im.dht.AbstractDHTDataConsumerInfoPlane;

/**
 * A TomP2PDHTDataConsumerInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 */

public class PlanxDHTDataConsumerInfoPlane extends AbstractDHTDataConsumerInfoPlane {
    // The hostname of the DHT root.
    String rootHost;

    // The port to connect to
    int rootPort;

    // The local port
    int port;

    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     * and start here on localPort.
     */
    public PlanxDHTDataConsumerInfoPlane(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new PlanxIMNode(localPort, remoteHostname, remotePort);
    }
    
    
    @Override
    public boolean dennounce() {
        try {
	    imNode.removeDataConsumer(dataConsumer);
	    LOGGER.info("just removed this Data Consumer " + dataConsumer.getID() + " from the info plane");
	    return true;
	} catch (IOException ioe) {
	    return false;
	} 
        
    }

    @Override
    public boolean announce() {
        try {
	    imNode.addDataConsumer(dataConsumer);
            imNode.addDataConsumerInfo(dataConsumer);
	    LOGGER.info("just added this Data Consumer " + dataConsumer.getID() + " to the info plane");
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
        
    }
}