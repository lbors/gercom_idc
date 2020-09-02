package mon.lattice.im.zmq;

import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
import mon.lattice.core.plane.InfoPlane;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractZMQInfoPlane implements InfoPlane  {
    
    ZMQSubscriber zmqSubscriber;
    
    ZMQPublisher zmqPublisher;
    
    static Logger LOGGER = LoggerFactory.getLogger(AbstractZMQInfoPlane.class);
    
    public AbstractZMQInfoPlane() {
    }

    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeout) {
        return zmqSubscriber.containsDataSource(dataSourceID, timeout); 
    }
    
    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeout) {
        return zmqSubscriber.containsDataConsumer(dataConsumerID, timeout);
    }

    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeout) {
        return zmqSubscriber.containsControllerAgent(controllerAgentID, timeout);
    }
    
    @Override
    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
        return zmqSubscriber.getDataSourceInfo(dataSource.getID(), info);
    }

    @Override
    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
        return zmqSubscriber.getDataSourceInfo(dataSourceID, info);
    }

    @Override
    public Object lookupProbeInfo(Probe probe, String info) {
        return zmqSubscriber.getProbeInfo(probe.getID(), info);
    }

    @Override
    public Object lookupProbeInfo(ID probeID, String info) {
        return zmqSubscriber.getProbeInfo(probeID, info);
    }

    @Override
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info) {
        return zmqSubscriber.getProbeAttributeInfo(probe.getID(), field, info);
    }

    @Override
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info) {
        return zmqSubscriber.getProbeAttributeInfo(probeID, field, info);
    }

    @Override
    public Object lookupDataConsumerInfo(ID dataConsumerID, String info) {
        return zmqSubscriber.getDataConsumerInfo(dataConsumerID, info);
    }
    
    @Override
    public Object lookupControllerAgentInfo(ID controllerAgentID, String info) {
        return zmqSubscriber.getControllerAgentInfo(controllerAgentID, info);
    }

    @Override
    public Object lookupReporterInfo(ID reporterID, String info) {
        return zmqSubscriber.getReporterInfo(reporterID, info);
    }
    
    @Override
    public Object lookupProbesOnDataSource(ID dataSourceID) {
        return zmqSubscriber.getProbesOnDataSource(dataSourceID);
    }
    
    @Override
    public boolean putInfo(String key, Serializable value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getInfo(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeInfo(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}