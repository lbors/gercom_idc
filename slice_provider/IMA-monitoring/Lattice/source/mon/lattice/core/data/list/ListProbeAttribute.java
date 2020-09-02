// ListProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package mon.lattice.core.data.list;

import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.AbstractProbeAttribute;

/**
 * An implementation for having a list as a ProbeAttribute
 * These are a Probe's Data Dictionary.
 */
public class ListProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    // The type of the elements
    ProbeAttributeType elementType;

    /**
     * Construct a ProbeAttribute.
     */
    public ListProbeAttribute(int field, String name, ProbeAttributeType type) {
	super(field, name, ProbeAttributeType.LIST, "_LIST_");
	this.elementType = type;
    }

}