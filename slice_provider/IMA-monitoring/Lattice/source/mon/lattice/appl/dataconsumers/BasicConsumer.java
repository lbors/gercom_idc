// BasicConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.appl.dataconsumers;

import mon.lattice.appl.reporters.PrintReporter;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractDataConsumer;
import java.io.IOException;

/**
 * A BasicConsumer is an object that is used in application 
 * level code.  It has the necessary functionality to act as a consumer
 * and have plugins for each of the data plane, control plane, and
 * info plane.
 */

public class BasicConsumer extends AbstractDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public BasicConsumer() {
	// The default way to report a measurement is to print it
	Reporter reporter =  new PrintReporter();
        
	addReporter(reporter);
    }

}