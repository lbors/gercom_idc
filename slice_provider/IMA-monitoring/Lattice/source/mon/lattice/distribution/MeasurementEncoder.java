// MeasurementEncoder.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.distribution;

import mon.lattice.core.data.table.TableAttribute;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableValue;
import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.MMapValue;
import mon.lattice.core.data.list.MListValue;
import mon.lattice.core.data.list.MList;
import mon.lattice.core.Measurement;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ID;
import java.util.Map;
import java.util.Set;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Convert a measurement to a byte array.
 */
public class MeasurementEncoder {
    // The Measurement
    Measurement measurement;

    // The Output
    DataOutput out;

    /**
     * Construct a MeasurementEncoder for a Measurement.
     */
    public MeasurementEncoder(Measurement m) {
	measurement = m;
    }

    /**
     * Encode the Measurement to a DataOutput object.
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
    public void encode(DataOutput out) throws IOException, TypeException {
	this.out = out;

	/* write measurement */
	//System.err.println("MeasurementEncoder: in encode()");

	// write seq no
	out.writeLong(measurement.getSequenceNo());
        // write options byte
        // bit 0 is zero to indicate no names
        out.writeByte((byte)0);
	// write probe id
        ID pid = measurement.getProbeID();
	out.writeLong(pid.getMostSignificantBits());
	out.writeLong(pid.getLeastSignificantBits());
	// write measurement type
        out.writeUTF(measurement.getType());
	// write timestamp
        out.writeLong(measurement.getTimestamp().value());
	// write measurement delta
        out.writeLong(measurement.getDeltaTime().value());
	// write out the service ID
        ID sid = measurement.getServiceID();
	out.writeLong(sid.getMostSignificantBits());
	out.writeLong(sid.getLeastSignificantBits());
	// write out the group ID
        ID gid = measurement.getGroupID();
	out.writeLong(gid.getMostSignificantBits());
	out.writeLong(gid.getLeastSignificantBits());

	// write attributes
	
	// count first
	int attrCount = measurement.getValues().size();
	out.writeInt(attrCount);

	//System.err.print(" [" + attrCount + "] ");
	
	// skip through all the attributes
	for (ProbeValue attr : measurement.getValues()) {
	    // write attr
	    int field = attr.getField();

	    // first write out the attribute field as an integer
	    out.writeInt(field);

	    // then write the type
	    Object value = attr.getValue();
	    ProbeAttributeType type = ProbeAttributeType.lookup(value);

	    //System.err.println("Encoding " + type + " Actual: " + value.getClass().getName());

	    encodeType(type);

	    // System.err.print((char)type.getCode());
	    // System.err.print(", ");

	    // and now the value
	    encodeValue(value, type);
	}

	// System.err.println();
    }

    /**
     * Encode a type
     */
    protected void encodeType(ProbeAttributeType type) throws IOException {	
	out.writeByte(type.getCode());
    }

    /**
     * Encode a value of a given type
     */
    protected void encodeValue(Object value, ProbeAttributeType type) throws IOException {	
	    // we need to determine which type to write out
	    switch (type) {

	    case BOOLEAN:
		out.writeBoolean(((Boolean)value).booleanValue());
		break;

	    case BYTE:
		out.writeByte(((Byte)value).byteValue());
		break;

	    case CHAR:
		out.writeChar(((Character)value).charValue());
		break;

	    case SHORT:
		out.writeShort(((Short)value).shortValue());
		break;

	    case INTEGER:
		writeInt(value);
		break;

	    case LONG:
		out.writeLong(((Long)value).longValue());
		break;

	    case FLOAT:
		out.writeFloat(((Float)value).floatValue());
		break;

	    case DOUBLE:
		out.writeDouble(((Double)value).doubleValue());
		break;

	    case STRING:
		writeUTF(value);
		break;

	    case BYTES:
		// write out no of byte first
		// so the decoder knows how many to get
		byte [] array = (byte[])value;
		out.writeInt(array.length);
		out.write(array);
		break;

	    case TABLE:
		// write out a table value
		writeTable(value);
		break;

	    case MAP:
		// write out a map value
		writeMap(value);
		break;

	    case LIST:
		// write out a list value
		writeList(value);
		break;

	    default:
		throw new Error("Unknown ProbeAttributeType: " + type);
		// unreachable break;
	    }
    }

    /**
     * Write an integer to the output.
     */
    protected void writeInt(Object value) throws IOException {
	out.writeInt(((Integer)value).intValue());
    }

    /**
     * Write a UTF string to the output.
     */
    protected void writeUTF(Object value) throws IOException {
	out.writeUTF((String)value);
    }

    /**
     * Write a table to the output.
     */
    protected void writeTable(Object value) throws IOException {
	// Example output might be
	// T 3 2 pid I proc " 1234 ps 543 bash 4234 less
	// The T has already been written out

	Table table = (Table)value;

	// write out no of rows in table
	int rowCount = table.getRowCount();
	writeInt(rowCount);

	// write out no of columns in each row
	int colCount = table.getColumnCount();
	writeInt(colCount);

	// write out a list of all the types
	TableHeader header = table.getColumnDefinitions();

	// write out the names and types
	for (int col=0; col < colCount; col++) {
	    TableAttribute attribute = header.get(col);

	    // write name
	    writeUTF(attribute.getName());

	    // write type
	    encodeType(attribute.getType());
	}

	// now write out the values
	// visit rows
	for (int row=0; row < rowCount; row++) {
	    TableRow thisRow = table.getRow(row);

	    // visit columns
	    for (int col=0; col < colCount; col++) {
		TableValue element = thisRow.get(col);

		// write element value
		// the decoder can determine the type from the header
		encodeValue(element.getValue(), element.getType());
	    }
	}
	
    }


    /**
     * Write a map to the output
     */
    protected void writeMap(Object value) throws IOException {
	// Example output might be
	// M 4 " I one 100 two 200 three 300 four 400
	// The M has already been written out

        MMap map = (MMap)value;


	// write out no of elements in the map
	int count = map.size();
	writeInt(count);


	// write out type of the keys
	encodeType(map.getKeyType());

	// write out type of the values
	encodeType(map.getValueType());

        // Get all the entries in the Map
        Set<Map.Entry<MMapValue, MMapValue>> entrySet = map.toMap().entrySet();

        // now write out the mappings
        for (Map.Entry<MMapValue, MMapValue> entry : entrySet) {
            MMapValue eKey = entry.getKey();
            MMapValue eValue = entry.getValue();

            encodeValue(eKey.getValue(), eKey.getType());
            encodeValue(eValue.getValue(), eValue.getType());
        }

    }


    /**
     * Write a list to the output.
     */
    protected void writeList(Object value) throws IOException {
	// Example output might be
	// L 4 I 100 200 300 400
	// The L has already been written out

	MList list = (MList)value;

	// write out no of elements of list
	int elemCount = list.size();
	writeInt(elemCount);

	// write out type of the elements
	encodeType(list.getType());

	// now write out the elements
	for (int e=0; e < elemCount; e++) {
	    MListValue element = list.get(e);

	    // write element value
	    encodeValue(element.getValue(), element.getType());
	}
    }

    
	
}
