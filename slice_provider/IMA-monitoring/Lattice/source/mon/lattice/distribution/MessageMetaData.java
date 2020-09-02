// MessageMetaData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.distribution;

import mon.lattice.core.ID;
import mon.lattice.core.plane.MessageType;
import java.io.Serializable;

/**
 * Information about a Measurement Message.
 * Includes: seq no
 */
public class MessageMetaData implements MetaData, Serializable {
    public final ID dataSourceID;
    public final int seqNo;
    public final MessageType type;

    /**
     * Construct a MessageMetaData object.
     */
    public MessageMetaData(ID dsID, int sn, MessageType t) {
	dataSourceID = dsID;
	seqNo = sn;
	type = t;
    }

    /**
     * MessageMetaData to string.
     */
    public String toString() {
	return seqNo + ": " + type;
    }
}