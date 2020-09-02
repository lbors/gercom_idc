/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.distribution.zmq;

import mon.lattice.distribution.Transmitting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQDataPublisher {
    String subscriberHost;
    int subscriberPort;
    
    Transmitting transmitting;
    
    ZMQ.Context context;
    ZMQ.Socket publisherSocket;
    

    public ZMQDataPublisher(Transmitting transmitting, String subscriberHost, int subscriberPort) {
        this.subscriberHost = subscriberHost;
        this.subscriberPort = subscriberPort;
        this.transmitting = transmitting;
        
        context = ZMQ.context(1);
        publisherSocket = context.socket(ZMQ.PUB);
    }
    
    public void connect() throws IOException {
        String uri = "tcp://" + subscriberHost + ":" + subscriberPort;
        publisherSocket.connect(uri);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {   
            }   
    }
    
    
    public void end() throws IOException {
        publisherSocket.setLinger(0);
        publisherSocket.close();
        context.term();
    }
    
    
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
        publisherSocket.sendMore("data".getBytes());
        publisherSocket.send(byteStream.toByteArray());

        if (transmitting != null) {
	    transmitting.transmitted(id);
        }

	return byteStream.size();
    } 
     
}
