// TableValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.core.data.table;

import mon.lattice.core.ProbeAttributeType;
import java.io.Serializable;

/**
 * An interface for a value in a Table.
 */
public interface TableValue extends Serializable {
    /**
     * Get the actual value.
     */
    public Object getValue();

    /**
     * Get the type for a TableAttribute.
     * e.g. INTEGER
     */
    public ProbeAttributeType getType();
}