/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.unidirectional;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class UDPMgmReceiver implements Runnable {
    
    DatagramSocket mgmSocket;
    int packets;
    int timeout;
    
    Thread t;
    Boolean isRunning = false;
    
    private Logger LOGGER = LoggerFactory.getLogger(UDPMgmReceiver.class);
    
    
    public UDPMgmReceiver(InetAddress mgmLocalAddr, int mgmLocalPort, int packets, int timeout) throws SocketException, UnknownHostException {
        mgmSocket = new DatagramSocket(mgmLocalPort, mgmLocalAddr);
        this.packets = packets;
        this.timeout = timeout;
    }
    
    
    public void start() {
        if (!isRunning) {
            isRunning = true;
            t = new Thread(this);
            t.start();
        }
    }
    
    
    public void stop() {
        if (isRunning) {
            isRunning = false;
            mgmSocket.close();
        }
    }
    
    
    
    private int mgmReceiveAndReply() {
        DatagramPacket pingPacket;
        DatagramPacket replyPacket;
        
        Integer sequenceNumber = 0;
        Integer timedOut = 0;
        Integer received = 0;
        
        do {
            try {
                byte[] rcvBuf = new byte[1024];
                pingPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                mgmSocket.receive(pingPacket);
                long nsReceived = System.nanoTime();
                received++;
                
                String pingPayload = new String(pingPacket.getData());
                String [] pingPayloadFields = pingPayload.split(" ");

                sequenceNumber = Integer.valueOf(pingPayloadFields[1]);
                
                String nsDestinationSent = pingPayloadFields[2];
                LOGGER.debug("sequenceNumber => " + sequenceNumber);
                                
                InetAddress destinationHost = pingPacket.getAddress();
                int destinationPort = pingPacket.getPort();
                String replyPayload = "REPLY " + sequenceNumber + " " + nsDestinationSent + " " + nsReceived + " \n";
                byte [] sendBuf = replyPayload.getBytes();
                replyPacket = new DatagramPacket(sendBuf, sendBuf.length, destinationHost, destinationPort);
                
                mgmSocket.send(replyPacket);
                
                if (mgmSocket.getSoTimeout() == 0) {
                    mgmSocket.setSoTimeout(timeout);
                }
                
            } catch (SocketTimeoutException te) {
                LOGGER.warn("Timeout for packet: " + (sequenceNumber + 1));
                timedOut++; 
                if (timedOut == packets/2)
                    return timedOut;
                
            } catch (SocketException so) {
                LOGGER.info("Socket was closed: shutting down");
                return -1;
            }
              catch (IOException ioe) {
                LOGGER.error("Error while sending REPLY message to PING: " + sequenceNumber);
            }
        } while (received < packets);
        
        try {
            mgmSocket.setSoTimeout(0);
        } catch (SocketException so) {
            LOGGER.info("Socket was closed: shutting down");
            return -1;
        }
        
        return timedOut;
    }
    
    
    
    @Override
    public void run() {
        Integer timedOut;
        while (isRunning) {
            timedOut = mgmReceiveAndReply();
            if (timedOut == 0)
                LOGGER.info("Time offset was calculated with no packet loss");
            else
                if (timedOut > 0)
                    LOGGER.warn("Time offset was calculated with " + timedOut + " timed out packets");
        }
    }
    
}
