// MeasurementMessage.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.core.plane;

import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.Probe;


/**
 * This implements the wrapping of a Measurement in the Data Plane.
 */
public class MeasurementMessage extends DataPlaneMessage {
    // The measurement
    ProbeMeasurement measurement;

    /**
     * Create a MeasurementMessage from a Measurement
     */
    public MeasurementMessage(ProbeMeasurement m) {
	measurement = m;
	type = MessageType.MEASUREMENT;
    }

    /**
     * Get the Measurement.
     */
    public ProbeMeasurement getMeasurement() {
	return measurement;
    }
}
