package mon.lattice.im.dht.tomp2p;

import mon.lattice.core.plane.AnnounceEventListener;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Random;
import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Distributed Hash Table implementation.
 * Values can be accessed and added from any one
 * of the distributed nodes.
 */

public class TomP2PDistributedHashTable implements ObjectDataReply {
    Peer peer; 

    PeerAddress rootPeer;
    
    int localPort = 0;
    
    int localUDPPort = 0;
    int localTCPPort = 0;
    
    AnnounceEventListener listener;
    
    static Logger LOGGER = LoggerFactory.getLogger(TomP2PDistributedHashTable.class);

    /**
     * Constructor a Peer on a specified localPort
     */
    
    public TomP2PDistributedHashTable(int port)  throws IOException {
	this.localPort = port;
        
        peer = new PeerMaker(new Number160(new Random())).setPorts(port)
                                                         .makeAndListen();
    }
    
    
    public TomP2PDistributedHashTable(int UDPPort, int TCPPort, InetAddress localAddress)  throws IOException {
	this.localUDPPort = UDPPort;
        this.localTCPPort = TCPPort;

        Bindings binding = new Bindings();
        binding.addAddress(localAddress); //binding the specified address
        
        peer = new PeerMaker(new Number160(new Random())).setTcpPort(TCPPort)
                                                         .setUdpPort(UDPPort)
                                                         .setBindings(binding)
                                                         .makeAndListen();
    }
    
    
    public TomP2PDistributedHashTable(int port, InetAddress localAddress)  throws IOException {
	this.localPort = port;

        Bindings binding = new Bindings();
        binding.addAddress(localAddress); //binding the specified address
        
        peer = new PeerMaker(new Number160(new Random())).setPorts(port)
                                                         .setBindings(binding)
                                                         .makeAndListen();
    }


    /**
     * Start the connection (used by the root node)
     */
    
    public String connect()  {
        setupReplyHandler(); // used by the root node to receive Announce/Deannounce messages
        return peer.getPeerAddress().getInetAddress().getHostName();
    }
    
    /**
     * Start the bootstrap process broadcasting to a given remote port
     */
    
    public String connect(int remPort) {
        FutureBootstrap bootstrap = peer.bootstrap().setBroadcast(true)
                                                    .setPorts(remPort)
                                                    .start();
        
        bootstrap.awaitUninterruptibly();
        if (bootstrap.getBootstrapTo() != null) {
            rootPeer = bootstrap.getBootstrapTo().iterator().next();
            peer.discover().setPeerAddress(rootPeer).start().awaitUninterruptibly();
        }
        
        return rootPeer.getInetAddress().getHostName();
    }
    
    
    public String connect(String remAddress, int remPort)  throws IOException {
        InetAddress remoteAddress = InetAddress.getByName(remAddress);
        
	FutureBootstrap bootstrap = peer.bootstrap().setInetAddress(remoteAddress)
                                                    .setPorts(remPort)
                                                    .start();
        
        bootstrap.awaitUninterruptibly();
        if (bootstrap.getBootstrapTo() != null) {
            rootPeer = bootstrap.getBootstrapTo().iterator().next();
            peer.discover().setPeerAddress(rootPeer).start().awaitUninterruptibly(); 
        }
        
        return rootPeer.getInetAddress().getHostName();
    }
    
    
    private void setupReplyHandler() {
        LOGGER.debug("Setting up reply handler for Announce/Deannounce messages");
        peer.setObjectDataReply(this);
    }
    
    // called back by the netty thread when a message is received on the DHT
    @Override
    public Object reply(PeerAddress sender, Object request) throws Exception {
        AbstractAnnounceMessage m = AbstractAnnounceMessage.fromString((String)request);
        LOGGER.debug("Received " + m.getMessageType() + " message for " + m.getEntity() + 
                     " with ID " + m.getEntityID() +
                     " from " + sender.getID());            
        this.fireEvent(AbstractAnnounceMessage.fromString((String)request));
            
        return "ACK"; // @ TODO: will return an ACK
    }
    
    
    /**
     * Close the peer connection
     */
    public void close() throws IOException {
        LOGGER.info("DHT Shutdown");
	peer.shutdown(); 
    }

    /**
     * Get an object out of the DHT.
     */
    public Object get(String aKey) throws IOException, ClassNotFoundException {
	Number160 keyHash = Number160.createHash(aKey);

	FutureDHT futureDHT = peer.get(keyHash).start();
        futureDHT.awaitUninterruptibly();
        
        if (futureDHT.isSuccess()) {
            return futureDHT.getData().getObject();
        }
        else
            throw new IOException("Object not found");
    }

    
    /**
     * Put an object into the DHT.
     */
    
    public TomP2PDistributedHashTable put(String aKey, Serializable aValue) throws IOException {
        Number160 keyHash = Number160.createHash(aKey);
        peer.put(keyHash).setData(new Data(aValue)).start().awaitUninterruptibly();
	return this;
    }
    

    /**
     * Does the DHT contain a particular Identifier.
     * Returns true if the map contains the specified key and false otherwise.
     */
    public boolean contains(String aKey, int timeout) throws IOException {
	Number160 keyHash = Number160.createHash(aKey);

	FutureDHT futureDHT = peer.get(keyHash).start();
        futureDHT.awaitUninterruptibly(timeout);
        
        if (futureDHT.isSuccess()) {
            return true;
        }
        else
            return false;
    }

    /**
     * Removes the mapping with the specified key.
     */
    public TomP2PDistributedHashTable remove(String aKey) throws IOException {
	Number160 keyHash = Number160.createHash(aKey);
        peer.remove(keyHash).start();
	return this;
    }
    
    
    public void announce(AbstractAnnounceMessage m) {
        try {
            LOGGER.debug("About to send " + m.getMessageType() + " message for this " + m.getEntity());
            FutureDHT futureSend = peer.send(rootPeer.getID()).setObject(AbstractAnnounceMessage.toString(m)).start();
            futureSend.awaitUninterruptibly();
            LOGGER.debug(m.getMessageType() + " message sent for this " + m.getEntity());
            
        } catch (IOException e) {
            LOGGER.error("Error while sending " + m.getMessageType() + "message " + e.getMessage());
        }
    }
  
    public String toString() {
            return peer.toString();
        }
    
    public void addAnnounceEventListener(AnnounceEventListener l) {
        listener = l;
    }

    protected void fireEvent(AbstractAnnounceMessage m) {
        listener.receivedAnnounceEvent(m);
    }
}
