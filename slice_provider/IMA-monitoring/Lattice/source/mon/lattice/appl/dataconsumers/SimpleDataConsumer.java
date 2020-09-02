// SimpleConsumerUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package mon.lattice.appl.dataconsumers;

import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class SimpleDataConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public SimpleDataConsumer(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up an IP address for data
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));

	consumer.connect();

    }

    public static void main(String [] args) {
        String currentHost="localhost";
        int port = 22997;
        try {
            currentHost = InetAddress.getLocalHost().getHostName();   
        } catch (Exception e) {
        } 
        
	if (args.length == 0) {
	    new SimpleDataConsumer(currentHost, port);
	    System.err.println("SimpleConsumerUDP listening on " + currentHost + "/" + port);
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	    new SimpleDataConsumer(addr, port);

	    System.err.println("SimpleConsumerUDP listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: SimpleConsumerUDP localhost port");
	    System.exit(1);
	}
    }

}
