// MemoryDev.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2010

package mon.lattice.appl.probes.host.linux;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A class used to get memory info on a Linux system.
 * It uses /proc/meminfo to read the underyling data.
 */
public class MemoryDev {
    // The /proc/meminfo file
    File procmeminfo = new File("/proc/meminfo");

    // A map of values for the probe
    Map<String, Integer> values = new HashMap<String, Integer>();

    /*
     * Construct a MemoryDev object.
     */
    public MemoryDev() {
    }

    /**
     * Get current value for a particular element of data.
     */
    public Integer getCurrentValue(String key) {
        return values.get(key);
    }


    /**
     * Read some data from /proc/meminfo
     */
    public boolean read() {
	values.clear();
	if (readProcMeminfo(procmeminfo)) {
	    return true;
	} else {
	    return false;
	}
    }


    /**
     * Read from /proc/meminfo
     *
     * Looking for
     * MemTotal:      6841344 kB
     * MemFree:       3463280 kB
     * Buffers:        109420 kB
     * Cached:        2512396 kB

     */
    private boolean readProcMeminfo(File procmeminfo) {
	String line;
	int count = 0;
	final int needed = 4;  // we are looking for 4 fields

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procmeminfo));

	    // find all required
	    while ((line = reader.readLine()) != null) {
		if (line.startsWith("MemTotal") || line.startsWith("MemFree") ||
		    line.startsWith("Buffers") || line.startsWith("Cached")) {
		    // it's required info

		    // split gives ["MemFree", "    3463280 kB"]
		    String[] parts = line.split(":");

		    // split gives ["3463280", "kB"]
		    String[] kb = parts[1].trim().split(" ");

		    // put ["MemFree", 3463280]
		    Integer value = Integer.valueOf(kb[0].trim());
		    values.put(parts[0], value);
		    
		    // seen one more
		    count++;
		}

		// check to see if we're finished
		if (count == needed) {
		    // we've reached the end of the fields we need
		    // so we close
		    reader.close();
		    break;
		}
	    }

	    return true;
	} catch (Exception e) {
	    // something went wrong
	    return false;
	}
    }

}
