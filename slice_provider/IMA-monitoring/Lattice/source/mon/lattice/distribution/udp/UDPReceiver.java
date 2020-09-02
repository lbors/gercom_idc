// UDPReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package mon.lattice.distribution.udp;

import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.TypeException;
import java.net.*;
import java.io.*;

/**
 * This is a UDP receiver for monitoring messages.
 */
public class UDPReceiver implements Runnable {
    /*
     * The receiver that interactes messages.
     */
    Receiving receiver = null;

    /*
     * The socket doing the listening
     */
    DatagramSocket socket;

    /*
     * A packet to receive
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    //InetSocketAddress address;

    InetAddress address;

    /*
     * The port
     */
    int port;

    /*
     * My thread.
     */
    Thread myThread;

    boolean threadRunning = false;

    /*
     * A default packet size.
     */
    static int PACKET_SIZE = 65535; // was 1500;

    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;

    /*
     * The InetSocketAddress of the last packet received
     */
    InetAddress srcAddr;

    /*
     * The length of the last packet received
     */
    int length;

    /*
     * The source port of the last packet received
     */
    int srcPort;
    
    /*
     * The last exception received.
     */
    Exception lastException;
    
    private String threadName="UDPReceiver";


    /**
     * Construct a receiver for a particular IP address
     */
    public UDPReceiver(Receiving receiver, InetSocketAddress ipAddr) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.address = ipAddr.getAddress();
	this.port = ipAddr.getPort();
        
	setUpSocket();
    }
    
    /**
     * Construct a receiver for a particular port 
     */
    public UDPReceiver(Receiving receiver, int port) throws IOException {
	//address = ipAddr;

	this.receiver = receiver;
	this.port = port;
     
        
	setUpSocket();
    }
    
    
    public UDPReceiver(Receiving receiver, int port, String name) throws IOException {
	this(receiver, port);
        this.threadName = name;
    }
    

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
        if (this.address == null)
            socket = new DatagramSocket(port);
        else
            socket = new DatagramSocket(port, address);

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    }

    /**
     * Join the address now
     * and start listening
     */
    public void listen()  throws IOException {
	// already bind to the address
	//socket.bind(address);
        
	// start the thread
	myThread = new Thread(this, this.threadName + "-" + Integer.toString(port));

	myThread.start();
    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void end()  throws IOException {
	// stop the thread
        threadRunning = false;

        socket.close();

        // disconnect: this shouldn't have any effect
        socket.disconnect();

    }

    /**
     * Receive a  replyMessage from the multicast address.
     */
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    socket.receive(packet);

            
	     /*System.out.println("FT: UDPReceiver Received " + packet.getLength() +
			   " bytes from "+ packet.getAddress() + 
			   ":" + packet.getPort()); 
            */

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(packet.getData(), 0, packet.getLength());

	    byteStream = theBytes;
	    srcAddr = packet.getAddress();
	    length = packet.getLength();
            srcPort = packet.getPort();

	    // Currently we reuse the packet.
	    // This could be dangerous.

	    // Maybe we should do this
	    // allocate an emtpy packet for use later
	    // packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

	    // Reset the packet size for next time
	    packet.setLength(PACKET_SIZE);


	    return true;
	} catch (Exception e) {
	    // something went wrong
	    lastException = e;
	    return false;
	}
    }
    
    public int sendMessage(ByteArrayOutputStream byteStream){
        DatagramPacket replyMessage = new DatagramPacket(byteStream.toByteArray(), byteStream.size(), srcAddr, srcPort);

        try {
            // now send it
            socket.send(replyMessage);
        } catch (IOException ex) {
            System.out.println("IO error occurred" + ex.getMessage());
            return -1;
        }
        return byteStream.size();
        
    }
    
    /**
     * The Runnable body
     */
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;
        
	while (threadRunning) {
            
	    if (receive()) {
		// construct the transmission meta data
		UDPTransmissionMetaData metaData = new UDPTransmissionMetaData(length, srcAddr, address, srcPort);

		// now notify the receiver with the replyMessage
		// and the address it came in on
		try {
		    receiver.received(byteStream, metaData);
		} catch (IOException ioe) {
		    receiver.error(ioe);
		} catch (TypeException te) {
		    receiver.error(te);
		} catch (Exception e) {
                      receiver.error(e);
                }
                  
	    } else {
		// the receive() failed
		// we find the exception in lastException
                // we notify the receiver only if the socket was not explicitly closed
                if (threadRunning)
                    receiver.error(lastException);
	    }
	}
    }
	    

}
