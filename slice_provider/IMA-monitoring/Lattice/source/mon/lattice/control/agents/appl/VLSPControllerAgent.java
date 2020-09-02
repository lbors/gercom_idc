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
import mon.lattice.control.ControlServiceException;
import mon.lattice.control.agents.AbstractZMQControllerAgent;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.put;
import static us.monoid.web.Resty.content;

/**
 *
 * @author uceeftu
 */
public class VLSPControllerAgent extends AbstractZMQControllerAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(VLSPControllerAgent.class);
    String VLSPURI;
    
    Resty rest;
    
    public VLSPControllerAgent(String id,
                                 String controlHostAddress, 
                                 int controlHostPort, 
                                 String remoteInfoHost, 
                                 int remoteInfoPort,
                                 String VLSPMonitoringAddress,
                                 int VLSPMonitoringPort) throws UnknownHostException {
        
        super(id, controlHostAddress, controlHostPort, remoteInfoHost, remoteInfoPort);
        this.VLSPURI = "http://" + VLSPMonitoringAddress + ":" + VLSPMonitoringPort;
        LOGGER.info("connecting to: " + VLSPURI);
        this.rest = new Resty();
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
                    System.err.println("use: VLSPControllerAGent [UUID] infoHost infoPort controllerHost controllerPort VLSPMonitoringAddress VLSPMonitoringPort");
                    System.exit(1);
            }
            
            VLSPControllerAgent vlspControllerAgent = new VLSPControllerAgent(id,
                                                                          controllerHost, 
                                                                          controllerPort,
                                                                          infoHost, 
                                                                          infoRemotePort,
                                                                          monitoringAddress,
                                                                          monitoringPort);
            vlspControllerAgent.init();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error while starting the VLSP Controller Agent " + ex.getMessage());
	}
    }

    @Override
    public boolean setCollectionRate(Rational dataRate) throws ControlServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rational getConnectionRate() throws ControlServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setMonitoringReportingEndpoint(ID id, String address, int port) throws ControlServiceException {
        LOGGER.info("Invoked setMonitoringReportingEndpoint");
        LOGGER.info("ID: " + id.toString());
        LOGGER.info("address: " + address);
        LOGGER.info("port: " + port);
        
        String uri;  
        JSONObject res;
        
        try {
            uri = VLSPURI + "/monitoring/forwarder?connect=1";
            res = rest.json(uri, put(content(""))).toObject();
            LOGGER.info(res.toString());
            
            uri = VLSPURI + "/monitoring/forwarder?host=" + address + "&port=" + port;
            res = rest.json(uri, put(content(""))).toObject();
            LOGGER.info(res.toString());
            
            if (res.getString("host").equals(address) && res.getInt("port")==port)
                return true;
            
        } catch (JSONException jex) {
            throw new ControlServiceException("setMonitoringReportingEndpoint exception: unexpected reply from the Monitoring System");
        } catch (IOException e) {
            throw new ControlServiceException("setMonitoringReportingEndpoint exception: no reply from the Monitoring System");
        }
        
        return false;
    }
    
}
