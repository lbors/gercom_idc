// ID.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.core;

import java.util.UUID;
import java.io.Serializable;

/**
 * An ID in RESERVOIR monitoring.
 * The actual implementation is based on a UUID
 * and so uses 128 bits.
 */
public class ID implements Serializable {
    UUID uuid;

    /**
     * Construct a ID based on an existing UUID.
     */
    ID(UUID u) {
        uuid = u;
    }

    /**
     * Construct an ID from one 64-bit value.
     * Not as unique as it could be, but sometimes useful.
     */
    public ID(long lsb) {
	uuid = new UUID(0, lsb);
    }

    /**
     * Construct an ID from two 64 bit values.
     */
    public ID(long msb, long lsb) {
	uuid = new UUID(msb, lsb);
    }


    /**
     * Creates an ID using a (pseudo randomly generated) UUID.
     * The underlying UUID is generated using a cryptographically strong
     * pseudo random number generator.
     */
    public static ID generate() {
        return new ID(UUID.randomUUID());
    }

    /**
     * Creates a ID from the string standard representation as described in the toString() method.
     */
    public static ID fromString(String name) {
        return new ID(UUID.fromString(name));
    }



    /**
     * Returns the least significant 64 bits of this ID's 128 bit value.
     */
    public long getLeastSignificantBits() {
        return uuid.getLeastSignificantBits();
    }

    /**
     * Returns the most significant 64 bits of this ID's 128 bit value.
     */
    public long getMostSignificantBits() {
        return uuid.getMostSignificantBits();
    }

    /**
     * Compares this UUID with the specified UUID.
     */
    public int compareTo(ID val) {
        return uuid.compareTo(val.uuid);
    }

    /**
     * Compares this object to the specified object.
     * The result is true if and only if the argument is not null,
     * is a ID object, has the same variant, and contains the same value, 
     * bit for bit, as this ID.
     */
    
    public boolean equals(Object val) {
        ID other = (ID) val;
        return uuid.equals(other.uuid);
    }

    /**
     * Returns a hash code for this ID.
     */
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * Returns a String object representing this ID.
     */
    public String toString() {
        // if it is a small ID show it as a long,
        // otherwise show full UUID string
        if (getMostSignificantBits() == 0) {
            return Long.toString(getLeastSignificantBits());
        } else {
            return uuid.toString();
        }
    }
    
    public UUID getUUID() {
        return this.uuid;
    }

}
