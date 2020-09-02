// QueueLengthMonitor.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.demo;

import mon.lattice.distribution.multicast.MulticastAddress;
import mon.lattice.distribution.multicast.MulticastDataPlaneProducerWithNames;
import mon.lattice.core.Probe;
import mon.lattice.core.DataSource;
import mon.lattice.appl.datasources.BasicDataSource;
import java.util.Scanner;

/**
* This monitor uses a QueueLengthDataSource and plugs in
* a DataSourceMulticaster to distribute the data.
*/
public class QueueLengthMonitor {
   // The DataSource
   DataSource ds;

   /*
    * Construct a QueueLengthMonitor
    */
    public QueueLengthMonitor(String addr, int port, String fqn) {
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
	if (args.length == 1) {
	    String fqn = args[0];

	    QueueLengthMonitor queueLengthMon = new QueueLengthMonitor("224.0.1.221", 10, fqn);
	    Probe monitorProbe = new QueueLengthProbe(fqn);
	    queueLengthMon.turnOnProbe(monitorProbe);

	} else if (args.length == 3) {
	    String fqn = args[0];

	    String addr = args[1];

	    Scanner sc = new Scanner(args[2]);
	    int port = sc.nextInt();

	    QueueLengthMonitor queueLengthMon = new QueueLengthMonitor(addr, port, fqn);
	    Probe monitorProbe = new QueueLengthProbe(fqn);
	    queueLengthMon.turnOnProbe(monitorProbe);
	} else {
	    System.err.println("QueueLengthMonitor fullyQualifiedName [multicast-address port]");
	    System.exit(1);
	}

   }
}
