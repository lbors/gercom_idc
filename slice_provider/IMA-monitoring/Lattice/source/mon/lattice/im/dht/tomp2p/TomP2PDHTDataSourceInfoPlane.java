// TomP2PDHTDataSourceInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.DataSourceDelegate;
import mon.lattice.im.dht.AbstractDHTDataSourceInfoPlane;

/**
 * A TomP2PDHTDataSourceInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class TomP2PDHTDataSourceInfoPlane extends AbstractDHTDataSourceInfoPlane {
    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;

    // The hostname of the DHT root.
    String rootHost;

    // The port to connect to
    int rootPort;

    // The local port
    int port;
    
    int TCPPort;
    int UDPPort;

    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     * and start here on localPort.
     */
    
    public TomP2PDHTDataSourceInfoPlane(String remoteHostname, int remotePort, int localUDPPort, int localTCPPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	UDPPort = localUDPPort;
        TCPPort = localTCPPort;

	imNode = new TomP2PIMNode(UDPPort, TCPPort, rootHost, rootPort);
    }
    
    public TomP2PDHTDataSourceInfoPlane(int remotePort, int localUDPPort, int localTCPPort) {
	rootPort = remotePort;
	UDPPort = localUDPPort;
        TCPPort = localTCPPort;

	imNode = new TomP2PIMNode(UDPPort, TCPPort, rootPort);
    }
    
    public TomP2PDHTDataSourceInfoPlane(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new TomP2PIMNode(port, rootHost, rootPort);
    }

    public TomP2PDHTDataSourceInfoPlane(int remotePort, int localPort) {
	rootPort = remotePort;
	port = localPort;

	imNode = new TomP2PIMNode(port, rootPort);
    }
    
}