// DataPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package mon.lattice.core.plane;


/**
 * A Plane.
 * This has the common methods for all planes.
 */
public interface Plane {

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect();

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect();

    /**
     * Announce that the plane is up and running
     */
    public boolean announce();

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce();
}
