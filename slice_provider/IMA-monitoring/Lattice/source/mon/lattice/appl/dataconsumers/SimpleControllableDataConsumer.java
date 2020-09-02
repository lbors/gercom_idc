/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

/**
 *
 * @author uceeftu
 */

import mon.lattice.control.udp.UDPDataConsumerControlPlaneXDRConsumer;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import mon.lattice.distribution.udp.UDPDataPlaneConsumerWithNames;
import mon.lattice.im.dht.tomp2p.TomP2PDHTDataConsumerInfoPlane;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public final class SimpleControllableDataConsumer extends Thread {
    DefaultControllableDataConsumer consumer;

    /*
     * Construct a controllable SimpleControllableDataConsumer
     */
    public SimpleControllableDataConsumer(String dataAddr, 
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          int infoPlaneLocalPort,
                                          String controlAddr,
                                          int controlPort,
                                          int controlRemotePort) throws UnknownHostException {
        
        this.attachShutDownHook();
        
	// set up a DefaultControllableDataConsumer
	consumer = new DefaultControllableDataConsumer("controllable-DC");
        System.out.println("Data Consumer ID: " + consumer.getID());

	// set up an IP address for data and control
	InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataAddr), dataPort);
        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(controlAddr), controlPort);
        
        //  we are assuming here that the infoplane and control plane host of the controller are the same
        InetSocketAddress ctrlRemoteAddress = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
        
	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumerWithNames(address));
        
        ControlPlane controlPlane = new UDPDataConsumerControlPlaneXDRConsumer(ctrlAddress, ctrlRemoteAddress);
        ((DataConsumerInteracter) controlPlane).setDataConsumer(consumer);
        consumer.setControlPlane(controlPlane);
        
        //InfoPlane infoPlane = new TomP2PDHTDataConsumerInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort);
        InfoPlane infoPlane = new TomP2PDHTDataConsumerInfoPlane(infoPlaneRootPort, infoPlaneLocalPort);
        ((DataConsumerInteracter) infoPlane).setDataConsumer(consumer);
        consumer.setInfoPlane(infoPlane);

	consumer.connect();
    }

    
    @Override
    public void run() {
        System.out.println("Disconnecting from the planes before shutting down");
        try {
            // first performs deannounce and then disconnect for each of the planes
            consumer.disconnect(); 
        } catch (Exception e) {
            System.out.println("Something went wrong while Disconnecting from the planes " + e.getMessage());
          }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public static void main(String [] args) {
        try {
            String dcAddr = null;
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 10000;
            String controlEndPoint = null;
            int controlLocalPort = 2222;
            int controllerRemotePort = 8888;
            
            if (args.length == 0) {
                String loopBack = InetAddress.getLoopbackAddress().getHostName();
                System.out.println("No arguments provided - running on loopback: " + loopBack);
                dcAddr = infoHost = controlEndPoint = loopBack;
                new SimpleControllableDataConsumer(dcAddr, dataPort, infoHost, infoRemotePort, infoLocalPort, controlEndPoint, controlLocalPort, controllerRemotePort);
                System.err.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                System.err.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
            } else if (args.length == 5) {  
                Scanner sc = new Scanner(args[0]);
                dataPort = sc.nextInt();
                
                infoHost = args[1];
                
                sc = new Scanner(args[2]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[3]);
                infoLocalPort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                controlLocalPort = sc.nextInt();
                
                dcAddr = controlEndPoint = InetAddress.getLocalHost().getHostName();

                new SimpleControllableDataConsumer(dcAddr, dataPort, infoHost, infoRemotePort, infoLocalPort, controlEndPoint, controlLocalPort, controllerRemotePort);

                System.err.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                System.err.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
            } else {
                System.err.println("usage: SimpleControllableDataConsumer localdataPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        } 

    }
}


