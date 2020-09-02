// DHTDataSourceInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.ID;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.Reporter;

import java.io.IOException;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import mon.lattice.im.dht.AbstractDHTDataConsumerInfoPlane;

/**
 * A TomP2PDHTDataConsumerInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 */

public class TomP2PDHTDataConsumerInfoPlane extends AbstractDHTDataConsumerInfoPlane {
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
    public TomP2PDHTDataConsumerInfoPlane(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new TomP2PIMNode(localPort, remoteHostname, remotePort);
    }
    
    public TomP2PDHTDataConsumerInfoPlane(int remotePort, int localPort) {
	rootPort = remotePort;
	port = localPort;

	imNode = new TomP2PIMNode(localPort, remotePort);
    }
}