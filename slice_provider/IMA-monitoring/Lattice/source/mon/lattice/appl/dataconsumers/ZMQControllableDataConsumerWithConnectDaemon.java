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

import mon.lattice.control.zmq.ZMQDataConsumerControlPlaneXDRConsumer;
import mon.lattice.distribution.zmq.ZMQDataPlaneConsumer;
import mon.lattice.im.zmq.ZMQDataConsumerInfoPlane;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.ID;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.InfoPlane;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This receives measurements from a UDP Data Plane.
 */
public final class ZMQControllableDataConsumerWithConnectDaemon extends Thread {
    DefaultControllableDataConsumer consumer;
    
    ID dataConsumerID;
    
    String dataConsumerName = "controllable-DC";
    
    int dataPort;
    
    InetSocketAddress localCtrlPair;
    InetSocketAddress remoteCtrlPair;
    
    String remoteInfoHost;
    int localInfoPort;
    int remoteInfoPort;
    
    String remoteDataHost;
    
    private static Logger LOGGER;
    
    PrintStream outStream;
    PrintStream errStream;

    
    public ZMQControllableDataConsumerWithConnectDaemon(String myID,
                                          String remoteDataHost,
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          int infoPlaneLocalPort,
                                          String controlAddr,
                                          int controlPort
                                          ) throws UnknownHostException {
    
        this.dataConsumerID = ID.fromString(myID);
        this.dataPort = dataPort;
        
        this.localCtrlPair = new InetSocketAddress(InetAddress.getByName(controlAddr), controlPort);
        
        this.remoteInfoHost = infoPlaneRootName;
        this.localInfoPort = infoPlaneLocalPort;
        this.remoteInfoPort = infoPlaneRootPort;
        
        this.remoteDataHost = remoteDataHost;
        
        }
    
    
    
    public ZMQControllableDataConsumerWithConnectDaemon(String myID,
                                          String remoteDataHost,
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          int infoPlaneLocalPort,
                                          String controlAddr,
                                          int controlPort,
                                          int controlRemotePort) throws UnknownHostException {
    
        this(myID, remoteDataHost, dataPort, infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort, controlAddr, controlPort);
        // commeting out this as the infoPlaneRootName is no longer passed as parameter
        //this.remoteCtrlPair = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
    }
    
    
    public void init() throws IOException {
        attachShutDownHook();
        setLogger();
        
        consumer = new DefaultControllableDataConsumer(dataConsumerName, dataConsumerID);
        
        LOGGER.info("Data Consumer ID: " + consumer.getID());
        LOGGER.info("Process ID: " + consumer.getMyPID());
        LOGGER.info("Connecting to the Info Plane using: " + localInfoPort + ":" + remoteInfoHost + ":" + remoteInfoPort);
        LOGGER.info("Connecting to the Control Plane using: " + localCtrlPair.getPort() + ":" + localCtrlPair.getHostName());
        
        // set up data plane listening on *:port
	//consumer.setDataPlane(new UDPDataPlaneConsumer(dataPort));
        consumer.setDataPlane(new ZMQDataPlaneConsumer(remoteDataHost, dataPort));
        //consumer.setDataPlane(new ZMQDataPlaneConsumerAndForwarder(dataPort));
       
        //InfoPlane infoPlane = new TomP2PDHTDataConsumerInfoPlane(remoteInfoHost, remoteInfoPort, localInfoPort);
        //InfoPlane infoPlane = new TomP2PDHTDataConsumerInfoPlane(remoteInfoPort, localInfoPort); // announcing to broadcast
        
        InfoPlane infoPlane = new ZMQDataConsumerInfoPlane(remoteInfoHost, remoteInfoPort);
        
        
        ((DataConsumerInteracter) infoPlane).setDataConsumer(consumer);
        consumer.setInfoPlane(infoPlane);
        
        ControlPlane controlPlane;
        // if (this.remoteCtrlPair != null)
        //    controlPlane = new UDPDataConsumerControlPlaneXDRConsumer(localCtrlPair, remoteCtrlPair);
        // else
        controlPlane = new ZMQDataConsumerControlPlaneXDRConsumer(localCtrlPair);
        
        ((DataConsumerInteracter) controlPlane).setDataConsumer(consumer);
        consumer.setControlPlane(controlPlane);    

        if (!consumer.connect()) {
            LOGGER.error("Error while connecting to the Planes");
            System.exit(1); //terminating as there was an error while connecting to the planes
        }
    }
    
    
    void setLogger() throws IOException {
        String logFileName = "data-consumer-" + dataConsumerID + ".log";
        File logFile;
        
        logFile = new File("/tmp/" + logFileName);
        
        if (!logFile.exists()) {
	    logFile.createNewFile();
	}
        
        if (!logFile.canWrite()) {
            logFile = new File(System.getProperty("user.home") + "/" + logFileName);
            if (!logFile.exists())
               logFile.createNewFile(); 
        }
        
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, logFile.getCanonicalPath());
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        //System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        
        LOGGER = LoggerFactory.getLogger(ZMQControllableDataConsumerWithConnectDaemon.class);
    }
    
    
   
    @Override
    public void run() {
        LOGGER.info("Disconnecting from the planes before shutting down");
        try {
            // performs deannounce and then disconnect for each plane
            consumer.disconnect(); 
        } catch (Exception e) {
            LOGGER.error("Something went wrong while Disconnecting from the planes " + e.getMessage());
          }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public void mockDisconnect() {
        consumer.disconnect();
    }
    
    
    public static void main(String [] args) {
        try {
            String dcID = ID.generate().toString();
            //String dcAddr = null; listening on all the addresses
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 10000;
            String controlEndPoint = null;
            int controlRemotePort = 5555;
            
            String remoteDataHost = null;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    remoteDataHost = infoHost = controlEndPoint = loopBack;
                    break;
                case 6:
                    remoteDataHost = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                case 7:
                    remoteDataHost = args[0];
                    dcID = args[1];
                    sc = new Scanner(args[2]);
                    dataPort = sc.nextInt();
                    infoHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[6]);
                    controlRemotePort = sc.nextInt();
                    controlEndPoint = infoHost;
                    break;
                default:
                    LOGGER.error("usage: ControllableDataConsumerDaemon [dcID] remotedataPort infoRemotePort infoLocalPort controlLocalPort");
                    System.exit(1);
            }
            ZMQControllableDataConsumerWithConnectDaemon dataConsumer = new ZMQControllableDataConsumerWithConnectDaemon(dcID, 
                                                                                   remoteDataHost,
                                                                                   dataPort, 
                                                                                   infoHost, 
                                                                                   infoRemotePort, 
                                                                                   infoLocalPort, 
                                                                                   controlEndPoint, 
                                                                                   controlRemotePort);
            dataConsumer.init();
            
        } catch (Exception e) {
            LOGGER.error("Error " + e.getMessage());
        } 

    }
}


