package mon.lattice.im.zmq;

import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.EntityType;
import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DeannounceMessage;
import java.util.HashMap;
import java.util.Map;
import mon.lattice.im.AbstractIMNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import mon.lattice.im.IMSubscriberNode;

/**
 * An ZMQSubscriber is responsible for receiving information about  
 * DataSources, DataConsumers, Probes and probes attributes on the InfoPlane 
 * using ZMQ.
**/

public class ZMQSubscriber extends AbstractIMNode implements IMSubscriberNode, Runnable {
    int remotePort = 0;
    int localPort = 0;
    
    ZMQ.Context context;
    ZMQ.Socket subscriberSocket;
    
    String internalURI;
    String messageFilter;
    
    boolean threadRunning = false;
    
    Map<ID, JSONObject> dataSources = new HashMap<>();
    Map<ID, JSONObject> probes = new HashMap<>();
    Map<ID, JSONObject> probeAttributes = new HashMap<>();
    
    Map<ID, JSONObject> dataConsumers = new HashMap<>();
    Map<ID, JSONObject> reporters = new HashMap<>();
    
    Map<ID, JSONObject> controllerAgents = new HashMap<>();
    
    AnnounceEventListener listener;
    
    Thread thread = new Thread(this, "zmq-info-subscriber");;
    
    static Logger LOGGER = LoggerFactory.getLogger(ZMQSubscriber.class);

    
    /**
     * Construct a ZMQSubscriber given a remote host, a remote 
     * port where connecting to and a message filter.
     */
    public ZMQSubscriber(String remHost, int remPort, String filter) {
        this(remHost, remPort, filter, ZMQ.context(1));
    } 
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQSubscriber(String remHost, int remPort, String filter, ZMQ.Context context) {
	remoteHost = remHost;
	remotePort = remPort;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
    }
    
    
    /**
     * Construct a ZMQInformationConsumer given a remote host, a remote 
     * port where connecting to, a message filter and an existing ZMQ.Context.
     */
    public ZMQSubscriber(String internalURI, String filter, ZMQ.Context context) {
	this.internalURI = internalURI;
        messageFilter = filter;
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
    }
    
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to 
     * and a message filter.
     */
    public ZMQSubscriber(int port, String filter) {
        this(port, filter, ZMQ.context(1));
    }
    
    
    /**
     * Construct a ZMQSubscriber given a local port where connecting to, 
     * a message filter and an existing ZMQ.Context.
     */
    
    public ZMQSubscriber(int port, String filter, ZMQ.Context context) {
	localPort = port;
        messageFilter = filter; 
        
        this.context = context;
        subscriberSocket = this.context.socket(ZMQ.SUB);
    }
    

    /**
     * Connect to the proxy Subscriber.
     */
    public boolean connectAndListen() {
        String uri;
        if (remoteHost != null && remotePort != 0)
            uri = "tcp://" + remoteHost + ":" + remotePort;
        else {
            uri = internalURI;
            // sleeping before connecting to the inproc socket
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
        
        subscriberSocket.connect(uri);
        thread.start();
        return true;
    }
    
    
    @Override
    public boolean connect() {
        return this.connectAndListen();
    }
    
    
    
    public boolean bindAndListen() {
        subscriberSocket.bind("tcp://*:" + localPort);
        thread.start();
        return true;
    }

    
    public ZMQ.Socket getSubscriberSocket() {
        return subscriberSocket;
    }
    

    /**
     * Disconnect from the DHT peers.
     */
    @Override
    public boolean disconnect() {
        threadRunning = false;
        subscriberSocket.setLinger(0);
        context.close();
        return true;
    }

    @Override
    public String getRemoteHostname() {
        return this.remoteHost;
    }
    
    
    @Override
    public boolean containsDataSource(ID dataSourceID, int timeOut) {
        return dataSources.containsKey(dataSourceID);
    }
    

    @Override
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut) {
        return dataConsumers.containsKey(dataConsumerID);
    }
    
    
    @Override
    public boolean containsControllerAgent(ID controllerAgentID, int timeOut) {
        return controllerAgents.containsKey(controllerAgentID);
    }
    
    
    @Override
    public Object getDataSourceInfo(ID dataSourceID, String info) {
        try {
            JSONObject dataSource = dataSources.get(dataSourceID);
            Object dataSourceInfo = dataSource.get(info);
            return dataSourceInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Data Source info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getProbeInfo(ID probeID, String info) {
        try {
            JSONObject probe = probes.get(probeID);
            Object probeInfo = probe.get(info);
            return probeInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    
    @Override
    public Object getProbeAttributeInfo(ID probeID, Integer field, String info) {
        try {
            JSONObject probeAttribute = probeAttributes.get(probeID);
            Object probeAttributeInfo = probeAttribute.getJSONObject(field.toString()).get(info);
            return probeAttributeInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Attribute info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getDataConsumerInfo(ID dataConsumerID, String info) {
        try {
            JSONObject dataConsumer = dataConsumers.get(dataConsumerID);
            Object dataConsumerInfo = dataConsumer.get(info);
            return dataConsumerInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Data Consumer info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    //@Override
    public Object getControllerAgentInfo(ID controllerAgentID, String info) {
        try {
            JSONObject controllerAgent = controllerAgents.get(controllerAgentID);
            Object controllerAgentInfo = controllerAgent.get(info);
            return controllerAgentInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Controller Agent info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    
    @Override
    public Object getReporterInfo(ID reporterID, String info) {
        try {
            JSONObject reporter = reporters.get(reporterID);
            Object reporterInfo = reporter.get(info);
            return reporterInfo;
        } catch (JSONException | NullPointerException e) {
            LOGGER.error("Error while retrieving Probe info '" + info + "': " + e.getMessage());
            return null;
        }
    }
    
    
    @Override
    public Object getProbesOnDataSource(ID dataSourceID) {
        JSONArray probesOnDS = new JSONArray();
        try {
            for (ID probeID : probes.keySet()) {
                if (probes.get(probeID).get("datasource").equals(dataSourceID.toString()))
                    probesOnDS.put(probeID.toString());
                }
                    
            } catch (JSONException e) {
                LOGGER.error("Error while retrieving Probe info" + e.getMessage());
                return null;
            }
        return probesOnDS;       
    }
    
    
    @Override
    public void run() {
        subscriberSocket.subscribe(messageFilter.getBytes());
        
        LOGGER.info("Listening for messages");
        
        threadRunning = true;
        try {
            while (threadRunning) {
                String header = subscriberSocket.recvStr();
                String content = subscriberSocket.recvStr();
                LOGGER.debug(header + " : " + content);
                messageHandler(content);
            }
            } catch (ZMQException e) {
                subscriberSocket.close();
                LOGGER.debug(e.getMessage());
            }
    }
    
    
    
    private void messageHandler(String message) {
        JSONObject msgObj;
        try {
            msgObj = new JSONObject(message);
            ID entityID = null;
            String field = null;
            
            String entityType = msgObj.getString("entity");
            String operation = msgObj.getString("operation");
            
            if (!entityType.equals("probeattribute")) {
                entityID = ID.fromString(msgObj.getJSONObject("info").getString("id"));
            }
            else
                field = msgObj.getJSONObject("info").getString("field");
            
            switch(entityType) {
                case "datasource":  
                    if (operation.equals("add")) {
                        dataSources.put(entityID, msgObj.getJSONObject("info"));
                        sendMessage(new AnnounceMessage(entityID, EntityType.DATASOURCE));
                    }
                    else if (operation.equals("remove")) {
                        dataSources.remove(entityID);
                        sendMessage(new DeannounceMessage(entityID, EntityType.DATASOURCE));
                    }
                    
                    LOGGER.trace("datasource map:\n");
                    for (ID id: dataSources.keySet())
                        LOGGER.trace(dataSources.get(id).toString(1));
                    
                    break;
                        
                case "probe":
                    if (operation.equals("add")) {
                        probes.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        probes.remove(entityID);
                    }
                    
                    
                    LOGGER.trace("probe map:\n");
                    for (ID id: probes.keySet())
                        LOGGER.trace(probes.get(id).toString(1));
                    
                    break;
                        
                case "probeattribute":
                    ID probeID = ID.fromString(msgObj.getJSONObject("info").getString("probe"));
                    JSONObject attributes;
                    
                    if (operation.equals("add")) {
                        
                        if (!probeAttributes.containsKey(probeID)) { 
                           attributes = new JSONObject();
                           attributes.put(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                           probeAttributes.put(probeID, attributes);
                        }
                        else {
                            attributes = probeAttributes.get(probeID);
                            attributes.accumulate(field, msgObj.getJSONObject("info").getJSONObject("properties"));
                            probeAttributes.put(probeID, attributes);
                        }
                        
                    }
                    
                    else if (operation.equals("remove")) {
                        attributes = probeAttributes.get(probeID);
                        if (attributes == null)
                            break;
                        if (attributes.has(field))
                            attributes.remove(field);
                        if (!attributes.keys().hasNext())
                            probeAttributes.remove(probeID);
                    }
                    
                    LOGGER.trace("probeattribute map:\n");
                    for (ID id: probeAttributes.keySet())
                        LOGGER.trace(probeAttributes.get(id).toString(1));
                    
                    break;
                    
                        
                case "dataconsumer":  
                    if (operation.equals("add")) {
                        dataConsumers.put(entityID, msgObj.getJSONObject("info"));
                        sendMessage(new AnnounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    else if (operation.equals("remove")) {
                        dataConsumers.remove(entityID);
                        sendMessage(new DeannounceMessage(entityID, EntityType.DATACONSUMER));
                    }
                    break;
                        
                case "reporter":  
                    if (operation.equals("add")) {
                        reporters.put(entityID, msgObj.getJSONObject("info"));
                    }
                    else if (operation.equals("remove")) {
                        reporters.remove(entityID);
                    }
                    
                    
                    LOGGER.debug("reporters map:\n");
                    for (ID id: reporters.keySet())
                        LOGGER.debug(reporters.get(id).toString(1));
                    
                    break;   
                    
                case "controlleragent":
                    if (operation.equals("add")) {
                        controllerAgents.put(entityID, msgObj.getJSONObject("info"));
                        sendMessage(new AnnounceMessage(entityID, EntityType.CONTROLLERAGENT));
                    }
                    else if (operation.equals("remove")) {
                        controllerAgents.remove(entityID);
                        sendMessage(new DeannounceMessage(entityID, EntityType.CONTROLLERAGENT));
                    }
            }
            
            
        } catch (JSONException e) {
            LOGGER.error("Error while deserializing received message" + e.getMessage());
        } 
    }
    
    
    
    @Override
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    @Override
    public void sendMessage(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
}
