/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.bidirectional;

import mon.lattice.core.AbstractProbe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.Rational;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DelayProbeClient extends AbstractProbe implements Probe  {
    
    DatagramSocket socket;
    
    private static final int MAX_TIMEOUT = 1000;
    private static final int REQUESTS_NUM = 10;
    InetAddress serverHost;
    int serverPort;

    public DelayProbeClient(String name, String serverHost, String serverPort) throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = Integer.valueOf(serverPort);
        
        setName(name);
        setDataRate(new Rational(360, 1));

        addProbeAttribute(new DefaultProbeAttribute(0, "link", ProbeAttributeType.STRING, "id"));
        addProbeAttribute(new DefaultProbeAttribute(1, "delay", ProbeAttributeType.FLOAT, "milliseconds"));
    }
    
    private float measureMeanDelay() {
        DatagramPacket ping;
	int sequence_number = 0;
        int timedOut = 0;
        
        float delaySum = 0;
        
	// Processing 10 requests loop.
	while (sequence_number < REQUESTS_NUM) {            
            long nsSend = System.nanoTime();
                        
            // Create string to send, and transfer i to a Byte Array
            String str = "PING " + sequence_number + " " + nsSend + " \n";
            byte[] buf;
            buf = str.getBytes();
            
            // Create a datagram packet to send as an UDP packet.
            ping = new DatagramPacket(buf, buf.length, serverHost, serverPort);
            
            try {
                // Send the Ping datagram to the server
                socket.send(ping);
                
		// Set up the timeout 1000 ms = 1 sec
		socket.setSoTimeout(MAX_TIMEOUT);
                
		// Set up an UDP packet for receiving
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                
		// Try to receive the response from the ping
		socket.receive(response);

		long nsReceived = System.nanoTime();
                
                float delay = (float)(nsReceived - nsSend)/(1000*1000);
                
		// Accumulate the delay in msec
		delaySum += delay;
                
		} catch (IOException e) {
                    // Print which packet has timed out
                    timedOut++;
                    LoggerFactory.getLogger(DelayProbeClient.class).debug("Timeout for packet " + sequence_number);
		}
            
		// next packet
		sequence_number ++;
	}
        
        return delaySum/(REQUESTS_NUM - timedOut)/2; //filtering out the timed out pings
        
    }
    
    
    @Override
    public ProbeMeasurement collect() {
        int attrCount = 2;
        ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(attrCount);
        
        try {
            list.add(new DefaultProbeValue(0, "linkId")); // TODO: this has to be passed as parameter of the probe!
            list.add(new DefaultProbeValue(1, measureMeanDelay()));
        } catch (Exception e) {
            return null;
        }
        return new ProducerMeasurement(this, list, "Link");	
    }  
}
