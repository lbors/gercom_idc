// KPIProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2010

package eu.reservoir.monitoring.appl.probes.vee;

import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.DefaultProbeAttribute;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.AbstractProbe;
import java.util.*;

/**
 * A KPI probe is a Probe that is embedded in a VEE.
 * It sends the following 4 attributes:
 * FQN = fully qualified name of the VEE
 * serviceName = the name of the service
 * class = kpi
 * the values read for this probe.
 */
public abstract class KPIProbe extends AbstractProbe {
    // The FQN
    String fqn;

    // next attribute number
    int next = 0;

    /**
     * Construct a KPIProbe given a FQN of a service element 
     */
    public KPIProbe(String fqn) {
        this.fqn = fqn;

        // define attributes
        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "serviceName", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(2, "class", ProbeAttributeType.STRING, "name"));
        next = 3;
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
     * Define a KPI in this measurement.
     * Returns the attribute position of this kpi
     */
    public int addKPI(String kpiName, ProbeAttributeType at, String units) {
        addProbeAttribute(new DefaultProbeAttribute(next, kpiName, at, units));
        int retVal = next;
        next++;

        return retVal;
    }

    

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() { 
        try {
            List<Object> retrieved = collectKPIValues();

            if (retrieved == null) {
                return null;
            } else {
                ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(next);

                // add default values to list
                list.add(new DefaultProbeValue(0, fqn));
                list.add(new DefaultProbeValue(1, getServiceName(fqn)));
                list.add(new DefaultProbeValue(2, "kpi"));

                // now add retrieved values to list
                for (int k=0; k < retrieved.size(); k++) {
                    list.add(new DefaultProbeValue(3+k, retrieved.get(k)));
                }

                // create a measurement based on the values
                return new ProducerMeasurement(this, list, "kpi");
            }
        } catch (Exception e) {
            // on error, return a null
            return null;
        }
    }

    /**
     * Retrieve the values for this KPI.
     * This will return a list of Objects which will be
     * used as the values for the KPI measurement.
     * The size of the list must be the same length as
     * the number of calls to addKPI().
     */
    public abstract List<Object>collectKPIValues();
}