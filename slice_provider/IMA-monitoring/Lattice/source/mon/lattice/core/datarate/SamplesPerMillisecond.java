// SamplesPerMillisecond.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package mon.lattice.core.datarate;

/**
 * Specifiy a number of samples per millisecond.
 */
public class SamplesPerMillisecond extends Timing {

    public SamplesPerMillisecond(int samples) {
	super(samples * 3600 * 1000, 1);
    }
}