// CPUInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

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
import java.util.*;

/**
 * A probe to get cpu info on a Linux system.
 * It uses /proc/stat to read the underyling data.
 */
public class CPUInfo extends AbstractProbe implements Probe  {
    // A CPUDev object that reads info about the CPU.
    CPUDev cpuDev;

    /*
     * Construct a CPUInfo probe
     */
    public CPUInfo(String name) {
	setName(name);
        setDataRate(new Rational(360, 1));

        // allocate cpuDev
        cpuDev = new CPUDev();

	// read data, but calculate nothing
	cpuDev.read(false);


	// determine actual attributes
	// skip through all keys of last read() to determine the attributes
	int field = 0;
	// sort the keys for later processing
	ArrayList<String> keyList = new ArrayList<String>(cpuDev.dataKeys());
	Collections.sort(keyList);

        String currentCPU = "";

	for (String key : keyList) {
            // current cpu name
            String[] parts = key.split("-");
            String cpuName = parts[0];


            // if we get to a new CPU add a total field
            if (! currentCPU.equals(cpuName)) {
                currentCPU = cpuName;
		// add total
		addProbeAttribute(new DefaultProbeAttribute(field, cpuName+"-total", ProbeAttributeType.INTEGER, "n"));
		field++;
	    }
	    
	    addProbeAttribute(new DefaultProbeAttribute(field, key, ProbeAttributeType.FLOAT, "percent"));
	    field++;
	}
    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
    }

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list the size of the thisdelta map
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(cpuDev.getDeltaSize() / 4 * 5);

	// read the data
	if (cpuDev.read(true)) {
	    try {
		// now collect up the results	
		int field = 0;
		// sort the keys
		ArrayList<String> keyList = new ArrayList<String>(cpuDev.deltaKeys());
		Collections.sort(keyList);

                String currentCPU = "";

		for (String key : keyList) {
                    // current cpu name
                    String[] parts = key.split("-");
                    String cpuName = parts[0];

                    // if we get to a new CPU add a total field
                    if (! currentCPU.equals(cpuName)) {
                        currentCPU = cpuName;
                        // add total
			list.add(new DefaultProbeValue(field, cpuDev.getTotalValue(cpuName)));
			field++;
		    }

		    list.add(new DefaultProbeValue(field, cpuDev.getDeltaValue(key)));
		    field++;
		}
	    
		return new ProducerMeasurement(this, list, "CPUInfo");	
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/stat");
	    return null;
	}
    }

}
