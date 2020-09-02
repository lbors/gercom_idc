/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.dht;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import mon.lattice.appl.dataconsumers.DefaultControllableDataConsumer;
import mon.lattice.appl.datasources.DockerDataSource;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.DataSource;
import mon.lattice.core.ID;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.im.AbstractIMNode;
import mon.lattice.im.IMBasicNode;
import mon.lattice.im.IMPublisherNode;
import mon.lattice.im.IMSubscriberNode;
import static mon.lattice.im.dht.AbstractDHTInfoPlane.LOGGER;
import mon.lattice.control.agents.ControllerAgent;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractDHTIMNode extends AbstractIMNode implements IMPublisherNode, IMSubscriberNode, DHTInteractor {
    
    @Override
    public AbstractDHTIMNode addDataConsumer(ControllableDataConsumer dc) throws IOException {
        putDHT("/dataconsumer/" + dc.getID() + "/name", dc.getName());  
        
        addDataConsumerControlEndPointInfo(dc);
        
        for (ControllableReporter r: dc.getReportersCollection()) {
            if (r instanceof ControllableReporter)
                addReporter((ControllableReporter)r);
        }
        
        return this;
    }
    
    
    private AbstractDHTIMNode addDataConsumerControlEndPointInfo(ControllableDataConsumer dc) {
        Map<String, String> controlEndPoint=dc.getControlPlane().getControlEndPoint();
        LOGGER.debug(controlEndPoint.toString());    
        StringBuilder controlEndPointInfo = new StringBuilder();
        for (Map.Entry<String, String> entry : controlEndPoint.entrySet()) {
            controlEndPointInfo.append(entry.getKey());
            controlEndPointInfo.append(":");
            controlEndPointInfo.append(entry.getValue());
            controlEndPointInfo.append(";");
        }
        
        //example -> type:zmq;address:localhost;port:2233
        LOGGER.debug(controlEndPointInfo.toString());
        putDHT("/dataconsumer/" + dc.getID() + "/controlEndPoint", controlEndPointInfo.toString());
        
        return this;
    }
    
    
    @Override
    public AbstractDHTIMNode addDataConsumerInfo(ControllableDataConsumer dc) throws IOException {
        // this maps the name to the ID
	putDHT("/dataconsumer/name/" + dc.getName(), dc.getID().toString()); 
        
        if (dc instanceof DefaultControllableDataConsumer)
            putDHT("/dataconsumer/" + dc.getID() + "/pid", ((DefaultControllableDataConsumer) dc).getMyPID());       
	return this;
    }
    
    @Override
    public AbstractDHTIMNode addReporter(ControllableReporter r) throws IOException {
        putDHT("/reporter/" + r.getId() + "/name", r.getName());
        putDHT("/reporter/" + r.getId() + "/dataconsumer", r.getDcId().toString());
        return this;
    }
    
    
    /**
     * Add data for a DataSource
     */
    @Override
    public AbstractDHTIMNode addDataSource(DataSource ds) throws IOException {
	putDHT("/datasource/" + ds.getID() + "/name", ds.getName());     
        
        addDataSourceControlEndPointInfo(ds);
        
	Collection<Probe> probes = ds.getProbes();

	// skip through all probes
	for (Probe aProbe : probes) {
	    addProbe(aProbe);
	}
	    
	return this;
    }
    
    
    private AbstractDHTIMNode addDataSourceControlEndPointInfo(DataSource ds) {
        Map<String, String> controlEndPoint;

        if (ds instanceof DockerDataSource && ((DockerDataSource) ds).getDataSourceConfigurator() != null) {
            controlEndPoint = new HashMap<>();
            String externalHost = ((DockerDataSource) ds).getDataSourceConfigurator().getDockerHost();
            Integer controlPort = ((DockerDataSource) ds).getDataSourceConfigurator().getControlForwardedPort();
            
            controlEndPoint.put("address", externalHost);
            controlEndPoint.put("port", controlPort.toString());
            controlEndPoint.put("type", "socket/NAT");
        }
            
        else 
            controlEndPoint = ds.getControlPlane().getControlEndPoint();
           
        LOGGER.debug(controlEndPoint.toString());
        
        
        StringBuilder controlEndPointInfo = new StringBuilder();
        for (Map.Entry<String, String> entry : controlEndPoint.entrySet()) {
            controlEndPointInfo.append(entry.getKey());
            controlEndPointInfo.append(":");
            controlEndPointInfo.append(entry.getValue());
            controlEndPointInfo.append(";");
        }
        
        LOGGER.debug(controlEndPointInfo.toString());
        //example -> type:zmq;address:localhost;port:2233
        putDHT("/datasource/" + ds.getID() + "/controlEndPoint", controlEndPointInfo.toString());
        
        return this;
    }
    
    
    
    @Override
    public AbstractDHTIMNode addDataSourceInfo(DataSource ds) throws IOException {
        // this maps the name to the ID
	putDHT("/datasource/name/" + ds.getName(), ds.getID().toString()); 
        
        if (ds instanceof ControllableDataSource)
            putDHT("/datasource/" + ds.getID() + "/pid", ((ControllableDataSource) ds).getMyPID());       
	return this;
    }
    
    
    /**
     * Add data for a Probe.
     */
    @Override
    public AbstractDHTIMNode addProbe(Probe aProbe) throws IOException {
	// add probe's ref to its data source
	// found through the ProbeManager
	DataSource ds = (DataSource)aProbe.getProbeManager();
	putDHT("/probe/" + aProbe.getID() + "/datasource", ds.getID().toString());

	// add probe name to DHT
	putDHT("/probe/" + aProbe.getID() + "/name", aProbe.getName());
	putDHT("/probe/" + aProbe.getID() + "/datarate", aProbe.getDataRate().toString());
	putDHT("/probe/" + aProbe.getID() + "/on", aProbe.isOn());
	putDHT("/probe/" + aProbe.getID() + "/active", aProbe.isActive());

	// now probe attributes
	Collection<ProbeAttribute> attrs = aProbe.getAttributes();

	putDHT("/probeattribute/" + aProbe.getID() + "/size", attrs.size());
	// skip through all ProbeAttributes
	for (ProbeAttribute attr : attrs) {
	    addProbeAttribute(aProbe, attr);
	}

	return this;
    }

    /**
     * Add data for a ProbeAttribute.
     */
    @Override
    public AbstractDHTIMNode addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
	    attr.getField() + "/";

	putDHT(attrRoot + "name", attr.getName());
	putDHT(attrRoot + "type", attr.getType().getCode());
	putDHT(attrRoot + "units", attr.getUnits());

	return this;

    }
    
    /*
     * Modify stuff
     */
    @Override
    public AbstractDHTIMNode modifyDataSource(DataSource ds) throws IOException {
	// remove then add
	throw new IOException("Not implemented yet!!");
    }

    @Override
    public AbstractDHTIMNode modifyProbe(Probe p) throws IOException {
	throw new IOException("Not implemented yet!!");
    }

    @Override
    public AbstractDHTIMNode modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
	throw new IOException("Not implemented yet!!");
    }


    /*
     * Remove stuff
     */
    @Override
    public AbstractDHTIMNode removeDataSource(DataSource ds) throws IOException {
	remDHT("/datasource/" + ds.getID() + "/name");
        remDHT("/datasource/" + ds.getID() + "/controlendpoint");
        remDHT("/datasource/name/" + ds.getName()); 
        
        if (ds instanceof ControllableDataSource)
            remDHT("/datasource/" + ds.getID() + "/pid");
        
	Collection<Probe> probes = ds.getProbes();

	// skip through all probes
	for (Probe aProbe : probes) {
	    removeProbe(aProbe);
	}
	    
	return this;
    }

    @Override
    public AbstractDHTIMNode removeProbe(Probe aProbe) throws IOException {
	// add probe's ref to its data source
	// found through the ProbeManager
	remDHT("/probe/" + aProbe.getID() + "/datasource");

	// add probe name to DHT
	remDHT("/probe/" + aProbe.getID() + "/name");
	remDHT("/probe/" + aProbe.getID() + "/datarate");
	remDHT("/probe/" + aProbe.getID() + "/on");
	remDHT("/probe/" + aProbe.getID() + "/active");

	// now probe attributes
	Collection<ProbeAttribute> attrs = aProbe.getAttributes();

	remDHT("/probeattribute/" + aProbe.getID() + "/size");
	// skip through all ProbeAttributes
	for (ProbeAttribute attr : attrs) {
	    removeProbeAttribute(aProbe, attr);
	}

	return this;
    }

    @Override
    public AbstractDHTIMNode removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
	    attr.getField() + "/";

	remDHT(attrRoot + "name");
	remDHT(attrRoot + "type");
	remDHT(attrRoot + "units");

	return this;
    }

    
    @Override
    public AbstractDHTIMNode removeDataConsumer(ControllableDataConsumer dc) throws IOException {
	remDHT("/dataconsumer/" + dc.getID() + "/name");
        remDHT("/dataconsumer/" + dc.getID() + "/controlendpoint"); //we also need to remove the control end point
        remDHT("/dataconsumer/name/" + dc.getName()); 
        
        if (dc instanceof DefaultControllableDataConsumer)
            remDHT("/dataconsumer/" + dc.getID() + "/pid");

	// skip through all reporters
	for (ControllableReporter r : dc.getReportersCollection()) {
	    removeReporter((ControllableReporter)r);
	}        
	return this;
    }
    
    
    @Override
    public AbstractDHTIMNode removeReporter(ControllableReporter r) throws IOException {
        remDHT("/reporter/" + r.getId() + "/name");
        remDHT("/reporter/" + r.getId() + "/dataconsumer");
        return this;
    }
    

    /**
     * Lookup DataSource info
     */
    @Override
    public Object getDataSourceInfo(ID dsID, String info) {
	return getDHT("/datasource/" + dsID + "/" + info);
    }

    /**
     * Lookup probe details.
     */
    @Override
    public Object getProbeInfo(ID probeID, String info) {
	return getDHT("/probe/" + probeID + "/" + info);
    }

    /**
     * Lookup probe attribute details.
     */
    @Override
    public Object getProbeAttributeInfo(ID probeID, Integer field, String info) {
	return getDHT("/probeattribute/" + probeID + "/" + field + "/" + info);
    }

    /**
     * Lookup ControllableDataConsumer info
     */
    @Override
    public Object getDataConsumerInfo(ID dcID, String info) {
	return getDHT("/dataconsumer/" + dcID + "/" + info);
    }
    
    
    /**
     * Lookup Reporter info
     */
    @Override
    public Object getReporterInfo(ID reporterID, String info) {
	return getDHT("/reporter/" + reporterID + "/" + info);
    }

    
    @Override
    public Object getProbesOnDataSource(ID dsID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    @Override
    public AbstractDHTIMNode addControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IMBasicNode removeControllerAgent(ControllerAgent agent) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
