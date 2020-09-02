// MeasurementDecoder.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.distribution;

import mon.lattice.core.data.table.TableAttribute;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.TableException;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.DefaultMMap;
import mon.lattice.core.data.list.DefaultMList;
import mon.lattice.core.data.list.MList;
import mon.lattice.core.Measurement;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.util.List;
import java.util.ArrayList;
import java.io.DataInput;
import java.io.IOException;

/**
 * Convert to a byte array to a measurement.
 */
public class MeasurementDecoder {
    // The input
    DataInput in;

    /**
     * Construct a MeasurementDecoder.
     */
    public MeasurementDecoder() {
    }

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
     * | attr count                                                             |
     * +------------------------------------------------------------------------+
     * | attr0 field no (int) | attr0 type (byte) | attr0 value (depends)       |
     * +------------------------------------------------------------------------+
     * | ....                                                                   |
     * +------------------------------------------------------------------------+
     * | attrN field no (int) | attrN type (byte) | attrN value (depends)       |
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
	

        // check if names are sent
        if (hasNames) {
            String name = in.readUTF();
        }

        ID probeID = new ID(probeIDMSB, probeIDLSB);
		
        //System.err.print(probeID + ": " + mType + " @ " + ts + ". ");

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

            // check if names are sent
            if (hasNames) {
                decodeValue(ProbeAttributeType.STRING);
            }
		    
	    // read on ProbeAttributeType code
	    ProbeAttributeType type = decodeType();

	    // System.err.print(type);
	    // System.err.print(", ");

	    // now get value
	    value = decodeValue(type);

	    // System.err.print("<");
	    // System.err.print(value);
	    // System.err.print(">");

	    // save this value
	    attrValues.add(new DefaultProbeValue(key, value));
	}

	// System.err.println();

	return new ConsumerMeasurementWithMetaData(seqNo, probeID, mType, ts, mDelta, new ID(serviceIDMSB, serviceIDLSB), new ID(groupIDMSB, groupIDLSB), attrValues);
    }

    /**
     * Decode a type
     */
    protected ProbeAttributeType decodeType() throws IOException, TypeException {
	byte typeCode = in.readByte();
	return ProbeAttributeType.fromCode(typeCode);
    }

    /**
     * Decode a value
     */
    protected Object decodeValue(ProbeAttributeType type) throws IOException, TypeException {
	Object value = null;
	byte typeCode = type.getCode();

	// we need to determine the type to read
	switch (typeCode) {
	case 'Z':
	    boolean b = in.readBoolean();
	    value = new Boolean(b);
	    break;

	case 'B':
	    byte by = in.readByte();
	    value = Byte.valueOf(by);
	    break;

	case 'C':
	    char c = in.readChar();
	    value = Character.valueOf(c);
	    break;

	case 'S':
	    short s = in.readShort();
	    value = new Short(s);
	    break;

	case 'I':
	    int i = readInt();
	    value = new Integer(i);
	    break;

	case 'J':
	    long l = in.readLong();
	    value = new Long(l);
	    break;

	case 'F':
	    float f = in.readFloat();
	    value = new Float(f);
	    break;

	case 'D':
	    double d = in.readDouble();
	    value = new Double(d);
	    break;

	case '"':
	    String str = readUTF();
	    value = str;
	    break;

	case ']':
	    int len = in.readInt();
	    byte [] array = new byte[len];
	    in.readFully(array);
	    value = array;
	    break;

	case 'T':
	    value = readTable();
	    break;

	case 'M':
	    value = readMap();
	    break;

	case 'L':
	    value = readList();
	    break;

	default:
	    // System.err.print(" ? ");
	    break;
	}

	return value;
    }

    /**
     * Read an Integer from the input.
     */
    protected int readInt() throws IOException {
	return in.readInt();
    }

    /**
     * Read a UTF strinh from the input.
     */
    protected String readUTF() throws IOException {
	return in.readUTF();
    }

    /**
     * Read a table from the input.
     */
    protected Table readTable() throws IOException, TypeException {
	// Example input might be:
	// T 3 2 pid I proc " 1234 ps 543 bash 4234 less
	// The T has already been read if we got here

	// allocate a table
	Table table = new DefaultTable();

	// get no of rows
	int rowCount = readInt();

	// get no of columns
	int colCount = readInt();

	// create table definition
	TableHeader header = new DefaultTableHeader();

	// now read colCount column definitions
	for (int col=0; col < colCount; col++) {
	    // read name
	    String name = readUTF();

	    // read type
	    ProbeAttributeType type = decodeType();

	    // add it to the header
	    header.add(name, type);
	    
	}

	// set column definitions
	table.defineTable(header);

	// now read all of the rows
	for (int row=0; row < rowCount; row++) {
	    TableRow thisRow = new DefaultTableRow();

	    // visit columns
	    for (int col=0; col < colCount; col++) {
		// find the type in the header
		TableAttribute attribute = header.get(col);
		ProbeAttributeType type = attribute.getType();

		// decode a value, 
		Object value = decodeValue(type);

		// add value to the row
		thisRow.add(value);
	    }

	    // add the row to the table
	    try {
		table.addRow(thisRow);
	    } catch (TableException te) {
		// if the data is bad it must be a transmission error
		throw new IOException(te.getMessage());
	    }
	}

	return table;

    }

    /**
     * Read a map from the input.
     */
    protected MMap readMap() throws IOException, TypeException {
	// Example output might be
	// M 4 " I one 100 two 200 three 300 four 400
	// The M has already been written out

	// Read the size
	int mapSize = readInt();

	// Read the key type
	ProbeAttributeType keyType = decodeType();

	// Read the value type
	ProbeAttributeType valueType = decodeType();

        // Allocate a map of the right type
	MMap map = new DefaultMMap(keyType, valueType);


	// now add all the values to the map
	for (int e=0; e < mapSize; e++) {
	    // decode a key
	    Object key = decodeValue(keyType);
	    // decode a value, 
	    Object value = decodeValue(valueType);

            map.put(key, value);
	}

	return map;



    }


    /**
     * Read a list from the input.
     */
    protected MList readList() throws IOException, TypeException {
	// Example input might be
	// L 4 I 100 200 300 400
	// The L has already been read if we got here

	// Read the size
	int listSize = readInt();

	// Read the type
	ProbeAttributeType type = decodeType();

	// Allocate a list of the right type
	MList list = new DefaultMList(type);

	// now add all the values to the list
	for (int e=0; e < listSize; e++) {
	    // decode a value, 
	    Object value = decodeValue(type);

	    list.add(value);
	}

	return list;
    }

    

}
