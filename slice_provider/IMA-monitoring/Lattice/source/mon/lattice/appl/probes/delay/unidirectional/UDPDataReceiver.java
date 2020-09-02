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
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class UDPDataReceiver implements Runnable {
    int packets;
    int timeout;
    
    DatagramSocket socket;
    Boolean isRunning = false;
    
    Thread t;
    
    LinkedBlockingQueue<Long> queue;
    
    private Logger LOGGER = LoggerFactory.getLogger(UDPDataReceiver.class);
    
    public UDPDataReceiver(int port, String address, LinkedBlockingQueue<Long> q, int packets, int timeout) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(port, InetAddress.getByName(address));
        queue = q;
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
            socket.close();
        }
    }
    
    
    @Override
    public void run() {
        byte[] buf = new byte[1024];
        try {
            while (isRunning) {
                
                int sequenceNumber = 0;
                long dataDelay;
                long dataDelaySum = 0;
                int receivedPackets = 0;
                long avgDelayMs;
                
                do {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        long nsReceived = System.nanoTime();
                        
                        receivedPackets++;

                        String receivedData = new String(packet.getData());
                        String [] receivedDataFields = receivedData.split(" ");

                        sequenceNumber = Integer.valueOf(receivedDataFields[1]);
                        long nsSent = Long.valueOf(receivedDataFields[2]);
                        dataDelay = nsReceived - nsSent;
                        dataDelaySum += dataDelay;
                        
                        if (socket.getSoTimeout() == 0) {
                            socket.setSoTimeout(timeout);
                        }
                    } catch (SocketTimeoutException e) {
                        LOGGER.warn("Timeout receiving packet => " + sequenceNumber);
                        break;
                    }
                } while (receivedPackets < packets);
                
                avgDelayMs = dataDelaySum/receivedPackets/(1000*1000);
                queue.put(avgDelayMs);
                LOGGER.info("Measured delay just added to the queue (size=" + queue.size() + "): " + avgDelayMs);
                
                socket.setSoTimeout(0);
            }
        }
          catch (SocketException so) {
            LOGGER.info("Socket was termineted");
        } catch (IOException ioe) {
            LOGGER.error("Error: " + ioe.getMessage());
        } catch (InterruptedException ie) {
            LOGGER.info("Thread is being shutted down");
        }
    }   
    
}
