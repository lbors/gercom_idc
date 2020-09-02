// ReporterMeasurementType.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: March 2014

package mon.lattice.core;

import java.util.List;

/**
 * An interface for Reporters that indicate the Measurement they can report on.
 */
public interface ReporterMeasurementType  {
    /**
     * The Measurement types this Reporter will accept
     */
    public List<String> getMeasurementTypes();

}
