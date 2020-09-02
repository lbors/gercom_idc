// NetInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package mon.lattice.appl.probes.host.linux;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.Probe;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.Rational;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.ArrayList;

/**
 * A probe to get net interface info on a Linux system.
 * It uses /proc/net/dev to read the underyling data.
 * Measurements return the amount of data since the last measurement.
 */
public class NetInfo extends AbstractProbe implements Probe  {
    // A NetDev object that reads info about a network interface
    NetDev netDev;

    // Interface 
    String ifName;

    /*
     * Construct a NetInfo probe
     */
    public NetInfo(String name, String ifName) {
	setName(name);
        setDataRate(new Rational(360, 1));

        // save interface
	this.ifName = ifName;
        // allocate NetDev
        netDev = new NetDev(ifName);

        // define the attributes
        addProbeAttribute(new DefaultProbeAttribute(0, "in_bytes", ProbeAttributeType.INTEGER, "bytes"));
	addProbeAttribute(new DefaultProbeAttribute(1, "in_packets", ProbeAttributeType.INTEGER, "packets"));
        addProbeAttribute(new DefaultProbeAttribute(2, "in_errors", ProbeAttributeType.INTEGER, "packets"));
        addProbeAttribute(new DefaultProbeAttribute(3, "in_dropped", ProbeAttributeType.INTEGER, "packets"));
        addProbeAttribute(new DefaultProbeAttribute(4, "out_bytes", ProbeAttributeType.INTEGER, "bytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "out_packets", ProbeAttributeType.INTEGER, "packets"));
        addProbeAttribute(new DefaultProbeAttribute(6, "out_errors", ProbeAttributeType.INTEGER, "packets"));
        addProbeAttribute(new DefaultProbeAttribute(7, "out_dropped", ProbeAttributeType.INTEGER, "packets"));
    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
	// read data, but calculate nothing
	netDev.read(false);

    }

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list the size 6
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(8);

	// read the data
	if (netDev.read(true)) {
	    try {
		// now collect up the results	
		int in_bytes = netDev.getDeltaValue("in_bytes");
		int in_packets = netDev.getDeltaValue("in_packets");
		int in_errors = netDev.getDeltaValue("in_errors");
		int in_dropped = netDev.getDeltaValue("in_dropped");
		int out_bytes = netDev.getDeltaValue("out_bytes");
		int out_packets = netDev.getDeltaValue("out_packets");
		int out_errors = netDev.getDeltaValue("out_errors");
		int out_dropped = netDev.getDeltaValue("out_dropped");


		// now collect up the results	
		list.add(new DefaultProbeValue(0, in_bytes));
		list.add(new DefaultProbeValue(1, in_packets));
		list.add(new DefaultProbeValue(2, in_errors));
		list.add(new DefaultProbeValue(3, in_dropped));
		list.add(new DefaultProbeValue(4, out_bytes));
		list.add(new DefaultProbeValue(5, out_packets));
		list.add(new DefaultProbeValue(6, out_errors));
		list.add(new DefaultProbeValue(7, out_dropped));
	    
		ProducerMeasurement m = new ProducerMeasurement(this, list, "NetInfo");	
		return m;
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/net/dev");
	    return null;
	}
    }

}
