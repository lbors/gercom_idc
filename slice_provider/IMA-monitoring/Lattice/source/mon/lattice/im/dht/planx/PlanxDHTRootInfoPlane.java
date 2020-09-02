// TomP2PDHTRootInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht.planx;

import mon.lattice.im.dht.AbstractDHTRootInfoPlane;


/**
 * A TomP2PDHTRootInfoPlane is an InfoPlane implementation
 that acts as a ROOT for the Information Model data.
 * There needs to be one root for a DHT.
 * The other nodes connect to it.
 */
public class PlanxDHTRootInfoPlane extends AbstractDHTRootInfoPlane {
    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     */
    public PlanxDHTRootInfoPlane(String localHostname, int localPort) {
	rootHost = localHostname;
	rootPort = localPort;

        // from the super class
	imNode = new PlanxIMNode(localPort, localHostname, localPort);
    }
}