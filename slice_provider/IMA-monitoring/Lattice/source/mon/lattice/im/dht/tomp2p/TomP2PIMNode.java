package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.ID;
import java.io.Serializable;
import java.io.IOException;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import java.net.InetAddress;
import mon.lattice.im.dht.AbstractDHTIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An TomP2PIMNode is responsible for converting  DataSource, ControllableDataConsumer and Probe
 attributes into Hashtable keys and values for the TomP2PDistributedHashTable.
 * <p>
 * For example, with a given DataSource you get:
 * <ul>
 * <li> /datasource/datasource-id/attribute = value
 * </ul>
 * and with a given Probe you get:
 * <ul>
 * <li> /probe/probe-id/attribute = value
 * </ul>
 */
public class TomP2PIMNode extends AbstractDHTIMNode implements AnnounceEventListener {
    // The actual DHT
    TomP2PDistributedHashTable dht = null;
    
    AnnounceEventListener listener;

    // the local port
    int localPort = 0;
    
    int localUDPPort = 0;
    int localTCPPort = 0;
    
    // the remote port
    int remotePort = 0;
    
    static Logger LOGGER = LoggerFactory.getLogger(TomP2PIMNode.class);

    /**
     * Construct an IMNode, given a local port and a remote host
     * and a remote port.
     */
    public TomP2PIMNode(int myPort, String remHost, int remPort) {
	localPort = myPort;
	remoteHost = remHost;
	remotePort = remPort;
    }
    
    /**
     * Construct an IMNode, given local TCP and UDP ports and a remote port.
     */
    
    public TomP2PIMNode(int myUDPPort, int myTCPPort, int remPort) {
	localUDPPort = myUDPPort;
        localTCPPort = myTCPPort;
        
	remotePort = remPort;
        remoteHost = null; // will be initialized after connection
    }
    
    
    /**
     * Construct an IMNode, given local TCP and UDP ports and a remote port.
     */
    
    public TomP2PIMNode(int myUDPPort, int myTCPPort, String remHost, int remPort) {
	localUDPPort = myUDPPort;
        localTCPPort = myTCPPort;
        remoteHost = remHost;
        
	remotePort = remPort;
    }
    
    /**
     * Construct an IMNode, given a local port and a remote port.
     */
    
    public TomP2PIMNode(int myPort, int remPort) {
	localPort = myPort;
	remotePort = remPort;
        remoteHost = null; // will be initialized after connection
    }
    
    public TomP2PIMNode(int myPort) {
	localPort = myPort;
	remotePort = localPort;
        remoteHost = null; // will be initialized after connection
    }

    /**
     * Connect to the DHT peers.
     */
    @Override
    public boolean connect() {
        String remoteConnectedHost = null;
        
	try {
	    // only connect if we don't already have a DHT
	    if (dht == null) {
                if (localPort == remotePort) {
                    dht = new TomP2PDistributedHashTable(localPort);
                    remoteHost = dht.connect();
                }
                else {
                    if (localPort != 0)
                        dht = new TomP2PDistributedHashTable(localPort, InetAddress.getLocalHost());
                    else
                        dht = new TomP2PDistributedHashTable(localUDPPort, localTCPPort, InetAddress.getLocalHost());
                    if (remoteHost == null)
                       remoteConnectedHost = dht.connect(remotePort);
                    else
                       remoteConnectedHost = dht.connect(remoteHost, remotePort);
                }

                //setting this TomP2PIMNode as a AnnounceEventListener in the DHT
                dht.addAnnounceEventListener(this);
                
		return remoteConnectedHost != null;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    LOGGER.error("Connect failed: " + ioe);
	    if (dht != null) {
		try {
		    dht.close();
		} catch (IOException e) {
		}
		dht = null;
	    }
	    return false;
	}
    }

    /**
     * Disconnect from the DHT peers.
     */
    @Override
    public boolean disconnect() {
        if (dht != null) {
            try {
                dht.close();
                dht = null;
                return true;
            } catch (IOException e) {
                dht = null;
                return false;
            }
        }
        // was already disconnected so returning true anyway
        return true;
    }

    
    
//    @Override
//    public TomP2PIMNode addDataConsumer(ControllableDataConsumer dc) throws IOException {
//        putDHT("/dataconsumer/" + dc.getID() + "/name", dc.getName());  
//        
//        addDataConsumerControlEndPointInfo(dc);
//        
//        for (ControllableReporter r: dc.getReportersCollection()) {
//            if (r instanceof ControllableReporter)
//                addReporter((ControllableReporter)r);
//        }
//        
//        return this;
//    }
//    
//    
//    private TomP2PIMNode addDataConsumerControlEndPointInfo(ControllableDataConsumer dc) {
//        Map<String, String> controlEndPoint=dc.getControlPlane().getControlEndPoint();
//        LOGGER.debug(controlEndPoint.toString());    
//        StringBuilder controlEndPointInfo = new StringBuilder();
//        for (Map.Entry<String, String> entry : controlEndPoint.entrySet()) {
//            controlEndPointInfo.append(entry.getKey());
//            controlEndPointInfo.append(":");
//            controlEndPointInfo.append(entry.getValue());
//            controlEndPointInfo.append(";");
//        }
//        
//        //example -> type:zmq;address:localhost;port:2233
//        LOGGER.debug(controlEndPointInfo.toString());
//        putDHT("/dataconsumer/" + dc.getID() + "/controlEndPoint", controlEndPointInfo.toString());
//        
//        return this;
//    }
//    
//    
//    @Override
//    public TomP2PIMNode addDataConsumerInfo(ControllableDataConsumer dc) throws IOException {
//        // this maps the name to the ID
//	putDHT("/dataconsumer/name/" + dc.getName(), dc.getID().toString()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            putDHT("/dataconsumer/" + dc.getID() + "/pid", ((DefaultControllableDataConsumer) dc).getMyPID());       
//	return this;
//    }
//    
//    @Override
//    public TomP2PIMNode addReporter(ControllableReporter r) throws IOException {
//        putDHT("/reporter/" + r.getId() + "/name", r.getName());
//        putDHT("/reporter/" + r.getId() + "/dataconsumer", r.getDcId().toString());
//        return this;
//    }
//    
//    
//    /**
//     * Add data for a DataSource
//     */
//    @Override
//    public TomP2PIMNode addDataSource(DataSource ds) throws IOException {
//	putDHT("/datasource/" + ds.getID() + "/name", ds.getName());     
//        
//        addDataSourceControlEndPointInfo(ds);
//        
//	Collection<Probe> probes = ds.getProbes();
//
//	// skip through all probes
//	for (Probe aProbe : probes) {
//	    addProbe(aProbe);
//	}
//	    
//	return this;
//    }
//    
//    
//    private TomP2PIMNode addDataSourceControlEndPointInfo(DataSource ds) {
//        Map<String, String> controlEndPoint;
//
//        if (ds instanceof DockerDataSource && ((DockerDataSource) ds).getDataSourceConfigurator() != null) {
//            controlEndPoint = new HashMap<>();
//            String externalHost = ((DockerDataSource) ds).getDataSourceConfigurator().getDockerHost();
//            Integer controlPort = ((DockerDataSource) ds).getDataSourceConfigurator().getControlForwardedPort();
//            
//            controlEndPoint.put("address", externalHost);
//            controlEndPoint.put("port", controlPort.toString());
//            controlEndPoint.put("type", "socket/NAT");
//        }
//            
//        else 
//            controlEndPoint = ds.getControlPlane().getControlEndPoint();
//           
//        LOGGER.debug(controlEndPoint.toString());
//        
//        
//        StringBuilder controlEndPointInfo = new StringBuilder();
//        for (Map.Entry<String, String> entry : controlEndPoint.entrySet()) {
//            controlEndPointInfo.append(entry.getKey());
//            controlEndPointInfo.append(":");
//            controlEndPointInfo.append(entry.getValue());
//            controlEndPointInfo.append(";");
//        }
//        
//        LOGGER.debug(controlEndPointInfo.toString());
//        //example -> type:zmq;address:localhost;port:2233
//        putDHT("/datasource/" + ds.getID() + "/controlEndPoint", controlEndPointInfo.toString());
//        
//        return this;
//    }
//    
//    
//    
//    @Override
//    public TomP2PIMNode addDataSourceInfo(DataSource ds) throws IOException {
//        // this maps the name to the ID
//	putDHT("/datasource/name/" + ds.getName(), ds.getID().toString()); 
//        
//        if (ds instanceof ControllableDataSource)
//            putDHT("/datasource/" + ds.getID() + "/pid", ((ControllableDataSource) ds).getMyPID());       
//	return this;
//    }
//    
//    
//    /**
//     * Add data for a Probe.
//     */
//    @Override
//    public TomP2PIMNode addProbe(Probe aProbe) throws IOException {
//	// add probe's ref to its data source
//	// found through the ProbeManager
//	DataSource ds = (DataSource)aProbe.getProbeManager();
//	putDHT("/probe/" + aProbe.getID() + "/datasource", ds.getID().toString());
//
//	// add probe name to DHT
//	putDHT("/probe/" + aProbe.getID() + "/name", aProbe.getName());
//	putDHT("/probe/" + aProbe.getID() + "/datarate", aProbe.getDataRate().toString());
//	putDHT("/probe/" + aProbe.getID() + "/on", aProbe.isOn());
//	putDHT("/probe/" + aProbe.getID() + "/active", aProbe.isActive());
//
//	// now probe attributes
//	Collection<ProbeAttribute> attrs = aProbe.getAttributes();
//
//	putDHT("/probeattribute/" + aProbe.getID() + "/size", attrs.size());
//	// skip through all ProbeAttributes
//	for (ProbeAttribute attr : attrs) {
//	    addProbeAttribute(aProbe, attr);
//	}
//
//	return this;
//    }
//
//    /**
//     * Add data for a ProbeAttribute.
//     */
//    @Override
//    public TomP2PIMNode addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
//	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
//	    attr.getField() + "/";
//
//	putDHT(attrRoot + "name", attr.getName());
//	putDHT(attrRoot + "type", attr.getType().getCode());
//	putDHT(attrRoot + "units", attr.getUnits());
//
//	return this;
//
//    }
//
//    /*
//     * Modify stuff
//     */
//    @Override
//    public TomP2PIMNode modifyDataSource(DataSource ds) throws IOException {
//	// remove then add
//	throw new IOException("Not implemented yet!!");
//    }
//
//    @Override
//    public TomP2PIMNode modifyProbe(Probe p) throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//    @Override
//    public TomP2PIMNode modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//
//    /*
//     * Remove stuff
//     */
//    @Override
//    public TomP2PIMNode removeDataSource(DataSource ds) throws IOException {
//	remDHT("/datasource/" + ds.getID() + "/name");
//        remDHT("/datasource/" + ds.getID() + "/controlendpoint");
//        remDHT("/datasource/name/" + ds.getName()); 
//        
//        if (ds instanceof ControllableDataSource)
//            remDHT("/datasource/" + ds.getID() + "/pid");
//        
//	Collection<Probe> probes = ds.getProbes();
//
//	// skip through all probes
//	for (Probe aProbe : probes) {
//	    removeProbe(aProbe);
//	}
//	    
//	return this;
//    }
//
//    @Override
//    public TomP2PIMNode removeProbe(Probe aProbe) throws IOException {
//	// add probe's ref to its data source
//	// found through the ProbeManager
//	remDHT("/probe/" + aProbe.getID() + "/datasource");
//
//	// add probe name to DHT
//	remDHT("/probe/" + aProbe.getID() + "/name");
//	remDHT("/probe/" + aProbe.getID() + "/datarate");
//	remDHT("/probe/" + aProbe.getID() + "/on");
//	remDHT("/probe/" + aProbe.getID() + "/active");
//
//	// now probe attributes
//	Collection<ProbeAttribute> attrs = aProbe.getAttributes();
//
//	remDHT("/probeattribute/" + aProbe.getID() + "/size");
//	// skip through all ProbeAttributes
//	for (ProbeAttribute attr : attrs) {
//	    removeProbeAttribute(aProbe, attr);
//	}
//
//	return this;
//    }
//
//    @Override
//    public TomP2PIMNode removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
//	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
//	    attr.getField() + "/";
//
//	remDHT(attrRoot + "name");
//	remDHT(attrRoot + "type");
//	remDHT(attrRoot + "units");
//
//	return this;
//    }
//
//    
//    @Override
//    public TomP2PIMNode removeDataConsumer(ControllableDataConsumer dc) throws IOException {
//	remDHT("/dataconsumer/" + dc.getID() + "/name");
//        remDHT("/dataconsumer/" + dc.getID() + "/controlendpoint"); //we also need to remove the control end point
//        remDHT("/dataconsumer/name/" + dc.getName()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            remDHT("/dataconsumer/" + dc.getID() + "/pid");
//
//	// skip through all reporters
//	for (ControllableReporter r : dc.getReportersCollection()) {
//	    removeReporter((ControllableReporter)r);
//	}        
//	return this;
//    }
//    
//    
//    @Override
//    public TomP2PIMNode removeReporter(ControllableReporter r) throws IOException {
//        remDHT("/reporter/" + r.getId() + "/name");
//        remDHT("/reporter/" + r.getId() + "/dataconsumer");
//        return this;
//    }
//    
//
//    /**
//     * Lookup DataSource info
//     */
//    @Override
//    public Object getDataSourceInfo(ID dsID, String info) {
//	return getDHT("/datasource/" + dsID + "/" + info);
//    }
//
//    /**
//     * Lookup probe details.
//     */
//    @Override
//    public Object getProbeInfo(ID probeID, String info) {
//	return getDHT("/probe/" + probeID + "/" + info);
//    }
//
//    /**
//     * Lookup probe attribute details.
//     */
//    @Override
//    public Object getProbeAttributeInfo(ID probeID, Integer field, String info) {
//	return getDHT("/probeattribute/" + probeID + "/" + field + "/" + info);
//    }
//
//    /**
//     * Lookup ControllableDataConsumer info
//     */
//    @Override
//    public Object getDataConsumerInfo(ID dcID, String info) {
//	return getDHT("/dataconsumer/" + dcID + "/" + info);
//    }
//    
//    
//    /**
//     * Lookup Reporter info
//     */
//    @Override
//    public Object getReporterInfo(ID reporterID, String info) {
//	return getDHT("/reporter/" + reporterID + "/" + info);
//    }
    
    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeout) {
        try {
            String newKey = "/datasource/" + dataSourceID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("ContainsDataSource failed for DS " + dataSourceID + " " + ioe.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
        try {
            String newKey = "/dataconsumer/" + dataConsumerID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("ContainsDataConsumer failed for DS " + dataConsumerID + " " + ioe.getMessage());
            return false;
        }
    }

    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeout) {
        try {
            String newKey = "/controlleragent/" + controllerAgentID + "/name";
            return dht.contains(newKey, timeout);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsControllerAgent failed for agent " + controllerAgentID + " " + ioe.getMessage());
            return false;
        }     
    }

    /**
     * Put stuff into DHT.
     */
    @Override
    public boolean putDHT(String aKey, Serializable aValue) {
	try {
	    LOGGER.info("put " + aKey + " => " + aValue);
	    dht.put(aKey, aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("putDHT failed for key: '" + aKey + "' value: '" + aValue + "' " +ioe.getMessage());
	    return false;
	}
    }

    /**
     * Lookup info directly from the DHT.
     * @return the value if found, null otherwise
     */
    @Override
    public Object getDHT(String aKey) {
	try {
	    Object aValue = dht.get(aKey);
	    LOGGER.debug("get " + aKey +  " => " + aValue);
	    return aValue;
	} catch (IOException | ClassNotFoundException e) {
	    LOGGER.error("getDHT failed for key: '" + aKey + " " + e.getMessage());
	    return null;
	}
    }

    /**
     * Remove info from the DHT.
     * @return boolean
     */
    @Override
    public boolean remDHT(String aKey) {
	try {
	    dht.remove(aKey);
	    LOGGER.debug("removing " + aKey);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("remDHT failed for key: '" + aKey + "' " + ioe.getMessage());
	    return false;
	}
    }
    
    
    @Override
    public void sendMessage(AbstractAnnounceMessage m) {
        dht.announce(m);
    }

    
    @Override
    public String toString() {
        return dht.toString();
    }
    

    @Override
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        fireEvent(m);
        
    }
    
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        this.listener=l;
    }
    
    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}
