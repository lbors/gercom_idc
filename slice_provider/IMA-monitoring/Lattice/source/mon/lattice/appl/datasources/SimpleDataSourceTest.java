// ResponseTimeEmulatorUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.appl.datasources;

import mon.lattice.core.DefaultControllableDataSource;
import mon.lattice.control.udp.UDPDataSourceControlPlaneXDRConsumer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.distribution.udp.UDPDataPlaneProducerWithNames;
import mon.lattice.im.dht.tomp2p.TomP2PDHTDataSourceInfoPlane;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.appl.demo.RandomProbe;

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane
 */
public final class SimpleDataSourceTest extends Thread {
    ControllableDataSource ds;

    /*
     * Construct a SimpleDataSource with no loaded probes.
     */
    public SimpleDataSourceTest(String myDsName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           int infoPlaneLocalPort,
                           String localControlEndPoint,
                           int controlPlaneLocalPort,
                           int controlRemotePort) throws Exception, UnknownHostException {
        
        this.attachShutDownHook();
        
	// set up data source
	ds = new DefaultControllableDataSource(myDsName);
        
        System.out.println("Sending data to: " + dataConsumerName + ":" + dataConsumerPort);
        
        System.out.println("Using local host name: " + myDsName);
        
        System.out.println("Connecting to InfoPlaneRoot using : " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to ControPlane using: " + controlPlaneLocalPort + ":" + myDsName);
        
        System.out.println("DataSource ID: " + ds.getID());
        
	// set up an IPaddress for data
	InetSocketAddress DataAddress = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);

        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);
        
        //  we are assuming here that the infoplane and control plane host of the controller are the same
        //InetSocketAddress ctrlRemoteAddress = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
        
	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducerWithNames(DataAddress));
        
        // set up control plane: a data source is a consumer of Control Messages 
        // ctrlAddress is the address:port where this DS will listen for ctrl messages
        // ctrlRemoteAddress is the port where the controller is listening for announce messages
        //ControlPlane controlPlane = new UDPDataSourceControlPlaneXDRConsumer(ctrlAddress, ctrlRemoteAddress);
        ControlPlane controlPlane = new UDPDataSourceControlPlaneXDRConsumer(ctrlAddress);
        
        ds.setControlPlane(controlPlane);
        
        //the root of the DHT is by default on infoPlaneRootName 6699
        //ds.setInfoPlane(new TomP2PDHTDataSourceInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort));
        ds.setInfoPlane(new TomP2PDHTDataSourceInfoPlane(infoPlaneRootPort, infoPlaneLocalPort));
        
	ds.connect();
    }
    
    
    private void turnOnProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.addProbe(p);
	    ds.turnOnProbe(p);
	}
    }

    
    @Override
    public void run() {
        System.out.println("Disconnecting from the planes before shutting down");
        try {
            // first performs deannounce and then disconnect for each of the planes
            ds.disconnect(); 
        } catch (Exception e) {
            System.out.println("Something went wrong while Disconnecting from the planes " + e.getMessage());
          }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    

    public static void main(String[] args) throws InterruptedException {
	try {
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 9999;
            String controlEndPoint = null;
            int controlLocalPort = 1111;
            int controllerRemotePort = 8888;
            
            if (args.length == 0) {
                // use existing settings
                String loopBack = InetAddress.getLoopbackAddress().getHostName();
                System.out.println("No arguments provided - running on loopback: " + loopBack);
                dsName = dataConsumerAddr = infoHost = controlEndPoint = loopBack;
                
            } else if (args.length == 6) {
                //dsName = args[0];
                
                dataConsumerAddr = args[0];
                
                Scanner sc = new Scanner(args[1]);
                dataConsumerPort = sc.nextInt();
                
                infoHost = args[2];
                
                sc = new Scanner(args[3]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                infoLocalPort = sc.nextInt();
                
                // controlEndPoint=args[6];
                
                sc= new Scanner(args[5]);
                controlLocalPort = sc.nextInt();
                
                dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                System.out.println(dsName);
                
            } else {
                System.err.println("use: SimpleDataSource dcAddress dcPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }            
            
            /*
            dsName: is saved in the infoplane to be used as the control endpoint for the DS (and as DS name)
            dataConsumerAddr: address of the destination dataConsumer 
            dataConsumerPort: port of the destination dataConsumer
            infoHost: host where the infoplane root node is running
            infoRemotePort: port where the info plane root node is listening
            infoLocalPort: port to be used by this DS to connect to the info plane
            controlLocalPort: port to be used locally for the control plane
            */

            SimpleDataSourceTest hostMon = new SimpleDataSourceTest(dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort, 
                                                            infoLocalPort, 
                                                            controlEndPoint, 
                                                            controlLocalPort,
                                                            controllerRemotePort);
            
            /*
            Probe openStack = new OpenstackProbe("localhost", 
                                                 "8777", 
                                                 "localhost",
                                                 "35357", 
                                                 "username",
                                                 "password",
                                                 "OpenstackTestProbe", 
                                                 "cbf84af5-3ac1-417d-b027-abcdeddfd000", 
                                                 "test-VNF");
            hostMon.turnOnProbe(openStack);
            */
            
            /*
            Probe docker = new DockerProbe("osboxes1", "4243", "dockerTestProbe", "fb8366a76b7aa", "name");
            hostMon.turnOnProbe(docker);
            */
            
            /*
            Probe memory = new MemoryInfoProbe("testMemoryProbe");
            hostMon.turnOnProbe(memory);
            */
            
            Probe random = new RandomProbe("Random Probe", "random attribute", "200");
            hostMon.turnOnProbe(random);
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}
