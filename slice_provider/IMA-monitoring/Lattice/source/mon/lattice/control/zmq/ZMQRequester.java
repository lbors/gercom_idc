package mon.lattice.control.zmq;

import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.Transmitting;
import mon.lattice.control.Transmitter;
import mon.lattice.control.SynchronousTransmitting;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.zeromq.ZMQ;

/**
 * This is a UDP transmitter for monitoring messages
 */
public final class ZMQRequester implements Transmitter {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    
    ZMQ.Context context;
    ZMQ.Socket transmitter;
    
    String identity;
    
    
    /**
    * Construct a transmitter from an existing ZMQ Context
    */
    public ZMQRequester(Transmitting transmitting, ZMQ.Context ctx) throws IOException {
        this.transmitting=transmitting;
        context = ctx;
        transmitter = context.socket(ZMQ.REQ);
        
        identity = Thread.currentThread().getName();
        transmitter.setIdentity(identity.getBytes(ZMQ.CHARSET));
    }
    
    
    @Override
    public void setTransmitting(Transmitting transmitting) {
        this.transmitting=transmitting;
    }
        

    /**
     * Connect to the internal socket
     */
    @Override
    public void connect()  throws IOException {
	transmitter.connect("inproc://frontend");
        //try {
        //    Thread.sleep(500);
        //} catch (InterruptedException e) {}
    }

    /**
     * End the connection to the internal socket
     */
    @Override
    public void end() throws IOException {
	transmitter.close();
    }
    
    
    public Object transmitAndWaitReply(ByteArrayOutputStream byteStream, ZMQControlMetaData MessageMetaData, int seqNo) throws IOException {
        String destination = MessageMetaData.getDestination();
        //System.out.println(" Sending: '" + "Request " + seqNo + "'" + " to " + destination);
        
        transmitter.sendMore(destination);
        transmitter.sendMore("");
        transmitter.send(byteStream.toByteArray());
        
        if (transmitting != null) {
            transmitting.transmitted(seqNo);
            
            // we block this thread until a reply message is received (timeout 5 secs)
            transmitter.setReceiveTimeOut(5000);
            
            // worker identity
            String sourceWorker = transmitter.recvStr();
            transmitter.recvStr(); // empty frame
            // actual reply
            byte [] reply = transmitter.recv();
            
            if (transmitting instanceof SynchronousTransmitting) {
                ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(reply, 0, reply.length);
                ZMQControlMetaData metaData = new ZMQControlMetaData(sourceWorker, reply.length);
                return ((SynchronousTransmitting)transmitting).receivedReply(theBytes, metaData, seqNo);
            }
        }
        
        return null;
    }   
}
