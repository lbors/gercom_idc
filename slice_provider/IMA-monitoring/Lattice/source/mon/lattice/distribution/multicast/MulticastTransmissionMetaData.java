// MulticastTransmissionMetaData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.distribution.multicast;

import mon.lattice.distribution.MetaData;
import java.net.InetAddress;
import java.io.Serializable;

/**
 * Information about a transmission.
 * Includes: packet length, ip address, multicast address
 */
public class MulticastTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final InetAddress ipAddr;
    public final MulticastAddress mAddr;

    /**
     * Construct a MulticastTransmissionMetaData object.
     */
    public MulticastTransmissionMetaData(int l, InetAddress ia, MulticastAddress ma) {
	length = l;
	ipAddr = ia;
	mAddr = ma;
    }

    /**
     * MulticastTransmissionMetaData to string.
     */
    public String toString() {
	return mAddr + ": "  + ipAddr + " => " + length;
    }
}