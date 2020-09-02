/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im;

import java.io.IOException;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.control.agents.ControllerAgent;

/**
 *
 * @author uceeftu
 */
public interface IMPublisherNode extends IMBasicNode {
    
    /**
     * Add data for a DataConsumer
     */

    IMBasicNode addDataConsumer(ControllableDataConsumer dc) throws IOException;

    IMBasicNode addDataConsumerInfo(ControllableDataConsumer dc) throws IOException;

    /**
     * Add data for a DataSource
     */
    IMBasicNode addDataSource(DataSource ds) throws IOException;

    IMBasicNode addDataSourceInfo(DataSource ds) throws IOException;

    /**
     * Add data for a Probe.
     */
    IMBasicNode addProbe(Probe aProbe) throws IOException;
    
    IMBasicNode addProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException;

    /**
     * Add data for a Reporter.
     */

    IMBasicNode addReporter(ControllableReporter r) throws IOException;
    
    IMBasicNode addControllerAgent(ControllerAgent agent) throws IOException;
    
    /*
     * Modify stuff
     */
    IMBasicNode modifyDataSource(DataSource ds) throws IOException;

    IMBasicNode modifyProbe(Probe p) throws IOException;

    IMBasicNode modifyProbeAttribute(Probe p, ProbeAttribute pa) throws IOException;

    /*
     * Remove stuff
     */
    IMBasicNode removeDataSource(DataSource ds) throws IOException;

    IMBasicNode removeProbe(Probe aProbe) throws IOException;

    IMBasicNode removeProbeAttribute(Probe aProbe, ProbeAttribute attr) throws IOException;
    
    IMBasicNode removeDataConsumer(ControllableDataConsumer dc) throws IOException;

    IMBasicNode removeReporter(ControllableReporter r) throws IOException;
    
    IMBasicNode removeControllerAgent(ControllerAgent agent) throws IOException;
    
}
