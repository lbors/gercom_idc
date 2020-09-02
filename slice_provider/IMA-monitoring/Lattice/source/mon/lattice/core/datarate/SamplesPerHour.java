// SamplesPerHour.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package mon.lattice.core.datarate;

/**
 * Specifiy a number of samples per hour.
 */
public class SamplesPerHour extends Timing {

    public SamplesPerHour(int samples) {
	super(samples, 1);
    }
}