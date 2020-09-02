package mon.lattice.appl.datasources;

import mon.lattice.core.DefaultControllableDataSource;
import mon.lattice.control.udp.UDPDataSourceControlPlaneXDRConsumer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ID;
import mon.lattice.distribution.udp.UDPDataPlaneProducerWithNames;
import mon.lattice.im.dht.tomp2p.TomP2PDHTDataSourceInfoPlane;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane and 
 * logs out/err to a file rather than standard streams.
 **/
public final class DataSourceDockerContainerDaemon extends Thread {
    ControllableDataSource dataSource;
    
    ID dataSourceID;
    String dataSourceName;
    
    InetSocketAddress dataConsumerPair;
    InetSocketAddress localCtrlPair;
    InetSocketAddress remoteCtrlPair;
    
    String remoteInfoHost;
    int localInfoUDPPort;
    int localInfoTCPPort;
    int remoteInfoPort;
    
    DockerDataSourceConfigurator dataSourceConfigurator;
    
    private static Logger LOGGER;
    
    
    /**
     * Construct a SimpleDataSource with no pre-loaded probes running as a daemon 
     * @param myID the UUID of the Data Source
     * @param myDSName the Name of the Data Source
     * @param dataConsumerName the host name of the Data Consumer to connect to
     * @param dataConsumerPort the port of the Data Consumer to connect to
     * @param infoPlaneRootName the host name of the Info Plane node to bootstrap to (i.e., the Controller)
     * @param infoPlaneRootPort the port of the Info Plane node to bootstrap to (i.e., the Controller)
     * @param localControlEndPoint the Control Plane address visible to the other nodes
     * @throws java.io.IOException
     **/
    
    public DataSourceDockerContainerDaemon(
                           String myID,
                           String myDSName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           String localControlEndPoint
                           ) throws IOException {
    
    
        this.dataSourceID = ID.fromString(myID);
        this.dataSourceName = myDSName;
        
        // dockerHost has to be resolvable from within the container
        // port can be a standard fixed
        // the container name is the hostname of the container
        String dockerHost = "docker"; // the container has to be able to resolve this name
        
        setLogger();
        
        this.dataSourceConfigurator = new DockerDataSourceConfigurator(dockerHost, 4243, InetAddress.getLocalHost().getHostName());
        int controlPlaneLocalPort = dataSourceConfigurator.getControlPort();
        
        this.dataConsumerPair = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);
        this.localCtrlPair = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);
        
        this.remoteInfoHost = infoPlaneRootName;
        this.localInfoUDPPort = dataSourceConfigurator.getInfoUDPPort();
        this.localInfoTCPPort = dataSourceConfigurator.getInfoTCPPort();
        this.remoteInfoPort = infoPlaneRootPort;
    }


    public void init() throws IOException {
        attachShutDownHook();
        //setLogger();
        
	dataSource = new DefaultControllableDataSource(dataSourceName, dataSourceID);
        
        LOGGER.info("Data Source ID: " + dataSource.getID());
        LOGGER.info("Process ID: " + dataSource.getMyPID());
        LOGGER.info("Using Data Source name: " + dataSourceName);
        LOGGER.info("Sending measurements to Data Consumer: " + dataConsumerPair.getHostName() + ":" + dataConsumerPair.getPort());
        LOGGER.info("Connecting to the Control Plane using: " + localCtrlPair.getPort() + ":" + localCtrlPair.getHostName());
        
        ((DockerDataSource)dataSource).setDataSourceConfigurator(dataSourceConfigurator);
        
	// set up the planes
	dataSource.setDataPlane(new UDPDataPlaneProducerWithNames(dataConsumerPair));
        
        if (remoteInfoHost != null)
            dataSource.setInfoPlane(new TomP2PDHTDataSourceInfoPlane(remoteInfoHost, remoteInfoPort, localInfoUDPPort, localInfoTCPPort));
        else
            dataSource.setInfoPlane(new TomP2PDHTDataSourceInfoPlane(remoteInfoPort, localInfoUDPPort, localInfoTCPPort)); // bootstraping using broadcast
        
        if (this.remoteCtrlPair != null)
            dataSource.setControlPlane(new UDPDataSourceControlPlaneXDRConsumer(localCtrlPair, remoteCtrlPair));
        else
            dataSource.setControlPlane(new UDPDataSourceControlPlaneXDRConsumer(localCtrlPair));
        
	if (!dataSource.connect()) {
            LOGGER.error("Error while connecting to the Planes");
            System.exit(1); //terminating as there was an error while connecting to the planes
        }
        
        //LOGGER.info("Connected to the Info Plane using: " + localInfoUDPPort + "/udp " + localInfoTCPPort + "/tcp" + ":" + dataSource.getInfoPlane().getInfoRootHostname() + ":" + remoteInfoPort);
    }
    
    
    void setLogger() throws IOException {
        String logFileName = "data-source-" + dataSourceID + ".log";
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
        
        LOGGER = LoggerFactory.getLogger(DataSourceDockerContainerDaemon.class);
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
    
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public static void main(String [] args) {
        try {
            String dsID = ID.generate().toString();
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controlEndPoint = null;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    dsName = dataConsumerAddr = controlEndPoint = loopBack;
                    infoHost = InetAddress.getLocalHost().getHostName();
                    break;
                case 4:
                    dataConsumerAddr = args[0];
                    sc = new Scanner(args[1]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    break;
                case 5:
                    dsID = args[0];
                    dataConsumerAddr = args[1];
                    sc = new Scanner(args[2]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    break;
                default:
                    System.err.println("use: DataSourceContainerDaemon [dsID] dcAddress dcPort infoRemoteHost infoRemotePort");
                    System.exit(1);
            }
            
            DataSourceDockerContainerDaemon dataSourceDaemon = new DataSourceDockerContainerDaemon(
                                                            dsID,
                                                            dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort,
                                                            controlEndPoint);
            
            dataSourceDaemon.init();
            
        } catch (Exception ex) {
            System.err.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}