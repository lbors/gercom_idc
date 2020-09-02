// TransmittingData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.distribution;

import mon.lattice.core.TypeException;
import mon.lattice.core.plane.DataPlaneMessage;
import java.io.IOException;

/**
 * An interface for distribution components that need
 * to do transmitting of DataPlaneMessage objects.
 */
public interface TransmittingData extends Transmitting {
    /**
     * Send a message onto the multicast address.
     */
    public int transmit(DataPlaneMessage dpm) throws Exception; //IOException, TypeException;
}