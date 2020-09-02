// MMap.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2012

package mon.lattice.core.data.map;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.io.Serializable;

/**
 * An interface for a Map that can be used as a ProbeAttributeType.
 * This is called MMap (meaning Measurment Map) to differentiate it
 * from java's builtin Map type.
 */
public class DefaultMMap implements MMap {
    // The key type
    ProbeAttributeType keyType;

    // The value type
    ProbeAttributeType valueType;

    // A Map of MMapValues that are part of this Map
    Map<MMapValue, MMapValue> theMap;

   /**
    * Construct a MMap.
    */
    public DefaultMMap(ProbeAttributeType keyType, ProbeAttributeType valueType) {
	theMap = new HashMap<MMapValue, MMapValue>();
        this.keyType = keyType;
        this.valueType = valueType;
    }


    /**
     * Get the size of the map
     */
    public int size() {
        return theMap.size();
    }


    /**
     * Is the map empty.
     */
    public boolean isEmpty() {
        return theMap.isEmpty();
    }


    /**
     * Does the map contain a key
     */
    public boolean containsKey(MMapValue key) throws TypeException{
	ProbeAttributeType keyType = key.getType();

	if (keyType.equals(this.keyType)) {
            return (theMap.containsKey(key));
	} else {
	    throw new TypeException("MMapValue key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Does the map contain a key
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public boolean containsKey(Object key) throws TypeException{
	ProbeAttributeType keyType = ProbeAttributeType.lookup(key);

	if (keyType.equals(this.keyType)) {
            return (theMap.containsKey(new DefaultMMapValue(key)));
	} else {
	    throw new TypeException("Object key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Does the map contain a value
     */
    public boolean containsValue(MMapValue value) throws TypeException {
	ProbeAttributeType valueType = value.getType();

	if (valueType.equals(this.valueType)) {
            return (theMap.containsValue(value));
	} else {
	    throw new TypeException("MMapValue value: '" + value +
				    "' cannot be of type " + valueType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Does the map contain a value
     * This throws a TypeException if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public boolean containsValue(Object value) throws TypeException {
	ProbeAttributeType valueType = ProbeAttributeType.lookup(value);

	if (valueType.equals(this.valueType)) {
            return (theMap.containsValue(new DefaultMMapValue(value)));
	} else {
	    throw new TypeException("Object value: '" + value +
				    "' cannot be of type " + valueType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }


    /**
     * Lookup a value for a specific key.
     */
    public MMapValue get(MMapValue key) throws TypeException {
	ProbeAttributeType keyType = key.getType();

	if (keyType.equals(this.keyType)) {
            return (theMap.get(key));
	} else {
	    throw new TypeException("MMapValue key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Lookup a value for a specific key.
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public MMapValue get(Object key) throws TypeException {
	ProbeAttributeType keyType = ProbeAttributeType.lookup(key);

	if (keyType.equals(this.keyType)) {
            return (theMap.get(new DefaultMMapValue(key)));
	} else {
	    throw new TypeException("Object key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Put a value in the MMap given a specific key.
     */
    public MMap put(MMapValue key, MMapValue value) throws TypeException {
	ProbeAttributeType keyType = key.getType();

	ProbeAttributeType valueType = value.getType();

        // check key type
	if (!keyType.equals(this.keyType)) {
	    throw new TypeException("MMapValue key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}


        // check value type
	if (!valueType.equals(this.valueType)) {
	    throw new TypeException("MMapValue value: '" + value +
				    "' cannot be of type " + valueType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

        theMap.put(key, value);

        return this;
    }

    /**
     * Put a value in the MMap given a specific key.
     * This throws a TypeException if the type of the key or the type
     * of the value is not one supported by ProbeAttributeType.
     */
    public MMap put(Object key, Object value) throws TypeException {
	ProbeAttributeType keyType = ProbeAttributeType.lookup(key);

	ProbeAttributeType valueType = ProbeAttributeType.lookup(value);

        // check key type
	if (!keyType.equals(this.keyType)) {
	    throw new TypeException("Object key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}


        // check value type
	if (!valueType.equals(this.valueType)) {
	    throw new TypeException("Object value: '" + value +
				    "' cannot be of type " + valueType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

        theMap.put(new DefaultMMapValue(key), new DefaultMMapValue(value));

        return this;
    }


    /**
     * Remove a value from the MMap given a specific key.
     */
    public MMapValue remove(MMapValue key) throws TypeException {
	ProbeAttributeType keyType = key.getType();

	if (keyType.equals(this.keyType)) {
            return (theMap.remove(key));
	} else {
	    throw new TypeException("MMapValue key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}


    }

    /**
     * Remove a value from the MMap given a specific key.
     * This throws a TypeException if the type of the key is not
     * one supported by ProbeAttributeType.
     */
    public MMapValue remove(Object key) throws TypeException {
	ProbeAttributeType keyType = ProbeAttributeType.lookup(key);

	if (keyType.equals(this.keyType)) {
            return (theMap.remove(new DefaultMMapValue(key)));
	} else {
	    throw new TypeException("Object key: '" + key +
				    "' cannot be of type " + keyType +
				    " in MMap of type " + this.keyType + " -> " + this.valueType);
	}

    }

    /**
     * Return a set view of the keys.
     */
    public Set<MMapValue> keySet(){
        return theMap.keySet();
    }

    /**
     * Return the type of the keys.
     */
    public ProbeAttributeType getKeyType() {
        return keyType;
    }

    /**
     * Returns a collection view of the values contained in this map.  
     */
    public Collection<MMapValue> values(){
        return theMap.values();
    }

    /**
     * Return the type of the values.
     */
    public ProbeAttributeType getValueType() {
        return valueType;
    }


    /**
     * Convert the MMap to a java.util.Map.
     */
    public Map<MMapValue, MMapValue> toMap(){
        return theMap;
    }

    /**
     * Equals
     */
    public boolean equals(Object obj) {
        return theMap.equals(obj);
    }

    /**
     * toString
     */
    public String toString() {
        return theMap.toString();
    }
}
