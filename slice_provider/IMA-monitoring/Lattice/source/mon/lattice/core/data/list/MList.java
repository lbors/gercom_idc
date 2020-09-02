// List.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package mon.lattice.core.data.list;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import java.util.List;
import java.io.Serializable;

/**
 * An interface for a List that can be used as a ProbeAttributeType.
 * This is called MList (meaning Measurment List) to differentiate it
 * from java's builtin List type.
 */
public interface MList extends Serializable {

    /**
     * Get the size of the list
     */
    public int size();

    /**
     * Get the Nth element from a list.
     */
    public MListValue get(int n);

    /**
     * Set the Nth element of a list.
     */
    public MList set(int n, MListValue value) throws TypeException;

    /**
     * Add a  value to the list.
     */
    public MList add(MListValue value) throws TypeException;

    /**
     * Add an object value to the list.
     * This throws a TypeException if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public MList add(Object value) throws TypeException;

    /**
     * Get the type of the elements for a MList.
     * e.g. INTEGER
     */
    public ProbeAttributeType getType();

    /**
     * Convert the List to a java.util.List.
     */
    List<MListValue> toList();
}
