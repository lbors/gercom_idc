// NetDev.java
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
 * A class used to get net interface info on a Linux system.
 * It uses /proc/net/dev to read the underyling data.
 */
public class NetDev {

    // The /proc/net/dev file
    File procnetdev = new File("/proc/net/dev");

    // a map of the last snapshot from /proc/net/dev
    HashMap<String, Long> lasttime = new HashMap<String, Long>();
    // a map of the current delta values from /proc/net/dev
    HashMap<String, Integer> thisdelta = new HashMap<String, Integer>();

    // The name of the interface we want to monitor
    String ifName;

    /*
     * Construct a NetDev object, given an interface name
     */
    public NetDev(String ifName) {
        this.ifName = ifName;
    }

    /**
     * Get current value for a particular element of data.
     */
    public Long getCurrentValue(String key) {
        return lasttime.get(key);
    }

    /**
     * Get delta value, the difference between the current values
     * and the previous value, for a particular element of data.
     */
    public Integer getDeltaValue(String key) {
        return thisdelta.get(key);
    }

    /**
     * Read info for one interface
     * An example is:
     * eth0:10394907884 21513412 0 0 0 0 0 4464024 19349741172 22821925 0 0 0 0 0 0
     */
    public boolean read(boolean calculate) {
        // This currently reads everything and then
        // searches for the line that matches ifName
        // It would be faster to read until ifName
        // and then return
        // TODO: write a version on readProcNetDev which only returns 1 line

        String name = ifName;

	List<String> results = readProcNetDev(procnetdev);
	String regexp = "^ *"+name+":.*";

	if (results == null) {
	    return false;

	} else {
            boolean retVal = false;
            
	    for (String infoLine : results) {

		if (infoLine.matches(regexp)) {

		    //System.err.println("Found: " + name);

		    processData(calculate, infoLine);

                    retVal = true;

		    break;

		} else {
		    continue;
		}
	    }

	    return retVal;
	}
    }

    /**
     * Process a line of net data
     */
    private void processData(boolean calculate, String infoLine) {
	// process a line of form
	// name: v v v v v
	String[] parts = infoLine.split(":");

	// interface name
	String ifName = parts[0].trim();

	// now get values as a Long[]
	Long [] values = new Long[16];

	Scanner scanner = new Scanner(parts[1]);
	for (int v=0; v<16; v++) {
	    values[v] = scanner.nextLong();
	}
		
	long in_bytes = values[0];
	long in_packets = values[1];
	long in_errors = values[2];
	long in_dropped = values[3];
	long out_bytes = values[8];
	long out_packets = values[9];
	long out_errors = values[10];
	long out_dropped = values[11];


	// determine if we need to calculate the deltas
	// from the raw data
	if (calculate) {
	    int in_bytes_diff = (int)(in_bytes - lasttime.get("in_bytes"));
	    int in_packets_diff  = (int)(in_packets - lasttime.get("in_packets"));
	    int in_errors_diff  = (int)(in_errors - lasttime.get("in_errors"));
	    int in_dropped_diff  = (int)(in_dropped - lasttime.get("in_dropped"));
	    int out_bytes_diff  = (int)(out_bytes - lasttime.get("out_bytes"));
	    int out_packets_diff  = (int)(out_packets - lasttime.get("out_packets"));
	    int out_errors_diff  = (int)(out_errors - lasttime.get("out_errors"));
	    int out_dropped_diff  = (int)(out_dropped - lasttime.get("out_dropped"));
			

            /*
	    System.err.println("netInfo => " + ifName + ":" +
			       " in_bytes = " + in_bytes_diff +
			       " in_packets = " + in_packets_diff +
			       " in_errors = " + in_errors_diff +
			       " in_dropped = " + in_dropped_diff +
			       " out_bytes = " + out_bytes_diff +
			       " out_packets = " + out_packets_diff +
			       " out_errors = " + out_errors_diff +
			       " out_dropped = " + out_dropped_diff);
            */


	    thisdelta.put("in_bytes", in_bytes_diff);
	    thisdelta.put("in_packets", in_packets_diff);
	    thisdelta.put("in_errors", in_errors_diff);
	    thisdelta.put("in_dropped", in_dropped_diff);
	    thisdelta.put("out_bytes", out_bytes_diff);
	    thisdelta.put("out_packets", out_packets_diff);
	    thisdelta.put("out_errors", out_errors_diff);
	    thisdelta.put("out_dropped", out_dropped_diff);

	}

	// keep values
	// face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed

	lasttime.put("in_bytes", in_bytes);
	lasttime.put("in_packets", in_packets);
	lasttime.put("in_errors", in_errors);
	lasttime.put("in_dropped", in_dropped);
	lasttime.put("out_bytes", out_bytes);
	lasttime.put("out_packets", out_packets);
	lasttime.put("out_errors", out_errors);
	lasttime.put("out_dropped", out_dropped);
    }

    /**
     * Read from /proc/net/dev
     */
    private List<String> readProcNetDev(File procnetdev) {
	List<String> list = new LinkedList<String>();
	String line;

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procnetdev));

	    // first two lines are header
	    reader.readLine();
	    reader.readLine();

	    // find all lines starting with cpu
	    while ((line = reader.readLine()) != null) {
		list.add(line);
	    }

	    // we've reached the end of the stream
	    // so we close
	    reader.close();

	    return list;

	} catch (Exception e) {
	    // something went wrong
	    return null;
	}
    }


    private void printout() {
	// print out map
	for (String key : lasttime.keySet()) {
	    System.out.print(key + ": " + lasttime.get(key) + "\n");
	}
	for (String key : thisdelta.keySet()) {
	    System.out.print(key + ": " + thisdelta.get(key) + "\n");
	}
    }


    public static void main(String[] args) {
	NetDev ni = new NetDev("eth0");

	ni.printout();

	long t0 = System.currentTimeMillis();
	ni.read(true);
	long t1 = System.currentTimeMillis();

	System.err.println((t1 - t0) + " ms");

	ni.printout();

	long t2 = System.currentTimeMillis();

	System.err.println((t2 - t1) + " ms");

    }

}
