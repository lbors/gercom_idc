// UDPTransmissionMetaData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package mon.lattice.distribution.udp;

import mon.lattice.distribution.MetaData;
import java.net.InetAddress;
import java.io.Serializable;

/**
 * Information about a transmission.
 * Includes: packet length, src ip address, dst ip address
 */
public class UDPTransmissionMetaData implements MetaData, Serializable {
    public final int length;
    public final InetAddress srcIPAddr;
    public final InetAddress dstIPAddr;
    public int srcPort = -1;
    
    /**
     * Construct a UDPTransmissionMetaData object.
     */
    public UDPTransmissionMetaData(int l, InetAddress sia, InetAddress dia) {
	length = l;
	srcIPAddr = sia;
	dstIPAddr = dia;
    }
    
    public UDPTransmissionMetaData(int l, InetAddress sia, InetAddress dia, int port) {
        this(l, sia, dia);
        srcPort = port;
    }

    /**
     * UDPTransmissionMetaData to string.
     */
    public String toString() {
	return dstIPAddr + ": "  + srcIPAddr + ":" + srcPort + " => " + length;
    }
}