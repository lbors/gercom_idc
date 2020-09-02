// SimpleConsumerUDP.java
// Author: Poe
// Date: Sept 2016

package mon.lattice.appl.dataconsumers;


import mon.lattice.appl.reporters.ReporterException;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
@Deprecated
public class DataConsumerWithMongoDBReporter {
    // The Basic consumer
    MongoDBConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public DataConsumerWithMongoDBReporter(String addr, int dataPort, String dbAddr, int dbPort, String dbName, String collectionName) throws ReporterException {
        // set up a BasicConsumer
        //consumer = new BasicConsumer();

        // set up a BasicConsumer for MongoDB
        consumer = new MongoDBConsumer(dbAddr, dbPort, dbName, collectionName);

        // set up an IP address for data
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);

        // set up data plane
        consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));

        consumer.connect();

    }
    public static void main(String [] args) {
        String currentHost="localhost";
        String mongoDBAddress = "192.168.56.102";
        int mongoDBPort = 27017;
        String mongDBName = "test";
        String collectionName = "cs";
        
        int port = 22997;
        try {
            currentHost = InetAddress.getLocalHost().getHostName();
            
            if (args.length == 0) {
                new DataConsumerWithMongoDBReporter(currentHost, port, mongoDBAddress, mongoDBPort, mongDBName, collectionName);
                System.err.println("DataConsumerWithMongoDBReporter listening on " + currentHost + "/" + port);
            } else if (args.length == 2) {
                String addr = args[0];

                Scanner sc = new Scanner(args[1]);
                port = sc.nextInt();

                new DataConsumerWithMongoDBReporter(addr, port, mongoDBAddress, mongoDBPort, mongDBName, collectionName);
                System.err.println("DataConsumerWithMongoDBReporter listening on " + addr + "/" + port);
            } else {
                System.err.println("usage: DataConsumerWithMongoDBReporter localhost port");
                System.exit(1);
            }
    } catch (Exception e) {
        System.out.println("Error while Starting the DataConsumerWithMongoDBReporter" + e.getMessage());
        }
    }
}
    

