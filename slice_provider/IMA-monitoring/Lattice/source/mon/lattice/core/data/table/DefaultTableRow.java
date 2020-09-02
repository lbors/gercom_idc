// DefaultTableRow.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.core.data.table;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import java.util.List;
import java.util.LinkedList;

/**
 * A default implementation of a TableRow.
 */
public class DefaultTableRow implements TableRow {
    // A list of TableValues that are part of this row
    List<TableValue> values;

   /**
    * Construct a TableRow.
    * It is done w.r.t the Table that the row will be in.
    */
    public DefaultTableRow() {
	values = new LinkedList<TableValue>();
    }

    /**
     * Get the size of the row.
     * It should be the same size as the number of column 
     * definitions for the table that the row is in.
     */
    public int size() {
	return values.size();
    }

    /**
     * Get the Nth element from a row.
     */
    public TableValue get(int n) {
	return values.get(n);
    }

    /**
     * Set the Nth element of a row.
     */
    public TableRow set(int n, TableValue value) {
	// set value in Nth position
	values.set(n, value);
	return this;
    }

    /**
     * Add some data value to the row.
     */
    public TableRow add(TableValue value) {
	int n = size();
	values.add(n, value);
	return this;
    }

    /**
     * Add an object value to the row.
     * This throws an Error if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public TableRow add(Object value) throws TypeException {
	ProbeAttributeType valueType = ProbeAttributeType.lookup(value);
	if (valueType != null) {
	    int n = size();
	    values.add(n, new DefaultTableValue(value));
	    return this;
	} else {
	    throw new TypeException("Value at position: " + size() +
				    " cannot be of type " + valueType);
	}
    }

    /**
     * Convert the Row to a List.
     */
    public List<TableValue> toList() {
	return values;
    }


    /**
     * Equals for a DefaultTableRow.
     */
    public boolean equals(Object obj) {
        // It is the equals() of the list
        if (obj instanceof DefaultTableRow) {
            DefaultTableRow other = (DefaultTableRow)obj;
            return values.equals(other.values);
        } else {
            return false;
        }
    }

    /**
     * The hashCode for a DefaultTableRow.
     */
    public int hashCode() {
        // It is the hashCode() of the list
        return values.hashCode();
    }

    /**
     * To string
     */
    public String toString() {
	return values.toString();
    }

}
