// TableProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.core.data.table;

import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.AbstractProbeAttribute;

/**
 * An implementation for having a table as a ProbeAttribute
 * These are a Probe's Data Dictionary.
 */
public class TableProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    /*
     * The TableHeader acts as the definition.
     */
    TableHeader definition;

    /**
     * Construct a ProbeAttribute.
     */
    public TableProbeAttribute(int field, String name,  TableHeader definition) {
	super(field, name, ProbeAttributeType.TABLE, "_TABLE_");
	this.definition = definition;
    }

}