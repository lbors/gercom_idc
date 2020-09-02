// InfoService.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package mon.lattice.core.plane;

import java.io.Serializable;

/**
 * The InfoService
 * This has the common methods for getting info.
 */
public interface DataConsumerInfoService {
    
    /*
     * General .
     */

    /**
     * Put a value in the InfoPlane.
     */
    public boolean putInfo(String key, Serializable value);

    /**
     * Get a value from the InfoPlane.
     */
    public Object getInfo(String key);

    /**
     * Remove a value from the InfoPlane.
     */
    public boolean removeInfo(String key);

}