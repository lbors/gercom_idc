// PlanxIMNode.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.im.dht.planx;

import mon.lattice.core.ID;
import java.io.Serializable;
import java.io.IOException;
import java.math.BigInteger;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.im.dht.AbstractDHTIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An PlanxIMNode is responsible for converting  DataSource, ControllableDataConsumer and Probe
 attributes into Hashtable keys and values for the PlanxDistributedHashTable.
 * <p>
 * For example, with a given DataSource you get:
 * <ul>
 * <li> /datasource/datasource-id/attribute = value
 * </ul>
 * and with a given Probe you get:
 * <ul>
 * <li> /probe/probe-id/attribute = value
 * </ul>
 * @deprecated, use eu.fivegex.monitoring.im.dht.tomp2p.IMNode
 **/

public class PlanxIMNode extends AbstractDHTIMNode {
    // The actual DHT
    PlanxDistributedHashTable dht = null;

    // the local port
    int localPort = 0;

    // the remote host
    //String remoteHost;

    // the remote port
    int remotePort = 0;
    
    static Logger LOGGER = LoggerFactory.getLogger(PlanxIMNode.class);

    /**
     * Construct an IMNode, given a local port and a remote host
     * and a remote port.
     */
    public PlanxIMNode(int myPort, String remHost, int remPort) {
	localPort = myPort;
	remoteHost = remHost;
	remotePort = remPort;
    }

    /**
     * Connect to the DHT peers.
     */
    @Override
    public boolean connect() {
	try {
	    // only connect if we don't already have a DHT
	    if (dht == null) {
		dht = new PlanxDistributedHashTable(localPort);
		dht.connect(remoteHost, remotePort);
                
		LOGGER.info("IMNode: connect: " + localPort + " to " + remoteHost + "/" + remotePort);

		return true;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: connect failed: " + ioe);
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
//    public PlanxIMNode addDataConsumer(ControllableDataConsumer dc) throws IOException {
//        putDHT("/dataconsumer/" + dc.getID() + "/name", dc.getName());   
//        
//        // this might be slightly different approach from other entries as we serialise a whole JSON
//        // it might be modified to use separate entries
//        
//        JSONObject controlEndPoint = new JSONObject(dc.getControlPlane().getControlEndPoint());
//        putDHT("/dataconsumer/" + dc.getID() + "/controlEndPoint", controlEndPoint.toString());
//        
//        //Object [] reporters = dc.getReporters();
//        for (ControllableReporter r: dc.getReportersCollection()) {
//            if (r instanceof ControllableReporter)
//                addReporter((ControllableReporter)r);
//        }
//        
//        return this;
//    }
//    
//    
//    @Override
//    public PlanxIMNode addDataConsumerInfo(ControllableDataConsumer dc) throws IOException {
//        // this maps the name to the ID
//	putDHT("/dataconsumer/name/" + dc.getName(), dc.getID().toString()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            putDHT("/dataconsumer/" + dc.getID() + "/pid", ((DefaultControllableDataConsumer) dc).getMyPID());       
//	return this;
//    }
//    
//    @Override
//    public PlanxIMNode addReporter(ControllableReporter r) throws IOException {
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
//    public PlanxIMNode addDataSource(DataSource ds) throws IOException {
//        addDataSourceControlEndPointInfo(ds);
//        
//	Collection<Probe> probes = ds.getProbes();
//	// skip through all probes
//	for (Probe aProbe : probes) {
//	    addProbe(aProbe);
//	}
//	    
//	return this;
//    }
//    
//    @Override
//    public PlanxIMNode addDataSourceInfo(DataSource ds) throws IOException {
//        // this maps the name to the ID
//	putDHT("/datasource/name/" + ds.getName(), ds.getID().toString()); 
//        
//        if (ds instanceof ControllableDataSource)
//            putDHT("/datasource/" + ds.getID() + "/pid", ((ControllableDataSource) ds).getMyPID());       
//	return this;
//    }
//    
//    
//    private PlanxIMNode addDataSourceControlEndPointInfo(DataSource ds) {
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
//        StringBuilder controlEndPointInfo = new StringBuilder();
//        for (Map.Entry<String, String> entry : controlEndPoint.entrySet()) {
//            controlEndPointInfo.append(entry.getKey());
//            controlEndPointInfo.append(":");
//            controlEndPointInfo.append(entry.getValue());
//            controlEndPointInfo.append(";");
//        }
//        
//        //example -> type:zmq;address:localhost;port:2233
//        boolean putDHT = putDHT("/datasource/" + ds.getID() + "/controlEndPoint", controlEndPointInfo.toString());
//        LOGGER.info("added endpoint info => " + controlEndPointInfo.toString() + " result: " + putDHT);
//        
//        return this;
//    }
//    
//    
//    /**
//     * Add data for a Probe.
//     */
//    @Override
//    public PlanxIMNode addProbe(Probe aProbe) throws IOException {
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
//    public PlanxIMNode addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
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
//    public PlanxIMNode modifyDataSource(DataSource ds) throws IOException {
//	// remove then add
//	throw new IOException("Not implemented yet!!");
//    }
//
//    @Override
//    public PlanxIMNode modifyProbe(Probe p) throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//    @Override
//    public PlanxIMNode modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
//	throw new IOException("Not implemented yet!!");
//    }
//
//
//    /*
//     * Remove stuff
//     */
//    @Override
//    public PlanxIMNode removeDataSource(DataSource ds) throws IOException {
//	remDHT("/datasource/" + ds.getID() + "/name");
//        remDHT("/datasource/" + ds.getID() + "/controlEndPoint");
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
//    public PlanxIMNode removeProbe(Probe aProbe) throws IOException {
//	// add probe's ref to its data source
//	// found through the ProbeManager
//	DataSource ds = (DataSource)aProbe.getProbeManager();
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
//    public PlanxIMNode removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
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
//    public PlanxIMNode removeDataConsumer(ControllableDataConsumer dc) throws IOException {
//	remDHT("/dataconsumer/" + dc.getID() + "/name");
//        remDHT("/dataconsumer/" + dc.getID() + "/controlEndPoint"); //we also need to remove the control end point
//        remDHT("/dataconsumer/name/" + dc.getName()); 
//        
//        if (dc instanceof DefaultControllableDataConsumer)
//            remDHT("/dataconsumer/" + dc.getID() + "/pid"); 
//        
//        
//	//Object[] reporters = dc.getReporters();
//
//	// skip through all reporters
//	for (ControllableReporter r: dc.getReportersCollection()) {
//	    removeReporter((ControllableReporter)r);
//	}
//	    
//	return this;
//    }
//    
//    
//    @Override
//    public PlanxIMNode removeReporter(ControllableReporter r) throws IOException {
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
//    
//    
    
    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeOut) {
        try {
            BigInteger newKey = keyToBigInteger("/datasource/" + dataSourceID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsDataSource failed for DS " + dataSourceID);
            return false;
        }
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut) {
        try {
            BigInteger newKey = keyToBigInteger("/dataconsumer/" + dataConsumerID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsDataConsumer failed for DC " + dataConsumerID);
            return false;
        }
    }

    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeout) {
        try {
            BigInteger newKey = keyToBigInteger("/controlleragent/" + controllerAgentID + "/name");
            return dht.contains(newKey);
        } 
        catch (IOException ioe) {
            LOGGER.error("IMNode: containsControllerAgent failed for agent " + controllerAgentID);
            return false;
        }
    }
    

    /**
     * Put stuff into DHT.
     */
    @Override
    public boolean putDHT(String aKey, Serializable aValue) {
	try {
	    BigInteger newKey = keyToBigInteger(aKey);
	    //System.out.println("PlanxIMNode: put " + aKey + " K(" + newKey + ") => " + aValue);
	    dht.put(newKey, aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: putDHT failed for key: '" + aKey + "' value: '" + aValue + "'");
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
	    BigInteger newKey = keyToBigInteger(aKey);
	    Object aValue = dht.get(newKey);
	    //System.out.println("PlanxIMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return aValue;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: getDHT failed for key: '" + aKey + "'");
	    ioe.printStackTrace();
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
	    BigInteger newKey = keyToBigInteger(aKey);
	    dht.remove(newKey);
	    //System.out.println("PlanxIMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return true;
	} catch (IOException ioe) {
	    LOGGER.error("IMNode: remDHT failed for key: '" + aKey + "'");
	    return false;
	}
    }

    /**
     * Convert a key like /a/b/c/d into a fixed size big integer.
     */
    private BigInteger keyToBigInteger(String aKey) {
	// hash codes are signed ints
	int i = aKey.hashCode();
	// convert this into an unsigned long
	long l = 0xffffffffL & i;
	// create the BigInteger
	BigInteger result = BigInteger.valueOf(l);

	return result;
    }
    
    
    @Override
    public String toString() {
        return dht.toString();
    }

    @Override
    public void sendMessage(AbstractAnnounceMessage m) {
        throw new UnsupportedOperationException("Not supported on this IMNode implementation");
    }

    @Override
    public void addAnnounceEventListener(AnnounceEventListener l) {
            throw new UnsupportedOperationException("Not supported on this IMNode implementation");
    }
    
    
    
}
