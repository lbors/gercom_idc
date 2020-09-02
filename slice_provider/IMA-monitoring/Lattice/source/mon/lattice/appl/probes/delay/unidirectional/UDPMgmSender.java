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
public class UDPMgmSender implements Runnable {
    DatagramSocket mgmSocket;
    
    InetAddress mgmSourceAddr;
    int mgmSourcePort;
    
    int packets;
    int timeout;
    int interval; // every interval seconds
    
    Thread t;
    Boolean isRunning = false;
    
    Long timeOffset;
    
    private Logger LOGGER = LoggerFactory.getLogger(UDPMgmSender.class);
    
    
    public UDPMgmSender(InetAddress mgmLocalAddr, int mgmLocalPort, InetAddress mgmSourceAddr, int mgmSourcePort, int packets, int timeout, int interval) throws SocketException, UnknownHostException {
        mgmSocket = new DatagramSocket(mgmLocalPort, mgmLocalAddr);
        
        this.mgmSourceAddr = mgmSourceAddr;
        this.mgmSourcePort = mgmSourcePort;
        
        this.packets = packets;
        this.timeout = timeout;
        this.interval = interval * 1000; // converting to milliseconds
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
    
    
    private Long measureTimeOffset() {
        
        DatagramPacket pingPacket;
        DatagramPacket replyPacket;
        
        
	int sequenceNumber = 0;
        int timedOut = 0;

        long timeOffsetSum = 0;
        
        LOGGER.info("Measuring time offset");
	while (sequenceNumber < packets) {            
            long nsSend = System.nanoTime();
            
            String pingPayload = "PING " + sequenceNumber + " " + nsSend + " \n";
            byte[] sendBuf = pingPayload.getBytes();
            
            pingPacket = new DatagramPacket(sendBuf, sendBuf.length, mgmSourceAddr, mgmSourcePort);
            
            try {
                mgmSocket.send(pingPacket);
                
                byte[] rcvBuf = new byte[1024];
		replyPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                mgmSocket.setSoTimeout(timeout);
		mgmSocket.receive(replyPacket);

		long nsReceived = System.nanoTime();
                
                String pongPayload = new String(replyPacket.getData());
                String [] pongPayloadFields = pongPayload.split(" ");
                
                Integer receivedSequenceNumber = Integer.valueOf(pongPayloadFields[1]);
                Long nsSent = Long.valueOf(pongPayloadFields[2]);
                Long nsSourceReceived = Long.valueOf(pongPayloadFields[3]);
                
                LOGGER.debug("receivedSequenceNumber => " + receivedSequenceNumber);
                
                // this should always be true as an exception would be raised before
                if (receivedSequenceNumber.equals(sequenceNumber) && nsSent.equals(nsSend)) {
                    // we double check we got the correct reply
                    long rtt = (nsReceived - nsSend);
                
                    long timeOffsetSample = nsSourceReceived - nsSent - (rtt/2);

                    timeOffsetSum += timeOffsetSample;
                    LOGGER.debug("Offset evaluated: " + timeOffsetSample);
                }
		} catch (SocketTimeoutException e) {
                    timedOut++;
                    LOGGER.warn("Timeout for REPLY packet " + sequenceNumber);
                    
                } catch (SocketException so) {
                    LOGGER.info("Socket was closed: shutting down");
                    break;
		} catch (IOException e) {
                    LOGGER.warn("Error while sending PING packet: " + sequenceNumber);
                }
            
		sequenceNumber++;
	}

        Long avgTimeOffsetMs = timeOffsetSum/(packets - timedOut)/(1000*1000);
        LOGGER.info("AVG measured time offset => " + avgTimeOffsetMs);
        return avgTimeOffsetMs;
    }
    
    
    public Long getTimeOffset() {
        synchronized(this) {
            return this.timeOffset;
        }
    }
    
    
    private void setTimeOffset(Long timeOffset) {
        synchronized(this) {
            this.timeOffset = timeOffset;
        }
    }
    

    @Override
    public void run() {
        Long tOffset;
        while (isRunning) {
            try {
                tOffset = measureTimeOffset();
                setTimeOffset(tOffset);                
                Thread.sleep(interval);  
            } catch (InterruptedException ie) {
                LOGGER.info("Thread was stopped");
                isRunning = false; 
            }   
        }
    }
    
    
}
