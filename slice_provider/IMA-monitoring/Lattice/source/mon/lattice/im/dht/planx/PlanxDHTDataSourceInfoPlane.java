// TomP2PDHTDataSourceInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.im.dht.planx;

import java.io.IOException;
import mon.lattice.core.DataSource;
import mon.lattice.im.dht.AbstractDHTDataSourceInfoPlane;

/**
 * A TomP2PDHTDataSourceInfoPlane is an InfoPlane implementation
 that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class PlanxDHTDataSourceInfoPlane extends  AbstractDHTDataSourceInfoPlane {

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
    
    
    public PlanxDHTDataSourceInfoPlane(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new PlanxIMNode(port, rootHost, rootPort);
    }

    @Override
    public boolean dennounce() {
        try {
            DataSource dataSource = dataSourceDelegate.getDataSource();
            imNode.removeDataSource(dataSource);
            LOGGER.info("just removed this Data Source " + dataSource.getID() + " from the info plane");
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    public boolean announce() {
        // the Data Source is not actually announced to the controller
        // via sending a message in this DHT implementation
        try {
	    DataSource dataSource = dataSourceDelegate.getDataSource();
	    imNode.addDataSource(dataSource);
            // adding additional DS information
            addDataSourceInfo(dataSource);
	    LOGGER.info("just added this Data Source " + dataSource.getID() + " to the info plane");
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }
    
}