// MMap.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2012

package mon.lattice.core.data.map;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.io.Serializable;

/**
 * An interface for a Map that can be used as a ProbeAttributeType.
 * This is called MMap (meaning Measurment Map) to differentiate it
 * from java's builtin Map type.
 */
public interface MMap extends Serializable {
    /**
     * Get the size of the map
     */
    public int size();


    /**
     * Is the map empty.
     */
    public boolean isEmpty();


    /**
     * Does the map contain a key
     */
    public boolean containsKey(MMapValue key) throws TypeException;

    /**
     * Does the map contain a key
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public boolean containsKey(Object key) throws TypeException;

    /**
     * Does the map contain a value
     */
    public boolean containsValue(MMapValue value) throws TypeException;

    /**
     * Does the map contain a value
     * This throws a TypeException if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public boolean containsValue(Object value) throws TypeException;


    /**
     * Lookup a value for a specific key.
     */
    public MMapValue get(MMapValue key) throws TypeException;

    /**
     * Lookup a value for a specific key.
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public MMapValue get(Object key) throws TypeException;

    /**
     * Put a value in the MMap given a specific key.
     */
    public MMap put(MMapValue key, MMapValue value) throws TypeException;

    /**
     * Put a value in the MMap given a specific key.
     * This throws a TypeException if the type of the key or the type
     * of the value is not one supported by ProbeAttributeType.
     */
    public MMap put(Object key, Object value) throws TypeException;


    /**
     * Remove a value from the MMap given a specific key.
     */
    public MMapValue remove(MMapValue key) throws TypeException;

    /**
     * Remove a value from the MMap given a specific key.
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public MMapValue remove(Object key) throws TypeException;

    /**
     * Return a set view of the keys.
     */
    public Set<MMapValue> keySet();

    /**
     * Return the type of the keys.
     */
    public ProbeAttributeType getKeyType();

    /**
     * Returns a collection view of the values contained in this map.  
     */
    public Collection<MMapValue> values();

    /**
     * Return the type of the values.
     */
    public ProbeAttributeType getValueType();


    /**
     * Convert the MMap to a java.util.Map.
     */
    Map<MMapValue, MMapValue> toMap();
}
