/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.openstack;

//import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.TypeException;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.Rational;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author uceeftu
 */
public class OpenstackProbe extends AbstractProbe implements Probe{
    
    // the instance ID
    String instanceUUID;

    // the instance name
    String instanceName;
    
    OpenstackDataCollector osdc;
    
    
    public OpenstackProbe(String ceilometerHost, 
                          String ceilometerPort, 
                          String keystoneHost,
                          String keystonePort, 
                          String username,
                          String password,
                          String probeName, 
                          String instanceId, 
                          String instanceName) throws UnknownHostException {
        
        setName(probeName);
        setDataRate(new Rational(360, 1));
        //setDataRate(new EveryNSeconds(3));
        
        this.instanceUUID=instanceId;
        this.instanceName = instanceName;
        
        osdc = new OpenstackDataCollector(ceilometerHost, Integer.valueOf(ceilometerPort), keystoneHost, Integer.valueOf(keystonePort), username, password, this.instanceUUID);
        
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name")); // we need to double check what info is needed here
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu.percent", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "mem.used", ProbeAttributeType.FLOAT, "megabytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "mem.percent", ProbeAttributeType.FLOAT, "percent"));  
    }
    

    @Override
    public void beginThreadBody() {
	System.err.println("OpenstackProbe: beginThread " + this.instanceName + " with ID " + this.instanceUUID);
    }
    
    @Override
    public ProbeMeasurement collect() {
        try {
            osdc.collectValues();
            
            ArrayList<ProbeValue> list = new ArrayList<>(4);

            list.add(new DefaultProbeValue(0, this.instanceName));
            list.add(new DefaultProbeValue(1, osdc.cpuPercent));
            list.add(new DefaultProbeValue(2, osdc.memUsed));
            list.add(new DefaultProbeValue(3, (osdc.memUsed/osdc.memTotal)*100));

            ProbeMeasurement m = new ProducerMeasurement(this, list, "VirtualMachine");
            
            System.out.println(m.getValues());
            
            return m;
        }
        catch (TypeException e)
            {
                System.out.println("Error in OpenstackProbe" + e.getMessage());
            }
    return null;
    }   
}
