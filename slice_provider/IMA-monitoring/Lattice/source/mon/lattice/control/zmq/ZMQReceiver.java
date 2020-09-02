/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.core.TypeException;
import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.Receiving;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 *
 * @author uceeftu
 */
public class ZMQReceiver implements Runnable {
    Receiving receiver = null;
    ZMQ.Context context;
    ZMQ.Socket receiverSocket;
    String routerAddress;
    int routerPort;
    
    Thread myThread;
    String threadName = "ZMQReceiver";
    boolean threadRunning = false;
    
    ByteArrayInputStream byteStream;
    
    Exception lastException;
    
    String srcAddr;
    int length;
    
    
    public ZMQReceiver(Receiving receiver, String routerAddress, int port) {
        this.receiver = receiver;
        this.context = ZMQ.context(1);
        this.receiverSocket = context.socket(ZMQ.REQ);
        this.routerAddress = routerAddress;
        this.routerPort = port;
    }
    
    public void setIdentity(String identity) {
        receiverSocket.setIdentity(identity.getBytes(ZMQ.CHARSET));
    }
    
    public void connect() {
        receiverSocket.connect("tcp://" + routerAddress + ":" + routerPort); // possible need of sleeping
    }

    public void listen()  throws IOException {        
	// start the thread
	myThread = new Thread(this, this.threadName);
	myThread.start();
    }
    
    
    public void end()  throws IOException {
        threadRunning = false;
        receiverSocket.setLinger(0);
        context.term();
    }
    
    
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;
            
            // now getting the client replyToClientAddress
            String replyToClientAddress = receiverSocket.recvStr();
            receiverSocket.recvStr(); // emtpy delimiter frame

            //  Get request
            byte[] request = receiverSocket.recv();
            
	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(request, 0, request.length);

	    byteStream = theBytes;
	    srcAddr = replyToClientAddress;
	    length = request.length;
            
	    return true;
            
	} catch (ZMQException e) {
            LoggerFactory.getLogger(ZMQReceiver.class).debug(e.getMessage());
            receiverSocket.close();
            lastException = e;
            return false; // generated when closing the context
        } 
         catch (Exception e) {
	    // something went wrong
            LoggerFactory.getLogger(ZMQReceiver.class).debug(e.getMessage());
	    lastException = e;
	    return false;
	}
    }
    
    
    public int sendMessage(ByteArrayOutputStream byteStream) {
       receiverSocket.sendMore(srcAddr);
       receiverSocket.sendMore(""); // delimiter
       // actual reply message
       receiverSocket.send(byteStream.toByteArray());
       return byteStream.size(); 
    }
    
    /**
     * The Runnable body
     */
    @Override
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;
        
        receiverSocket.send("READY");
        
	while (threadRunning) {
            
	    if (receive()) {
		// construct the transmission meta data
		ZMQControlMetaData metaData = new ZMQControlMetaData(srcAddr, length);

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
                if (threadRunning) {
                    receiver.error(lastException);
                }
	    }
	}
    }    
    
}
