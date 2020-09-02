package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A PrintReporter just prints a Measurement.
 */
public final class LoggerReporter1 extends AbstractReporter {
    /**
     * In a LoggerReporter, report() just logs the Measurement to the log file.
     */
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerReporter1.class);
    
    
    public LoggerReporter1(String reporterName) {
        super(reporterName); 
    }
    
    
    @Override
    public void report(Measurement m) {
	LOGGER.info(m.toString());
    }
}