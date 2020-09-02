// QueueLengthProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.demo;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.Rational;
import eu.reservoir.monitoring.appl.probes.vee.KPIProbe;
import java.util.List;
import java.util.ArrayList;


/**
 * A probe for gettting the queue length.
 * <p>
 * It needs to emulate a REST agent that sends:
 * <pre>
 * <MonitoringInformation> 
 *   <EventType>Agent</EventType> 
 *   <EpochTimestamp>25547674</EpochTimestamp> 
 *   <TimeDelta>0</TimeDelta> 
 *   <FQN>sun.services.sge.kpis.queueSize</FQN> 
 *   <Value>45</Value> 
 * </MonitoringInformation>
 * </pre>
 * <p>
 * A measurement already has:  a timestamp, a time delta, and a type.
 * For this probe we will send the queue length, and the FQN.
 * 
 */
public class QueueLengthProbe extends KPIProbe implements Probe  {
    /*
     * Construct a probe
     */
    public QueueLengthProbe(String fqn) {
        // call super constructor
        super(fqn);

	// set name
        setName(fqn);

	// data rate is 360 measurements per hour
        setDataRate(new Rational(360, 1));

        // add a KPI value
        addKPI("queueLength", ProbeAttributeType.INTEGER, "n");

    }

    /**
     * Collect KPI values for measurement.
     */
    public List<Object> collectKPIValues() {
	try {
	    ArrayList<Object> list = new ArrayList<Object>(1);

	    // make up a queue length
	    int queueLength = (int)(Math.random()*100);

	    // add queueLength to list
	    list.add(new Integer(queueLength));

	    return list;
	} catch (Exception e) {
	    return null;
	}
    }

}