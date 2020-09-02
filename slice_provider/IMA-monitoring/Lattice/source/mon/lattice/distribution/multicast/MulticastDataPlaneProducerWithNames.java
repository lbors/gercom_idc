// MulticastDataPlaneProducerWith.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package mon.lattice.distribution.multicast;

import mon.lattice.xdr.XDRDataOutputStream;
import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.MeasurementEncoderWithNames;
import mon.lattice.distribution.MeasurementEncoder;
import mon.lattice.core.plane.MeasurementMessage;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.DataSourceDelegate;
import mon.lattice.core.DataSourceDelegateInteracter;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.ID;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A MulticastDataPlaneProducerWithNames is a DataPlane implementation
 * that sends Measurements by multicast.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class MulticastDataPlaneProducerWithNames extends AbstractMulticastDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, TransmittingData {

    /**
     * Construct a MulticastDataPlaneProducerWithNames.
     */
    public MulticastDataPlaneProducerWithNames(MulticastAddress addr) {
        super(addr);
    }

    /**
     * Send a message onto the multicast address.
     * The message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public int transmit(DataPlaneMessage dpm) throws Exception { // IOException, TypeException {
	// convert DataPlaneMessage into a ByteArrayOutputStream
	// then transmit it

	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    DataOutput dataOutput = new XDRDataOutputStream(byteStream);

	    // write the DataSource id
            ID dataSourceID = dpm.getDataSource().getID();
	    dataOutput.writeLong(dataSourceID.getMostSignificantBits());
	    dataOutput.writeLong(dataSourceID.getLeastSignificantBits());

	    // write message type
	    dataOutput.writeInt(dpm.getType().getValue());

	    // write DataSource seqNo
	    int seqNo = dpm.getSeqNo();
	    dataOutput.writeInt(seqNo);

	    // write the message object
	    switch (dpm.getType()) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;

	    case MEASUREMENT:
		// extract Measurement from message object
		ProbeMeasurement measurement = ((MeasurementMessage)dpm).getMeasurement();
		// encode the measurement, ready for transmission
		MeasurementEncoder encoder = new MeasurementEncoderWithNames(measurement);
		encoder.encode(dataOutput);

		break;
	    }

	    //System.err.println("DP: " + dpm + " AS " + byteStream);

	    // now tell the multicaster to transmit this byteStream
	    mcastTransmitter.transmit(byteStream, seqNo);

	    return 1;
	} catch (TypeException te) {
	    te.printStackTrace(System.err);
	    return 0;
	}
    }


}
