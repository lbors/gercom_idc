/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents.appl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import mon.lattice.appl.Daemon;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public class VLSPControllerAgentDaemon extends Daemon {
    
    VLSPControllerAgent controllerAgent;
    
    String controlHostAddress;
    int controlHostPort;
    String remoteInfoHost;
    int remoteInfoPort;
    String VLSPMonitoringAddress;
    int VLSPMonitoringPort;
            
            
    public VLSPControllerAgentDaemon(String id,
                                       String controlHostAddress, 
                                       int controlHostPort, 
                                       String remoteInfoHost, 
                                       int remoteInfoPort,
                                       String VLSPMonitoringAddress,
                                       int VLSPMonitoringPort) throws UnknownHostException {
        
        entityID = id;
        this.controlHostAddress = controlHostAddress;
        this.controlHostPort = controlHostPort;
        this.remoteInfoHost = remoteInfoHost;
        this.remoteInfoPort = remoteInfoPort;
        this.VLSPMonitoringAddress = VLSPMonitoringAddress;
        this.VLSPMonitoringPort = VLSPMonitoringPort;
    }

    @Override
    protected void init() throws IOException {
        entityType = "controller-agent-";
        classMetadata = VLSPControllerAgentDaemon.class;
        
        attachShutDownHook();
        initLogger();
        
        controllerAgent = new VLSPControllerAgent(entityID, controlHostAddress, controlHostPort, remoteInfoHost, remoteInfoPort, VLSPMonitoringAddress, VLSPMonitoringPort);
        controllerAgent.init();
    }
    
    
    @Override
    public void run() {
        LOGGER.info("Disconnecting from the planes before shutting down");
        try {
            // will first do deannounce and then disconnect from each of the planes
            controllerAgent.disconnect();
        } catch (Exception e) {
            LOGGER.error("Something went wrong while disconnecting from the planes " + e.getMessage());
          }
    }
    
    
    public static void main(String [] args) {
        try {
            String id = ID.generate().toString();
            String name = null;
            
            String monitoringAddress="localhost";
            int monitoringPort=6666;

            String infoHost = null;
            int infoRemotePort= 6699;
            String controllerHost = null;
            int controllerPort = 5555;
            
            Scanner sc;
                 
            System.out.println(args.length);
            
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    controllerHost = loopBack;
                    name = infoHost = InetAddress.getLocalHost().getHostName();
                    break;
                case 5:
                    infoHost = controllerHost = args[0];
                    sc = new Scanner(args[1]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[2]);
                    controllerPort = sc.nextInt();
                    monitoringAddress = args[3];
                    sc = new Scanner(args[4]);
                    monitoringPort = sc.nextInt();
                    break;
                case 6:
                    id = args[0];
                    infoHost = controllerHost = args[1];
                    sc = new Scanner(args[2]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[3]);
                    controllerPort = sc.nextInt();
                    name = InetAddress.getLocalHost().getHostName();
                    monitoringAddress = args[4];
                    sc = new Scanner(args[5]);
                    monitoringPort = sc.nextInt();
                    break;
                default:
                    System.err.println("use: VLSPControllerAgentDaemon [UUID] infoHost infoPort controllerPort VLSPMonitoringAddress VLSPMonitoringPort");
                    System.exit(1);
            }
            
            VLSPControllerAgentDaemon vlspControllerAgentDaemon = new VLSPControllerAgentDaemon(id,
                                                                                                      controllerHost, 
                                                                                                      controllerPort,
                                                                                                      infoHost, 
                                                                                                      infoRemotePort,
                                                                                                      monitoringAddress,
                                                                                                      monitoringPort);
            vlspControllerAgentDaemon.init();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error while starting the VLSP Controller Agent " + ex.getMessage());
	}
    }
    
    
}
