// HostMonitor.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.demo;

import mon.lattice.appl.probes.host.linux.NetInfo;
import mon.lattice.appl.probes.host.linux.CPUInfo;
import mon.lattice.appl.probes.host.linux.MemoryInfo;
import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.distribution.multicast.MulticastDataPlaneProducerWithNames;
import mon.lattice.distribution.multicast.MulticastAddress;
import mon.lattice.control.udp.AbstractUDPControlPlaneConsumer;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends CPU info and uses a Multicast Data Plane.
 */
public class HostMonitor {
    // The DataSource
    DataSource ds;

    /*
     * Construct a HostMonitor.
     */
    public HostMonitor(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducerWithNames(address));
        //ds.setControlPlane(new AbstractUDPControlPlaneConsumer());

	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.addProbe(p);
	ds.deactivateProbe(p);
    }

    public static void main(String [] args) {
	String addr = "229.229.0.1";
	int port = 2299;
	String flag = "-c";   	// default is CPU

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 1) {
	    flag = args[0];

	} else if (args.length == 3) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();


	    flag = args[2];

	} else {
	    System.err.println("HostMonitor multicast-address port -c|-n|-m");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	HostMonitor hostMon = new HostMonitor(addr, port, currentHost);

	if (flag.equals("-c")) {
	    hostMon.turnOnProbe(new CPUInfo(currentHost + ".cpuInfo"));
	} else if (flag.equals("-n")) {
	    hostMon.turnOnProbe(new NetInfo(currentHost + ".netInfo", "eth0"));
	} else if (flag.equals("-m")) {
	    hostMon.turnOnProbe(new MemoryInfo(currentHost + ".memoryInfo"));
	} else if (flag.equals("-a")) {
	    hostMon.turnOnProbe(new CPUInfo(currentHost + ".cpuInfo"));
	    hostMon.turnOnProbe(new NetInfo(currentHost + ".netInfo", "eth0"));
	    hostMon.turnOnProbe(new MemoryInfo(currentHost + ".memoryInfo"));
	} else if (flag.equals("-i")) {
	    hostMon.turnOnProbe(new InfraProbe("es.tid.customers.ucl.services.app1.vees.infra.replicas.1", currentHost));
        }
        else if (flag.equals("-z")) {        
            System.out.println("Monitoring Apple MAC CPU");
            hostMon.turnOnProbe(new CPUInfo(currentHost + ".cpuInfo"));
	} else {
	    System.err.println("HostMonitor multicast-address port -c|-n|-m");
	}
    }


}
