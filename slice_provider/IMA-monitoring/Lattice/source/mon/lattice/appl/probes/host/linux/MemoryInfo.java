// MemoryInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sep 2009

package mon.lattice.appl.probes.host.linux;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.Rational;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.ArrayList;

/**
 * A probe to get memory info on a Linux system.
 * It uses /proc/meminfo to read the underyling data.
 */
public class MemoryInfo extends AbstractProbe implements Probe  {
    // A MemoryDev object that reads info about memory 
    MemoryDev memDev;

    /*
     * Construct a MemoryInfo probe
     */
    public MemoryInfo(String name) {
	setName(name);
        setDataRate(new Rational(360, 1));

        // allocate a MemoryDev
        memDev = new MemoryDev();

        addProbeAttribute(new DefaultProbeAttribute(0, "total", ProbeAttributeType.INTEGER, "kilobytes"));
	addProbeAttribute(new DefaultProbeAttribute(1, "free", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(2, "used", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "reallyused", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(4, "cached", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "buffers", ProbeAttributeType.INTEGER, "kilobytes"));
 
   }



    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list the size of the no of attributes
	int attrCount = 6;   // probeAttributes.size();
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(attrCount);

	// read the data
	if (memDev.read()) {
	    // the relevant data will be in the values map
	    try {
		int memTotal = memDev.getCurrentValue("MemTotal");
		int memFree = memDev.getCurrentValue("MemFree");
		int cached = memDev.getCurrentValue("Cached");
		int buffers = memDev.getCurrentValue("Buffers");

		int used = memTotal - memFree;
		int reallyUsed = used - (cached + buffers);

                /*
		System.err.println("memoryInfo => " +
				       " total = " + memTotal +
				       " free = " + memFree +
				       " used = " + used +
				       " reallyUsed = " + reallyUsed);
                */

		// now collect up the results	
		list.add(new DefaultProbeValue(0, memTotal));
		list.add(new DefaultProbeValue(1, memFree));
		list.add(new DefaultProbeValue(2, used));
		list.add(new DefaultProbeValue(3, reallyUsed));
		list.add(new DefaultProbeValue(4, cached));
		list.add(new DefaultProbeValue(5, buffers));
	    
		return new ProducerMeasurement(this, list, "MemInfo");	
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/stat");
	    return null;
	}
    }


}

