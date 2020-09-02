// HypervisorProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.probes.hypervisor.libvirt;

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
import java.util.ArrayList;
import org.libvirt.*;

/**
 * A probe for listing the status of a vee
 */
public class HypervisorProbe extends AbstractProbe implements Probe  {
    // The hypervisor
    HypervisorCache hypervisor;

    // the vee ID
    int id;

    // the vee name == FQN
    String vee;

    // The service name
    String serviceName;

    // The class of measurement for SM
    final static String mClass = "infra_vee";

    // Previoussnapshot of DomainInfo
    DomainInfo prevInfo;

    /*
     * Construct a probe
     */
    public HypervisorProbe(int id, String vee, String currentHost, HypervisorCache hypervisor) {
	this.id = id;
	this.vee = vee;
        this.serviceName = getServiceName(vee);

        setName(currentHost + "." + vee);
        setDataRate(new Rational(600, 1));  // every 15 seconds == 240 samples/hr // was 600 samples

	this.hypervisor = hypervisor;

	// data to be sent is:
	// string FQN name
	// float cpuPercent 
	// long cpuMillisDiff milliseconds of cpu used since last time
	// long cpuSecsTotal seconds of cpu used in total
	// long maxMem the maximum memory in KBytes allowed
	// long memory the memory in KBytes used by the domain
	// int nrVirtCpu the number of virtual CPUs for the domain
	// char state the running state, one of virDomainFlag
	// long netRX 
	// long netTX
	// string serviceName name
	// string class name

        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu.percent", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "cpu.millis", ProbeAttributeType.LONG, "milliseconds"));
        addProbeAttribute(new DefaultProbeAttribute(3, "cpu.total", ProbeAttributeType.LONG, "seconds"));
        addProbeAttribute(new DefaultProbeAttribute(4, "mem.allocated", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "mem.used", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(6, "cpu.cores", ProbeAttributeType.INTEGER, "n"));
        addProbeAttribute(new DefaultProbeAttribute(7, "vee.state", ProbeAttributeType.CHAR, "DomainState"));
        addProbeAttribute(new DefaultProbeAttribute(8, "net.rx", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(9, "net.tx", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(10, "serviceName", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(11, "class", ProbeAttributeType.STRING, "name"));

    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
	System.err.println("HypervisorProbe: beginThread " + vee + " with ID " + id);

	//System.err.println("HypervisorProbe: current thread = " + Thread.currentThread());

	// wait for the cache to be ready
	hypervisor.waitForCacheReady();

	// get current domain info
	CachedDomain domain = hypervisor.domainLookupByID(id);
	prevInfo = domain.getInfo();
    }

    /**
     * End of thread
     */
    public void endThreadBody() {
	System.err.println("HypervisorProbe: endThread " + vee + " with ID " + id);
    }

    /**
     * Determine the serviceName from the FQN.
     */
    protected String getServiceName(String fqn) {
        // currently converts something like
        // es.tid.customers.sun.services.sge1.vees.veemaster.replicas.1
        // into es.tid.customers.sun.services.sge1


        // skip back from end for 2 dots
        int dot1 = fqn.lastIndexOf('.');
        int dot2 = fqn.lastIndexOf('.', dot1-1);
        int dot3 = fqn.lastIndexOf('.', dot2-1);
        int dot4 = fqn.lastIndexOf('.', dot3-1);

        // return from start to dot4
        return fqn.substring(0, dot4);
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {	    

	    System.err.println("HypervisorProbe: current thread for " + vee + "/" + id + " = " + Thread.currentThread());

	    // get current domain info
	    CachedDomain domain = hypervisor.domainLookupByID(id);

	    if (domain == null) {
		// the domain has probably shutdown
		return null;
	    }

	    // System.err.println("collect: " + vee + ": " + domain);

	    DomainInfo di = domain.getInfo();

	    // determine the scale factor for normalizing cpu per cent
	    // it depends on the no of milliseconds between each reading
	    float scaleFactor = (float)rationalToMillis(getDataRate());

	    // work out difference for cpu
	    long cpuNanosD = di.cpuTime - prevInfo.cpuTime;
	    long cpuMillisD = cpuNanosD / (1000 * 1000);
	    float cpuPerCent = (cpuMillisD * 100) / scaleFactor;
	    long cpuSecsTotal = di.cpuTime  / (1000 * 1000 * 1000);

	    // network stats 
	    //DomainInterfaceStats ifStats = domain.interfaceStats("vif" + domain.getID() + ".0");

	    // Convert the state into a byte
	    char state = HypervisorState.processState(di.state);

	    //System.out.printf("%5.2f%8d%10d%20s", cpuPerCent, cpuMillisD, cpuSecsTotal, di.memory , di.state);
	    //System.out.printf("%6d%6d\n", ifStats.rx_bytes, ifStats.tx_bytes);

	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(12);

	    list.add(new DefaultProbeValue(0, vee));
	    list.add(new DefaultProbeValue(1, cpuPerCent));
	    list.add(new DefaultProbeValue(2, cpuMillisD));
	    list.add(new DefaultProbeValue(3, cpuSecsTotal));
	    list.add(new DefaultProbeValue(4, di.maxMem));
	    list.add(new DefaultProbeValue(5, di.memory));
	    list.add(new DefaultProbeValue(6, di.nrVirtCpu));
	    list.add(new DefaultProbeValue(7, state));
	    //list.add(new DefaultProbeValue(8, ifStats.rx_bytes));
	    //list.add(new DefaultProbeValue(9, ifStats.tx_bytes));
	    list.add(new DefaultProbeValue(8, 0L));
	    list.add(new DefaultProbeValue(9, 0L));
	    list.add(new DefaultProbeValue(10, serviceName));
	    list.add(new DefaultProbeValue(11, mClass));

	    // create the Measurement
	    ProbeMeasurement m = new ProducerMeasurement(this, list, mClass);

	    System.out.println(m.getValues());

	    // now save current info
	    prevInfo = di;

	    // return the measurement
	    return m;

	} catch (TypeException te) {
	    System.err.println("TypeException " + te + " in HypervisorProbe");
	    return null;
	}
    }

}
