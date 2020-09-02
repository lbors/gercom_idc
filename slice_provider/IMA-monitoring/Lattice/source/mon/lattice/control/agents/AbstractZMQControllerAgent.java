/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import mon.lattice.core.AbstractPlaneInteracter;
import mon.lattice.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractZMQControllerAgent extends AbstractPlaneInteracter implements ControllerAgent {

    ID myID;

    String name = "controller-agent";
    int pID = Integer.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);;
    
    InetSocketAddress ctrlPair;
    
    String remoteInfoHost;
    int remoteInfoPort;
    
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQControllerAgent.class);

    public AbstractZMQControllerAgent(String id, String controlHostAddress, 
                               int controlHostPort, 
                               String remoteInfoHost, 
                               int remoteInfoPort) 
                               throws UnknownHostException {
        
        this.ctrlPair = new InetSocketAddress(InetAddress.getByName(controlHostAddress), controlHostPort);
        this.remoteInfoHost = remoteInfoHost;
        this.remoteInfoPort = remoteInfoPort;
        myID = ID.fromString(id);
    }
    
    public ID getID() {
	return myID;
    }


    public void setID(ID id) {
	myID = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }
    
    public int getPID() {
        return pID;
    }

    @Override
    public boolean disconnect() {
        super.dennounce();
        return super.disconnect();
    }
    
    
    
    
    public void init() throws IOException {
        // ZMQ Info Plane
        ZMQInfoPlaneWithControlAgents zmqInfoPlane = new ZMQInfoPlaneWithControlAgents(remoteInfoHost, remoteInfoPort);
        zmqInfoPlane.setControllerAgent(this);
        super.setInfoPlane(zmqInfoPlane);
            
        // ZMQ Control Plane
        ZMQControlPlaneXDRConsumerWithControlAgents zmqControlPlaneConsumer = new ZMQControlPlaneXDRConsumerWithControlAgents(ctrlPair);
        zmqControlPlaneConsumer.setControllerAgent(this);
        super.setControlPlane(zmqControlPlaneConsumer);
        
	if (!super.connect()) {
            LOGGER.error("Error while connecting to the Planes");
            System.exit(1); //terminating as there was an error while connecting to the planes
        }
        
        LOGGER.info("Connected to the Info Plane using: " + super.getInfoPlane().getInfoRootHostname() + ":" + remoteInfoPort);
    }
    
    
}
