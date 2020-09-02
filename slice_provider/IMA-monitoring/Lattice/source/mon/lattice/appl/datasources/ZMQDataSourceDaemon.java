package mon.lattice.appl.datasources;

import mon.lattice.appl.Daemon;
import mon.lattice.core.DefaultControllableDataSource;
import mon.lattice.control.zmq.ZMQDataSourceControlPlaneXDRConsumer;
import mon.lattice.distribution.zmq.ZMQDataPlaneProducer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ID;
import mon.lattice.im.zmq.ZMQDataSourceInfoPlane;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane and 
 * logs out/err to a file rather than standard streams.
 **/

public class ZMQDataSourceDaemon extends Daemon {
    protected ControllableDataSource dataSource;
    
    String dataSourceName;
    
    InetSocketAddress dataConsumerPair;
    InetSocketAddress ctrlPair;
    InetSocketAddress remoteCtrlPair;
    
    String remoteInfoHost;
    int remoteInfoPort;
    
    /**
     * Construct a ZMQDataSourceDaemon with no pre-loaded probes running as a daemon 
     * @param myID the UUID of the Data Source
     * @param myDSName the Name of the Data Source
     * @param dataConsumerName the Data Consumer to connect to
     * @param dataConsumerPort the port of the Data Consumer to connect to
     * @param infoPlaneRootName the host name of the Info Plane node to connect/bootstrap to (i.e., the Controller)
     * @param infoPlaneRootPort the port of the Info Plane node to connect/bootstrap to (i.e., the Controller)
     * @param controlHostAddress the Controller address
     * @param controlHostPort the Controller port
     * @throws UnknownHostException
     **/
    
    public ZMQDataSourceDaemon(
                           String myID,
                           String myDSName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           String controlHostAddress,
                           int controlHostPort
                           ) throws UnknownHostException {
    
    
        this.entityID = myID;
        this.dataSourceName = myDSName;
        
        this.dataConsumerPair = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);
        this.ctrlPair = new InetSocketAddress(InetAddress.getByName(controlHostAddress), controlHostPort);
        
        this.remoteInfoHost = infoPlaneRootName;
        this.remoteInfoPort = infoPlaneRootPort;
    }
     


    @Override
    public void init() throws IOException {
        entityType = "data-source-";
        classMetadata = ZMQDataSourceDaemon.class;
        
        attachShutDownHook();
        initLogger();
        
	dataSource = new DefaultControllableDataSource(dataSourceName, ID.fromString(entityID));
        
        LOGGER.info("Data Source ID: " + dataSource.getID());
        LOGGER.info("Process ID: " + dataSource.getMyPID());
        LOGGER.info("Using Data Source name: " + dataSourceName);
        LOGGER.info("Sending measurements to Data Consumer: " + dataConsumerPair.getHostName() + ":" + dataConsumerPair.getPort());
        LOGGER.info("Connecting to the Control Plane: " + ctrlPair.getHostName() + ":" + ctrlPair.getPort());
        
	// set up the planes
        dataSource.setDataPlane(new ZMQDataPlaneProducer(dataConsumerPair.getAddress().getHostAddress(), dataConsumerPair.getPort()));
        
        // ZMQ Info Plane
        dataSource.setInfoPlane(new ZMQDataSourceInfoPlane(remoteInfoHost, remoteInfoPort));
            
        // ZMQ Control Plane   
        dataSource.setControlPlane(new ZMQDataSourceControlPlaneXDRConsumer(ctrlPair));
        
	if (!dataSource.connect()) {
            LOGGER.error("Error while connecting to the Planes");
            System.exit(1); //terminating as there was an error while connecting to the planes
        }
        
        LOGGER.info("Connected to the Info Plane using: " + dataSource.getInfoPlane().getInfoRootHostname() + ":" + remoteInfoPort);
    }
    
    
    @Override
    public void run() {
        LOGGER.info("Disconnecting from the planes before shutting down");
        try {
            // will first do deannounce and then disconnect from each of the planes
            dataSource.disconnect();
        } catch (Exception e) {
            LOGGER.error("Something went wrong while disconnecting from the planes " + e.getMessage());
          }
    }
    
    
    public static void main(String [] args) {
        try {
            String dsID = ID.generate().toString();
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controllerHost = null;
            int controllerPort = 5555;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    dsName = dataConsumerAddr = controllerHost = loopBack;
                    infoHost = InetAddress.getLocalHost().getHostName();
                    break;
                case 5:
                    dataConsumerAddr = args[0];
                    sc = new Scanner(args[1]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    break;
                case 6:
                    dsID = args[0];
                    dataConsumerAddr = args[1];
                    sc = new Scanner(args[2]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    break;
                default:
                    System.err.println("use: ZMQDataSourceDaemon [UUID] dcAddress dcPort infoHost infoPort controllerHost controllerPort");
                    System.exit(1);
            }
            
            ZMQDataSourceDaemon dataSourceDaemon = new ZMQDataSourceDaemon(
                                                            dsID,
                                                            dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort,
                                                            controllerHost, 
                                                            controllerPort);
            dataSourceDaemon.init();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}
