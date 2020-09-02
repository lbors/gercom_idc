// ThalesLogFileMonitor.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Jan 2010

package eu.reservoir.monitoring.appl.probes.vee.thales;

import mon.lattice.distribution.multicast.MulticastDataPlaneProducerWithNames;
import mon.lattice.distribution.multicast.MulticastAddress;
import mon.lattice.core.Probe;
import mon.lattice.core.DataSource;
import mon.lattice.appl.datasources.BasicDataSource;
import java.util.Scanner;

/**
 * This monitor uses the ThalesLogFileProbe to collect
 * the latest data from a file.
 */
public class ThalesLogFileMonitor {
   // The DataSource
   DataSource ds;

   /*
    * Construct a ThalesLogFileMonitor
    */
    public ThalesLogFileMonitor(String addr, int port, String fqn) {
	// set up data source
	ds = new BasicDataSource(fqn);

	// set up multicast addresses
	MulticastAddress dataGroup = new MulticastAddress(addr, port); 

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducerWithNames(dataGroup));

	ds.connect();
   }

   private void turnOnProbe(Probe p) {
       ds.addProbe(p);
       ds.turnOnProbe(p);
   }

   private void turnOffProbe(Probe p) {
       ds.turnOffProbe(p);
       ds.removeProbe(p);
   }

   /**
    * Pass in the fullyQualifiedName.
    * This is the name of the Probe.
    * e.g. java eu.reservoir.appl.vee.sge.QueueLengthMonitor eu.reservoir.host54.vee.11.queuelength
    */
   public static void main(String [] args) {
	if (args.length == 2) {
	    String fqn = args[0];
	    String filename = args[1];

	    ThalesLogFileMonitor thalesMon = new ThalesLogFileMonitor("224.0.1.221", 10, fqn);
	    Probe lfProbe = new ThalesLogFileProbe(fqn, filename);
	    thalesMon.turnOnProbe(lfProbe);

	} else if (args.length == 4) {
	    String fqn = args[0];
	    String filename = args[1];
	    String addr = args[2];

	    Scanner sc = new Scanner(args[3]);
	    int port = sc.nextInt();

	    ThalesLogFileMonitor thalesMon = new ThalesLogFileMonitor(addr, port, fqn);
	    Probe lfProbe = new ThalesLogFileProbe(fqn, filename);
	    thalesMon.turnOnProbe(lfProbe);
	} else {
	    System.err.println("ThalesLogFileMonitor fullyQualifiedName logfilename [multicast-address port]");
	    System.exit(1);
	}

   }
}
