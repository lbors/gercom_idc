/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.unidirectional;

import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeMeasurement;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DelaySourceProbe extends AbstractProbe implements Probe {
    int mgmPackets;
    int mgmTimeout;
    
    int dataPackets;
    int dataTimeout;
    int dataInterval;
    
    DatagramSocket dataSocket;
    UDPMgmReceiver mgmReceiver;
    
    InetAddress dataDestinationAddr;
    int dataDestinationPort;
    
    private Logger LOGGER = LoggerFactory.getLogger(DelaySourceProbe.class);
    
    
    
    public DelaySourceProbe(String probeName, 
                          String mgmLocalAddr,
                          String mgmLocalPort,
                          String dataLocalAddr,
                          String dataLocalPort,
                          String dataDestinationAddr, 
                          String dataDestinationPort,
                          String mgmPackets,
                          String mgmTimeout,
                          String dataPackets,
                          String dataTimeout,
                          String dataInterval) throws SocketException, UnknownHostException {
        
        this.mgmPackets = Integer.valueOf(mgmPackets);
        this.mgmTimeout = Integer.valueOf(mgmTimeout);
        
        this.dataPackets = Integer.valueOf(dataPackets);
        this.dataTimeout = Integer.valueOf(dataTimeout);
        this.dataInterval = Integer.valueOf(dataInterval);
        
        dataSocket = new DatagramSocket(Integer.valueOf(dataLocalPort), InetAddress.getByName(dataLocalAddr));
        mgmReceiver = new UDPMgmReceiver(InetAddress.getByName(mgmLocalAddr), Integer.valueOf(mgmLocalPort), this.mgmPackets, this.mgmTimeout);
        
        this.dataDestinationAddr = InetAddress.getByName(dataDestinationAddr);
        this.dataDestinationPort = Integer.valueOf(dataDestinationPort);
        
        setName(probeName);
        setDataRate(new EveryNSeconds(this.dataInterval));
    }
    
    
    @Override
    public void beginThreadBody() {
        mgmReceiver.start();
    }
    
    
    @Override
    public void endThreadBody() {
        mgmReceiver.stop();
    }
    

    
    @Override
    public ProbeMeasurement collect() {
        dataSend();
        //does not provide any measurements, it only sends measurement packets periodically
        return null;
    }
    
    
    
    private void dataSend() {
        DatagramPacket pingPacket;
        String pingPayload;
        int sequenceNumber = 0;
        LOGGER.info("Sending measurements packets");
        try {
            while (sequenceNumber < dataPackets) {
                long nsSend = System.nanoTime();
                pingPayload = "PING " + sequenceNumber + " " + nsSend + " \n";
                byte[] sendBuf = pingPayload.getBytes();
                pingPacket = new DatagramPacket(sendBuf, sendBuf.length, dataDestinationAddr, dataDestinationPort);
                dataSocket.send(pingPacket);
                LOGGER.debug("Sending Packet =>" + sequenceNumber);
                sequenceNumber++;
            }
            LOGGER.info("Done");
        } catch (IOException e) {
            LOGGER.error("Error while sending messages: " + e.getMessage());
        }
    }
    
}

