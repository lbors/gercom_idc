// MeasurementDecoderWithNames.java
// Author: Fabrizio Pastore
// Date: Feb 2010

package mon.lattice.distribution;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.core.TypeException;

/**
 * This decoder is capable to decode messages that contains the name of the values
 * 
 * @author Fabrizio Pastore
 *
 */
public class MeasurementDecoderWithNames extends MeasurementDecoder {


    /**
     * Decode the Measurement from a DataOutput object.
     * The message is encoded and it's structure is:
     * <pre>
     * +------------------------------------------------------------------------+
     * | seq no (long) | options (byte) [bit 0 : 0 = no names / 1 = with names] |
     * +------------------------------------------------------------------------+
     * |  probe id (2 X long) | type (utf string)  | timestamp (long)           |
     * +------------------------------------------------------------------------+
     * | time delta (long) | service id (2 X long) | group id (2 X long)        |
     * +------------------------------------------------------------------------+
     * | probe name (utf string) | attr count                                   |
     * +------------------------------------------------------------------------+
     * | attr0 field no (int) | attr0 name (utf string) | attr0 type (byte)     |
     * | attr0 value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * | ....                                                                   |
     * +------------------------------------------------------------------------+
     * | attrN field no (int) | attrN name (utf string) | attrN type (byte)     | 
     * | attrN value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * </pre>
     */
    public Measurement decode(DataInput in) throws IOException, TypeException {
        this.in = in;
        /* read measurement */

        // read seq no
        long seqNo = in.readLong();
		
        // options byte
        byte options = in.readByte();

        // check the options
        boolean hasNames = false;
        if ((options & 0x01) == 0x01) {
            hasNames = true;
        }

        // read probe id
	long probeIDMSB = in.readLong();
	long probeIDLSB = in.readLong();
	
        //read measurement type
        String mType = in.readUTF();

        // read timestamp
        long ts = in.readLong();

        // read measurement time delta
        long mDelta = in.readLong();

        // read the service ID of the probe
	long serviceIDMSB = in.readLong();
	long serviceIDLSB = in.readLong();
		
        // read the group ID of the probe
	long groupIDMSB = in.readLong();
	long groupIDLSB = in.readLong();
		
        // added by TOF
        // decode probe name
        String probeName = null;

        // check if names are sent
        if (hasNames) {
            probeName = in.readUTF();
        } else {
            probeName = "";
        }
		
        ID probeID = new ID(probeIDMSB, probeIDLSB);
		
        // System.err.print(probeID + ": " + mType + " @ " + ts + ". ");

        // read attributes
		
        // read count
        int attrCount = in.readInt();
        List<ProbeValue> attrValues = new ArrayList<ProbeValue>();

        // System.err.print(" [" + attrCount + "] ");

        // skip through all the attributes
        for (int attr=0; attr < attrCount; attr++) {
            // read attr key
            int key = in.readInt();
            Object value = null;

            // System.err.print(key);
            // System.err.print(" -> ");

            // get the attribute name
            String name = null;

            // check if names are sent
            if (hasNames) {
                name = (String) decodeValue(ProbeAttributeType.STRING);
            } else {
                name = "";
            }
		    
            // read on ProbeAttributeType code
            ProbeAttributeType type = decodeType();

            //System.out.println("ADDING NAME "+name+" "+key);
		    
            // System.err.print(type);
            // System.err.print(", ");

            // now get value
            value = decodeValue(type);

            // System.err.print("<");
            // System.err.print(value);
            // System.err.print(">");

            // save this value
            attrValues.add(new ProbeValueWithName(name, key, value));
        }

        // System.err.println();
	return new ConsumerMeasurementWithMetadataAndProbeName(seqNo, probeID, mType, ts, mDelta, new ID(serviceIDMSB, serviceIDLSB), new ID(groupIDMSB, groupIDLSB), attrValues, probeName);
		
    }
	
}
