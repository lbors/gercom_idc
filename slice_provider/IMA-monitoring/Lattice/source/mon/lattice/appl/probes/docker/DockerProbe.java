/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.docker;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.datarate.EveryNSeconds;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;

/**
 *
 * @author uceeftu
 */
public class DockerProbe extends AbstractProbe implements Probe {
    
    // the container ID
    String containerId;

    // the container name
    String resourceId;
    
    long previousContainerCPUTime;
    long previousSystemCPUTime;
    
    DockerDataCollector ddc;
    
    private Logger LOGGER = LoggerFactory.getLogger(DockerProbe.class);
    
    
    public DockerProbe(String dockerHost, String dockerPort, String probeName, String cId, String resourceId)  {       
        
        LOGGER.info("dockerHost => " + dockerHost);
        LOGGER.info("dockerPort => " + dockerPort);
        LOGGER.info("resource ID => " + resourceId);
        LOGGER.info("container ID => " + cId);
        LOGGER.info("probeName => " + probeName);
        
        
        setName(probeName);
        setDataRate(new EveryNSeconds(10));
        
        this.containerId=cId;
        this.resourceId = resourceId;
        
        try {
            ddc = new DockerDataCollector(dockerHost, Integer.valueOf(dockerPort), this.containerId);
        } catch (UnknownHostException e) {
            LOGGER.error("Error while resolving docker host: " + e.getMessage());
        }
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name")); // we need to double check what info is needed here
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu_percent", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "mem_used", ProbeAttributeType.LONG, "bytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "mem_percent", ProbeAttributeType.FLOAT, "percent"));
        
        //addProbeAttribute(new DefaultProbeAttribute(4, "tx_bytes", ProbeAttributeType.LONG, "bytes"));
        //addProbeAttribute(new DefaultProbeAttribute(5, "rx_bytes", ProbeAttributeType.LONG, "bytes"));
        
    }
    

    @Override
    public void beginThreadBody() {
        try {
            ddc.collectValues();
        } catch (IOException ioe) {
            LOGGER.error("Error while contacting DOCKER API: " + ioe.getMessage());
        } catch (JSONException je) {
            LOGGER.error("Error while parsing DOCKER API response: " + je.getMessage());
        }
        
        previousContainerCPUTime = ddc.getContainerCpuTime();
        previousSystemCPUTime = ddc.getSystemCpuTime();
    }
    
    @Override
    public ProbeMeasurement collect() {
        try {
            //float scaleFactor = (float)rationalToMillis(getDataRate());
            
            ddc.collectValues();
            
            //System.out.println("Current CPU time: " + ddc.getContainerCpuTime());
            //System.out.println("Old CPU time: " + previousContainerCPUTime);
            
            //System.out.println("Current system time: " + ddc.getSystemCpuTime());
            //System.out.println("Old system time: " + previousSystemCPUTime);
            
            
            long containerCpuTimeDelta = ddc.getContainerCpuTime() - previousContainerCPUTime;
            long systemCpuTimeDelta = ddc.getSystemCpuTime() - previousSystemCPUTime;
            
            //System.out.println("CPUTime delta: " + containerCpuTimeDelta);
            //System.out.println("SystemTime delta: " + systemCpuTimeDelta);
            
            float cpuPercent = 0;
            if (systemCpuTimeDelta > 0)
                //this is according to 'docker stats' source code: TODO test formula on multiple cores
                cpuPercent = ((float)containerCpuTimeDelta / systemCpuTimeDelta) * ddc.getCoresNumber() * 100; 
            
            float memPercent = 0;
            float memMaxBytes = ddc.getMaxMemBytes();
            if (memMaxBytes > 0)
                memPercent = ((float)ddc.getUsedMemBytes() / memMaxBytes) * 100;
            
            ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(4);

            list.add(new DefaultProbeValue(0, this.resourceId));
            list.add(new DefaultProbeValue(1, cpuPercent));
            list.add(new DefaultProbeValue(2, ddc.getUsedMemBytes()));
            list.add(new DefaultProbeValue(3, memPercent));
            
            //list.add(new DefaultProbeValue(4, ddc.getTxBytes()));
            //list.add(new DefaultProbeValue(5, ddc.getRxBytes()));

            ProbeMeasurement m = new ProducerMeasurement(this, list, "Container");
            
            previousContainerCPUTime = ddc.getContainerCpuTime();
            previousSystemCPUTime = ddc.getSystemCpuTime();
            
            return m;
        }
        catch (IOException ioe) {
            LOGGER.error("Error while contacting DOCKER API: " + ioe.getMessage());
        } catch (JSONException je) {
            LOGGER.error("Error while parsing DOCKER API response: " + je.getMessage());
        } catch (TypeException te) {
            LOGGER.error("Error while adding probe attribute: " + te.getMessage());
        }
        
    return null;
    }   
}
