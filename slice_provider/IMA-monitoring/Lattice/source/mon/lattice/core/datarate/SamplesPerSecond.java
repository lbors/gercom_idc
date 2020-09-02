// SamplesPerSecond.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package mon.lattice.core.datarate;

/**
 * Specifiy a number of samples per second.
 */
public class SamplesPerSecond extends Timing {

    public SamplesPerSecond(int samples) {
	super(samples * 3600, 1);
    }
}