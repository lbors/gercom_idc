// UDPDataPlaneConsumerWithNames.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sep 2012

package mon.lattice.distribution.udp;

import mon.lattice.distribution.ExposedByteArrayInputStream;
import mon.lattice.distribution.TransmittingData;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Receiving;
import mon.lattice.core.plane.DataPlaneMessage;
import mon.lattice.core.plane.DataPlane;
import mon.lattice.core.Measurement;
import mon.lattice.core.MeasurementReporting;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.ConsumerMeasurement;
import mon.lattice.core.ID;
import mon.lattice.core.TypeException;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.net.InetSocketAddress;


/**
 * This consumer also forwards every recevied Measurement.
 */
public class UDPDataPlaneForwardingConsumerWithNames extends UDPDataPlaneConsumerWithNames implements DataPlane, MeasurementReporting, Receiving, TransmittingData {

    // The address we are sending to
    InetSocketAddress transmitAddress;

    // The UDPTransmitter
    UDPTransmitter udpTransmitter;

    // A count
    int count = 0;

    /**
     * Construct a UDPDataPlaneForwardingConsumerWithNames.
     * @param addr the consumer address
     * @param tAddr the address to forward to, or null if no forwarding required
     */
    public UDPDataPlaneForwardingConsumerWithNames(InetSocketAddress addr, InetSocketAddress tAddr) {
        super(addr);
	// sending address
        transmitAddress = tAddr;
    }

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
        boolean result = false;

        // connect on consumer
	try {
	    // only connect if we're not already connected
	    if (udpReceiver == null) {
		UDPReceiver rr = new UDPReceiver(this, address);

		rr.listen();
		
		udpReceiver = rr;

		result = true;
	    } else {
		result = true;
	    }

	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    //ioe.printStackTrace();

	    return false;
	}

        // now try to connect to the transmitAddress
        // if forward address is specified
        if (transmitAddress != null) {
            try {
                // only connect if we're not already connected
                if (udpTransmitter == null) {
                    // Now connect to the IP address
                    UDPTransmitter tt = new UDPTransmitter(this, transmitAddress);

                    tt.connect();
		
                    udpTransmitter = tt;

                    result =  true;
                } else {
                    result = true;
                }

            } catch (IOException ioe) {
                // Current implementation will be to do a stack trace
                //ioe.printStackTrace();

                return false;
            }

        }

        return result;
    }


    /**
     * Disconnect from a delivery mechansim.
     */
    public boolean disconnect() {
        boolean result = false;

	try {
	    udpReceiver.end();
	    udpReceiver = null;
	    result =  true;
	} catch (IOException ieo) {
	    udpReceiver = null;
	    return false;
	}

        if (udpTransmitter != null) {
            try {
                udpTransmitter.end();
                udpTransmitter = null;
                result = true;
            } catch (IOException ieo) {
                udpTransmitter = null;
                return false;
            }
        }

        return result;
    }


    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular address.
     * The expected message is XDR encoded and it's structure is:
     * +---------------------------------------------------------------------+
     * | data source id (2 X long) | msg type (int) | seq no (int) | payload |
     * +---------------------------------------------------------------------+
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {
        bis.mark(0);

        // do usual received()
        super.received(bis, metaData);

        // forward IFF the transmitter is up and running
        if (udpTransmitter != null) {

            bis.reset();

            // now Transmit
            int volume = bis.available();

            // first convert a ByteArrayInputStream into a ByteArrayOutputStream
            ByteArrayOutputStream boas = new ByteArrayOutputStream(volume);


            // can we copy straight out of the ByteArrayInputStream
            if (bis instanceof ExposedByteArrayInputStream) {

                boas.write(((ExposedByteArrayInputStream)bis).toByteArray(), 0, volume);
            } else {
                // create temp buffer
                byte [] tmp = new byte[volume];
                // Reads up to len bytes of data into an array of bytes from this input stream.
                bis.read(tmp, 0, volume);

                // now copy to ByteArrayOutputStream
                boas.write(tmp, 0, volume);
            }
            
            udpTransmitter.transmit(boas, count); 

            count++;
        }
    }


    /**
     * Never called in the class.
     * Needed in order to implement TransmittingData
     */
    public int transmit(DataPlaneMessage dsp) throws Exception {
        return 0;
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id) {
	return true;
    }


}

        
