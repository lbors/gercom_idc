// InfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package mon.lattice.core.plane;

import java.io.Serializable;


/**
 * A InfoPlane.
 * This has the common methods for all 
 */
public interface InfoPlane extends Plane, ProducerInfoService, ConsumerInfoService {
    public String getInfoRootHostname();
    
    public boolean putInfo(String key, Serializable value);

    public boolean removeInfo(String key);

    public Object getInfo(String key);
    
}
