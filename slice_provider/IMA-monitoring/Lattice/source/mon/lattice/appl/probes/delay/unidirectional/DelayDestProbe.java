/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.unidirectional;

import mon.lattice.core.datarate.EveryNSeconds;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DelayDestProbe extends AbstractProbe implements Probe {
    int mgmPackets = 5;
    int mgmTimeout = 1000;
    int mgmInterval = 120; // seconds
    
    int dataPackets = 5;
    int dataTimeout = 1000;
    int probeInterval = 30; // seconds, used also as the actual probe rate
    
    UDPDataReceiver dataReceiver;
    UDPMgmSender mgmSender;
    
    Long timeOffset;
    
    LinkedBlockingQueue<Long> queue;
    
    private Logger LOGGER = LoggerFactory.getLogger(DelayDestProbe.class);
    
    
    
    public DelayDestProbe(String probeName,
                        String mgmLocalAddr,
                        String mgmLocalPort,
                        String dataLocalAddr,
                        String dataLocalPort,
                        String mgmSourceAddr, 
                        String mgmSourcePort,
                        String mgmPackets,
                        String mgmTimeout,
                        String mgmInterval,
                        String dataPackets,
                        String dataTimeout,
                        String dataInterval) throws SocketException, UnknownHostException {
        
        this.mgmPackets = Integer.valueOf(mgmPackets);
        this.mgmTimeout = Integer.valueOf(mgmTimeout);
        this.mgmInterval = Integer.valueOf(mgmInterval);
        
        this.dataPackets = Integer.valueOf(dataPackets);
        this.dataTimeout = Integer.valueOf(dataTimeout);
        this.probeInterval = Integer.valueOf(dataInterval);
        
        
        
        queue = new LinkedBlockingQueue<>();
        
        mgmSender = new UDPMgmSender(InetAddress.getByName(mgmLocalAddr), 
                                        Integer.valueOf(mgmLocalPort), 
                                        InetAddress.getByName(mgmSourceAddr), 
                                        Integer.valueOf(mgmSourcePort),
                                        this.mgmPackets,
                                        this.mgmTimeout,
                                        this.mgmInterval);
        
        dataReceiver = new UDPDataReceiver(Integer.valueOf(dataLocalPort), dataLocalAddr, queue, this.dataPackets, this.dataTimeout); 
        
        setName(probeName);
        setDataRate(new EveryNSeconds(this.probeInterval));

        addProbeAttribute(new DefaultProbeAttribute(0, "link", ProbeAttributeType.STRING, "id"));
        addProbeAttribute(new DefaultProbeAttribute(1, "delay", ProbeAttributeType.LONG, "milliseconds"));
    }  
    
    
    
    
    @Override
    public void beginThreadBody() {
        mgmSender.start();
        dataReceiver.start();
    }
    
    
    @Override
    public void endThreadBody() {
        mgmSender.stop();
        dataReceiver.stop();
    }
    
    
    @Override
    public ProbeMeasurement collect() {
        
        try {
            Long dataDelay = queue.take();
            LOGGER.info("Measured delay just taken off the queue (size=" + queue.size() + "): " + dataDelay);
            
            timeOffset = mgmSender.getTimeOffset();
            LOGGER.info("current time offset: " + timeOffset);
            
            ArrayList<ProbeValue> list = new ArrayList<>(2);
            list.add(new DefaultProbeValue(0, "vnf1vnf2")); // TODO check this and see if we need to use a parameter
            list.add(new DefaultProbeValue(1, dataDelay + timeOffset));
            
            ProbeMeasurement m = new ProducerMeasurement(this, list, "Link");
            LOGGER.debug("Returning measurement: " + m.toString());
            return m;   
        } catch (InterruptedException ie) {
            LOGGER.error("Received interrupt: shutting down probe thread");
            super.threadRunning = false;
            
        } catch (TypeException te) {
            LOGGER.error("Error while adding probe attribute: " + te.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error " + e.getMessage());
        }
        return null;
    }
    
}

