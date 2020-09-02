// ForwardMeasurement.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2012

package mon.lattice.distribution;

import mon.lattice.core.Timestamp;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.Measurement;
import mon.lattice.core.ID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.Serializable;

/**
 * A class for measurements that will be Forwarded.
 * They do not come directly from Probes.
 */
public class ForwardMeasurement implements Measurement, ProbeMeasurement {
    // the probe id
    ID id;

    // the service ID for the probe
    ID serviceID;

    // the group ID for the probe
    ID groupID;

    // the timestamp
    Timestamp timestamp = null;

    // the delta since the previous measurement
    Timestamp mDelta = null;

    // the type
    String type;

    // the sequence no
    long sequenceNo;

    // the attribute values list
    List<ProbeValue> attributes;



    /**
     * Construct a ForwardMeasurement, given a ConsumerMeasurement.
     * Throws a TypeException if any of the objects passed in are
     * not valid types for a Measurement.
     */
    public ForwardMeasurement(Measurement m) {
	id = m.getProbeID();
	serviceID = m.getServiceID();
        groupID = m.getGroupID();
	timestamp = m.getTimestamp();
	mDelta = m.getDeltaTime();
	type = m.getType();
        sequenceNo = m.getSequenceNo();

	attributes = m.getValues();
    }

    /**
     * Get the timestamp
     */
    public Timestamp getTimestamp() {
	return timestamp;
    }

    /**
     * Get the delta since the last measurement.
     */
    public Timestamp getDeltaTime() {
	return mDelta;
    }

    /**
     * Get the probe this Measurement is from.
     * There isn't one for a ForwardMeasurement
     * @return null
     */
    public Probe getProbe() {
	return null;
    }

    /**
     * Get the ID of the probe this Measurement is from
     */
    public ID getProbeID() {
	return id;
    }

    /**
     * Get the service ID of the probe this Measurement is from
     */
    public ID getServiceID() {
	return serviceID;
    }

    /**
     * Set the service ID.
     */
    public void setServiceID(ID sid) {
	serviceID = sid;
    }


    /**
     * Get the group ID for this Measurement
     */
    public ID getGroupID() {
	return groupID;
    }

    /**
     * Set the groupID.
     */
    public void setGroupID(ID gid) {
	groupID = gid;
    }

    /**
     * Get the measurement type
     */
    public String getType() {
	return type;
    }

    /**
     * Set the measurement type
     */
    public void setType(String t) {
	type = t;
    }

    /**
     * Get the sequence number of this measurement.
     */
    public long getSequenceNo() {
	return sequenceNo;
    }

    /**
     * Set the sequence number of this measurement.
     */
    public void setSequenceNo(long n) {
	sequenceNo = n;
    }

    /**
     * Get the attribute / values
     */
    public List<ProbeValue> getValues() {
	return attributes;
    }

    /**
     * Add attribute / values to the measurement.
     */
    public Measurement addValues(List<ProbeValue> values) {
	attributes.addAll(values);
	return this;
    }


    /**
     * To String
     */
    public String toString() {
	return ("seq: " + getSequenceNo() + " probeid: " + getProbeID() + " serviceid: " + getServiceID() + " groupid: " + getGroupID() + " timestamp: " +  timestamp + " delta: " + getDeltaTime() + 
		" type: " + type + " attributes: " + attributes);
    }

}
