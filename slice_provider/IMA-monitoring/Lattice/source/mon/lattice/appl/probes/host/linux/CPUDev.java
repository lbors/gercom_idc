// CPUDev.java
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
 * A class used to get  cpu info on a Linux system.
 * It uses /proc/stat to read the underyling data.
 */
public class CPUDev {
    // The /proc/stat file
    File procstat = new File("/proc/stat");

    // a map of the last snapshot from /proc/stat
    HashMap<String, Integer> lasttime = new HashMap<String, Integer>();
    // a map of the current delta values from /proc/stat
    HashMap<String, Float> thisdelta = new HashMap<String, Float>();
    //a map of the total usage for each cpu
    HashMap<String, Integer> lasttotal = new HashMap<String, Integer>();

    // no of cpus
    int cpuCount = 0;

    // last timestamp
    long lastSample = 0;

    /**
     * Construct a CPUDev object.
     */
    public CPUDev() {
    }

    /**
     * Get the no of cpus on the host.
     */
    public int getCPUCount() {
        return cpuCount;
    }

    /**
     * Get the list of keys based on data read in.
     */
    public Set<String> dataKeys() {
        return lasttime.keySet();
    }

    /**
     * Get current value for a particular element of data.
     */
    public Integer getCurrentValue(String key) {
        return lasttime.get(key);
    }

    /**
     * Get the size of the delta map.
     */
    public int getDeltaSize() {
        return thisdelta.size();
    }

    /**
     * Get the list of keys in the delta map.
     */
    public Set<String> deltaKeys() {
        return thisdelta.keySet();
    }


    /**
     * Get delta value, the difference between the current values
     * and the previous value, for a particular element of data.
     */
    public Float getDeltaValue(String key) {
        return thisdelta.get(key);
    }

    /**
     * Get total value for a particular element of data.
     */
    public Integer getTotalValue(String key) {
        return lasttotal.get(key+"-total");
    }


    /**
     * Read some data from /proc/stat.
     * If calculate is true, then calculate the deltas between 
     * this read and the last read.
     */
    public boolean read(boolean calculate) {
        // time when read
        long now = System.currentTimeMillis();

	List<String> results = readProcStat(procstat);

	if (results == null) {
            lastSample = now;
	    return false;

	} else {

	    for (String infoLine : results) {
		String[] parts = infoLine.split(" ");

		String cpu = parts[0];
		int userN = Integer.parseInt(parts[1]);
		int niceN = Integer.parseInt(parts[2]);
		int systemN = Integer.parseInt(parts[3]);
		int idleN = Integer.parseInt(parts[4]);

		int total = userN + niceN + systemN + idleN;


                /*
		System.err.println("data => " + cpu +
                 " calculate = " + calculate +
		 " user = " + userN +
		 " nice = " + niceN +
		 " system = " + systemN +
		 " idle = " + idleN +
                 " total = " + total);
                */


		// determine if we need to calculate the deltas
		// from the raw data
		if (calculate) {  // as a %age
                    // determine millis since last read
                    int millis = (int)(now - lastSample);

                    // timeout is in milliseconds, jiffies is in 100ths
                    int jiffies =  millis / 10;

                    //if (jiffies == 0) { jiffies = 1; }
                    jiffies++;

		    int userDiff = userN - (Integer)lasttime.get(cpu+"-user");
		    int niceDiff = niceN - (Integer)lasttime.get(cpu+"-nice");
		    int systemDiff = systemN - (Integer)lasttime.get(cpu+"-system");
		    int idleDiff = idleN - (Integer)lasttime.get(cpu+"-idle");

                    /*
		    System.err.println("cpuInfo => " + cpu + ":" +
                                       // " millis = " + millis +
                                       //" jiffies = " + jiffies +
				       " user = " + userDiff +
				       " nice = " + niceDiff +
				       " system = " + systemDiff +
				       " idle = " + idleDiff);
                    */

                    // readings are in jiffies, so we convert them to %age
		    thisdelta.put(cpu+"-user", (float) (((float)userDiff) / jiffies * 100));
		    thisdelta.put(cpu+"-nice", (float) (((float)niceDiff) / jiffies * 100));
		    thisdelta.put(cpu+"-system", (float) (((float)systemDiff) / jiffies * 100));
		    thisdelta.put(cpu+"-idle", (float) (((float)idleDiff) / jiffies * 100));
		}

		lasttime.put(cpu+"-user", userN);
		lasttime.put(cpu+"-nice", niceN);
		lasttime.put(cpu+"-system", systemN);
		lasttime.put(cpu+"-idle", idleN);
		lasttotal.put(cpu+"-total", total);
	    }


            // save timestamp
            lastSample = now;
            return true;

	}


    }

    /**
     * Read from /proc/stat
     */
    private List<String> readProcStat(File procstat) {
	LinkedList<String> cpuInfo = new LinkedList<String>();
	String line;

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procstat));

	    // find all lines starting with cpu
	    while ((line = reader.readLine()) != null) {
		if (line.startsWith("cpu")) {
		    // it's cpu info
		    cpuInfo.add(line);
		} else {
		    // we've reached the end of the cpu stat lines
		    // so we close
		    reader.close();
		    break;
		}
	    }
	} catch (Exception e) {
	    // something went wrong
	    return null;
	}

	// now we do a bit of post processing
	if (cpuInfo.size() == 1) {
	    // there is only one cpu, so return the info
	    cpuCount = 1;
	    return cpuInfo;
	} else {
	    // there is more than one.
	    // the first entry is a summation, so we drop it
	    cpuCount = cpuInfo.size() - 1;
	    cpuInfo.remove(0);
	    return cpuInfo;
	}
    }

}
