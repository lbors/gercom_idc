/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.rtt.balancer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class Dispatcher implements Runnable {
    public boolean isRunning = false;
    int port = 9875;
    
    DatagramSocket serverSocket;
    
    Map<String, LinkedBlockingQueue<String>> queues;
    
    Thread t;

    private static final Dispatcher DISPATCHER = new Dispatcher();
    private final Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

 
    private Dispatcher() {
        try {
            serverSocket = new DatagramSocket(port);
            queues = new ConcurrentHashMap<>();
        } catch (SocketException e) {
            LOGGER.error("Error while starting the UDP receiver: " + e.getMessage());
        }
    }
    
    
    public static Dispatcher getInstance() {
        return DISPATCHER;
    }
    
    
    public LinkedBlockingQueue<String> addNewQueue(String containerId) {
        if (!queues.containsKey(containerId)) {
            LOGGER.info("added new queue for: " + containerId);
            return queues.put(containerId, new LinkedBlockingQueue<>());   
        } 
        else
            return queues.get(containerId);
    }
    
    
    public void deleteQueue(String containerId) {
        queues.remove(containerId); 
        LOGGER.info("removed queue for: " + containerId);
    }
    
    
    public LinkedBlockingQueue<String> getContainerQueue(String containerId) {
        return queues.get(containerId);
    }
   
    public void start() {
        if (!isRunning) {
            isRunning = true;
            t = new Thread(this);
            t.start();
            LOGGER.info("Receiving/Dispatcher thread started");
            
        }
    }
    
    public void stop() {
        if (queues.isEmpty()) {
            isRunning = false;
            serverSocket.close();
        }
    }
    
    
    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[1000];

            while (isRunning) {
                   DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                   serverSocket.receive(receivePacket);
                   String report = new String(receivePacket.getData());
                   
                   //example report 
                   //2086 u3764aba86056a5155e81629373c5a59a3e8ba0eedf28f68099cc03133b3591e RTT 0.00270016 robot 1520959110

                   for (String containerId : queues.keySet()) {
                        if (report.contains(containerId)) {
                            queues.get(containerId).put(report);
                            LOGGER.info("Added: " + report + " to the queue " + containerId);
                        }
                   }
            }  
        } catch (SocketException so) {
            LOGGER.info("UDP Socket has been closed");
        } catch (IOException e) {
            LOGGER.error("Error: " + e.getMessage()); 
        } catch (InterruptedException ie) {
            LOGGER.error("Error adding to the queue: " + ie.getMessage()); 
        
        }
    }
}
