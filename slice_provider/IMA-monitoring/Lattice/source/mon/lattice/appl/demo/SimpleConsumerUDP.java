// SimpleConsumerUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package mon.lattice.appl.demo;

import mon.lattice.appl.dataconsumers.BasicConsumer;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class SimpleConsumerUDP {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public SimpleConsumerUDP(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));

	consumer.connect();

    }

    public static void main(String [] args) {
	if (args.length == 0) {
	    new SimpleConsumerUDP("localhost", 22997);
	    System.err.println("SimpleConsumerUDP listening on localhost/22997");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    new SimpleConsumerUDP(addr, port);

	    System.err.println("SimpleConsumerUDP listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: SimpleConsumerUDP localhost port");
	    System.exit(1);
	}
    }

}