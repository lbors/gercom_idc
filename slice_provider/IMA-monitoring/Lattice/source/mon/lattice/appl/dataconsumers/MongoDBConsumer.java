// MongoDBConsumer.java
// 

package mon.lattice.appl.dataconsumers;

import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.Reporter;
import mon.lattice.core.AbstractDataConsumer;
import mon.lattice.appl.reporters.MongoDBReporter;
import mon.lattice.appl.reporters.PrintReporter;
import mon.lattice.appl.reporters.ReporterException;


/**
 * A BasicConsumer is an object that is used in application
 * level code.  It has the necessary functionality to act as a consumer
 * and have plugins for each of the data plane, control plane, and
 * info plane.
 */
@Deprecated
public class MongoDBConsumer extends AbstractDataConsumer implements MeasurementReceiver {

    /**
     * Construct a BasicConsumer.
     */
    public MongoDBConsumer(String dbAddr, int dbPort, String dbName, String collectionName) throws ReporterException {
        Reporter printReporter =  new PrintReporter();
        Reporter mongoReporter = new MongoDBReporter(dbAddr, dbPort, dbName, collectionName);
        
        addReporter(printReporter);
        addReporter(mongoReporter);
    }
}
