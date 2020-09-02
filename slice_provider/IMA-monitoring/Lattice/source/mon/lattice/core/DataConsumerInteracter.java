// DataSourceInteracter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.core;


/**
 * A DataSourceInteracter is responsible interacting with
 a DataSource.
 */

public interface DataConsumerInteracter {
    /**
     * Get the ControllableDataConsumer
     */
    public ControllableDataConsumer getDataConsumer();

    /**
     * Set the ControllableDataConsumer
     */
    public ControllableDataConsumer setDataConsumer(ControllableDataConsumer ds);

}