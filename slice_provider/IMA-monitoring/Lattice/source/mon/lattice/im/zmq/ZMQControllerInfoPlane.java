package mon.lattice.im.zmq;

import mon.lattice.im.delegate.ControlInformationManager;
import mon.lattice.im.delegate.InfoPlaneDelegate;
import mon.lattice.core.DataSource;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.Reporter;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.core.plane.InfoPlane;
import mon.lattice.im.delegate.InfoPlaneDelegateInteracter;
import mon.lattice.control.agents.ControllerAgent;
import mon.lattice.core.ID;

/**
 * A ZMQControllerInfoPlane is an InfoPlane implementation
 that mainly collects data from the Information Model data.
 */
public class ZMQControllerInfoPlane extends AbstractZMQInfoPlane implements InfoPlane, InfoPlaneDelegateInteracter, AnnounceEventListener  {
    private InfoPlaneDelegate infoPlaneDelegate;
    
    AnnounceEventListener listener;
    
    // The local port
    int port;

    ZMQProxy zmqProxy;
    
    /**
     * Constructor for subclasses.
     */
    private ZMQControllerInfoPlane() {
        setInfoPlaneDelegate(new ControlInformationManager(this));
        // setting the announce listener to the InfoPlaneDelegate
        listener = infoPlaneDelegate;
    }


    /**
     * Construct a ZMQInfoPlaneConsumer.
     */
    public ZMQControllerInfoPlane(int localPort) {
        this();
	port = localPort;
        zmqProxy = new ZMQProxy(port);
        zmqSubscriber = new ZMQSubscriber(zmqProxy.getInternalURI(), "info.", zmqProxy.getContext());
        zmqSubscriber.addAnnounceEventListener(listener);
    }
    
    
    /**
     * Connect to a delivery mechanism.
     */
    @Override
    public boolean connect() {
	return zmqProxy.startProxy() && zmqSubscriber.connectAndListen();
    }

    
    /**
     * Disconnect from a delivery mechanism.
     */
    @Override
    public boolean disconnect() {
	return zmqSubscriber.disconnect() && zmqProxy.stopProxy();
    }

    
    @Override
    public String getInfoRootHostname() {
        return zmqSubscriber.getRemoteHostname();
    }
   
    
   /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	return true;
    }

    
    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	return true;
    }

    
    /**
     * Consumer can never add a DataSource.
     * Return false
     */
    public boolean addDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never add a Probe.
     * Return false
     */
    public boolean addProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    /**
     * Consumer can never remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	return false;
    }

    /**
     * Consumer can never remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
	return false;
    }

    /**
     * Consumer can never remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	return false;
    }

    @Override
    public boolean addDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }
    
    @Override
    public boolean addControllerAgentInfo(ControllerAgent agent) {
        return false;
    }

    @Override
    public boolean addReporterInfo(Reporter r) {
        return false;
    }

    @Override
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc) {
        return false;
    }

    @Override
    public boolean removeReporterInfo(Reporter r) {
        return false;
    }
    
    @Override
    public boolean removeControllerAgentInfo(ControllerAgent agent) {
        return false;
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
    public void receivedAnnounceEvent(AbstractAnnounceMessage m) {
        infoPlaneDelegate.receivedAnnounceEvent(m);
    }
    
    @Override
    public void setInfoPlaneDelegate(InfoPlaneDelegate im) {
        this.infoPlaneDelegate = im;
    }

    @Override
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return this.infoPlaneDelegate;
    }
    
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        this.listener=l;
    }
    
    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
    
}