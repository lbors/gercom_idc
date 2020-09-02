// ThalesLogFileProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Jan 2010

package eu.reservoir.monitoring.appl.probes.vee.thales;

import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeLifecycle;
import eu.reservoir.monitoring.appl.probes.vee.KPIProbe;
import mon.lattice.core.datarate.EveryNSeconds;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A probe for gettting the response time.
 */
public class ThalesLogFileProbe extends KPIProbe implements Probe  {
    /*
     * The file with the data in.
     */
    File logFile = null;

    /*
     * File length
     */
    long length = 0;

    /*
     * Hit eof
     */
    boolean eof = false;
     
    /*
     * A reader for the file.
     */
    BufferedReader reader;

    /**
     * Construct a probe
     */
    public ThalesLogFileProbe(String fqn, String filename) {
        // call super constructor
        super(fqn);

	// set log file
	logFile = new File(filename);
	// get it's length
	length = length();

	// set name
	setName(fqn);

	// once every 1 seconds
	setDataRate(new EveryNSeconds(1));

        // add a KPI value
        addKPI("responseTime", ProbeAttributeType.INTEGER, "n");


	// activate the Probe
	activateProbe();
    }

    /**
     * Turning on the Probe should open the file.
     */
    public ProbeLifecycle turnOnProbe() {
	// open file
	try {
	    reader = new BufferedReader(new FileReader(logFile));	
	    super.turnOnProbe();
	} catch (Exception e) {
	    System.err.println("ThalesLogFileProbe: Cannot open file " + logFile + " so Probe not started.");
	}

	return this;
    }

    /**
     * Turn off the probe closes the file.
     */
    public ProbeLifecycle turnOffProbe() {
	super.turnOffProbe();
	try {
	    reader.close();
	} catch (Exception e) {
	    System.err.println("ThalesLogFileProbe: cannot close " + logFile);
	}

	return this;
    }


    /**
     * Collect KPI values for measurement.
     */
    public List<Object> collectKPIValues() {
	try {
	    ArrayList<Object> list = new ArrayList<Object>(1);

            //extract KPI from Thales log file
            
            // read a response time
	    int responseTime = readEntry();

	    if (responseTime == -1) {
		// there is no value in the file
		return null;
	    } else {

                // add queueLength to list
                list.add(new Integer(responseTime));

                return list;
            }
	} catch (Exception e) {
	    return null;
	}
    }


    /*
     * We need to take account of the fact that the file grows
     * and that we might reach the end of the file.
     */
    private int readEntry() {
	int rt;

	// see if we hit eof ast time
	if (eof) {
	    // check file length
	    if (length == length()) {
		// still same size
	    } else {
		//System.err.println("File got bigger");
		length = length();
		eof = false;
	    }
	}

	try {
	    // read a line
	    String line = reader.readLine();

	    if (line == null) {
		eof = true;
		return -1;
	    }

	    // split into parts
	    String[] parts = line.split(" ");
	    // part 12 is actually the time
	    rt = Integer.parseInt(parts[11]);

	    return rt;
	} catch (Exception e) {
	    // something went wrong
	    System.err.println("ThalesLogFileProbe: " + e);
	    try {
		reader.close();
	    } catch (Exception ce) {
		System.err.println("ThalesLogFileProbe: cannot close " + logFile);
	    }

	    return 0;
	}
    }

    /**
     * Get file length
     */
    public long length() {
	long len = logFile.length();
	return len;
    }
}