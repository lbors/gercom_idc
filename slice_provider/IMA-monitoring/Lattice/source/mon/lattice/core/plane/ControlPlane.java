// ControlPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package mon.lattice.core.plane;

import java.util.Map;

/**
 * A ControlPlane.
 * This has the common methods for all 
 */
public interface ControlPlane extends Plane {

    /**
     *
     * @return the Control End Point
     */
    
    public Map getControlEndPoint();
}
