// UserProcProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.demo;

import mon.lattice.core.data.table.DefaultTableValue;
import mon.lattice.core.data.table.TableProbeAttribute;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.data.list.ListProbeAttribute;
import mon.lattice.core.data.list.DefaultMList;
import mon.lattice.core.data.list.MList;
import mon.lattice.core.Timestamp;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.Rational;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A probe for listing processes on a tty
 */
public class UserProcTableProbe extends AbstractProbe implements Probe  {
    // the tty name
    String tty;

    // The Table definition
    TableHeader definition;

    /*
     * Construct a probe
     */
    public UserProcTableProbe(String tty) {
	this.tty = tty;
        setName(tty);

	// get a randomizer in range -100 to 100
	int random = ((int)(Math.random() * 200)) - 100;

	System.err.println("random = " + random);

        setDataRate(new Rational(180 + random, 1));

	//   PID TTY           TIME CMD
	// 52306 ttys000    0:03.27 login -pf sclayman
	// 52316 ttys000    0:00.14 -bash
	// 96754 ttys000    0:00.01 ps -t ttys000

	// this probe has two values
	// 1. the tty
	// 2 the table a la 'ps'

	// define the header
	definition = new DefaultTableHeader().
	    add("pid", ProbeAttributeType.INTEGER).
	    add("tty", ProbeAttributeType.STRING).
	    add("time", ProbeAttributeType.STRING).
	    add("cmd", ProbeAttributeType.STRING);

        addProbeAttribute(new DefaultProbeAttribute(0, "tty", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new ListProbeAttribute(1, "list", ProbeAttributeType.LONG));
        addProbeAttribute(new TableProbeAttribute(2, "procs", definition));


        activateProbe();
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    Process proc = Runtime.getRuntime().exec("ps -rt " + tty);
	    InputStream inSteam = proc.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inSteam));

	    // read 2 lines
	    String line;
	    
	    // line 1 is header
	    if ((line = reader.readLine()) == null) {
		// EOF on line 1
		System.err.println(getName() + ": EOF on header");
		// get the probe manager to turn off this probe
		// and deactivate it
		getProbeManager().turnOffProbe(this);
		getProbeManager().deactivateProbe(this);
		return null;
	    } else {
		if ((line = reader.readLine()) == null) {
		    // EOF on line 2
		    System.err.println(getName() + ": EOF on line 1");
		    // get the probe manager to turn off this probe
		    // and deactivate it
		    getProbeManager().turnOffProbe(this);
		    getProbeManager().deactivateProbe(this);
		    return null;
		} else {
		    // we got something useful

		    // Allocate a Table for the listing
		    Table psTable = new DefaultTable();

		    // now define the table, using the same TableHeader
		    psTable.defineTable(definition);

		    // skip through all lines
		    do {
			// each line should look like this:
			//  PID TTY TIME CMD
			Scanner scanner = new Scanner(line);
			int pid = scanner.nextInt();
			String tty = scanner.next();
			String time = scanner.next();
			String cmd = scanner.findInLine(".*");

			psTable.addRow(new DefaultTableRow().
				       add(new DefaultTableValue(pid)).
				       add(new DefaultTableValue(tty)).
				       add(new DefaultTableValue(time)).
				       add(new DefaultTableValue(cmd))
				       );
					   
					   

		    } while ((line = reader.readLine()) != null);

		    // close process
		    proc.getInputStream().close();
		    proc.getOutputStream().close();
		    proc.waitFor();

		    // collect the list of numbers
		    MList mlist = new DefaultMList(ProbeAttributeType.LONG);
		    Timestamp t = getLastMeasurementCollection();
		    long lastTime = (t == null ? System.currentTimeMillis() : t.value());
		    mlist.add(lastTime);
		    mlist.add(System.currentTimeMillis() - lastTime);
		    

		    // collate measurement values
		    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(2);

		    list.add(new DefaultProbeValue(0, tty));
		    list.add(new DefaultProbeValue(1, mlist));
		    list.add(new DefaultProbeValue(2, psTable));

		    ProbeMeasurement m = new ProducerMeasurement(this, list, "ps");

		    System.out.println(m.getValues());

		    return m;
		}
	    }
	} catch (IOException ioe) {
	    System.err.println("IOException in UserProcProbe");
	    ioe.printStackTrace();
	    return null;
	} catch (InterruptedException ie) {
	    System.err.println("InterruptedException in UserProcProbe");
	    return null;
	} catch (Exception e) {
	    System.err.println("Exception " + e + " in UserProcProbe");
	    e.printStackTrace();
	    return null;
	}
    }

}
