/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.control.ControlPlaneConsumerException;
import mon.lattice.core.plane.ControlPlaneMessage;
import mon.lattice.core.plane.ControlOperation;
import mon.lattice.core.plane.MessageType;
import mon.lattice.distribution.MetaData;
import mon.lattice.xdr.XDRDataInputStream;
import mon.lattice.xdr.XDRDataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;


public class ZMQControlPlaneXDRProducer extends AbstractZMQControlPlaneProducer {
    
    /**
     * Creates a Producer without announce/deannounce management capabilities
     * @param maxPoolSize is the size of the UDP Transmitters pool
     */
    public ZMQControlPlaneXDRProducer(int maxPoolSize, int port) {
        super(maxPoolSize, port);
    }
    

    @Override
    public Object synchronousTransmit(ControlPlaneMessage cpMessage, MetaData metadata) throws IOException, ControlPlaneConsumerException {
        Object result=null;
        
        // convert the object to a byte []
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new XDRDataOutputStream(byteStream);

        try {
            // write type
            dataOutput.writeInt(cpMessage.getType().getValue());            

            // write object
            dataOutput.writeUTF(cpMessage.getControlOperation().getValue());            

            // writing message seqNo
            int seqNo = cpMessage.getSequenceNumber();
            dataOutput.writeInt(seqNo);

            // convert args to byte          
            dataOutput.write(cpMessage.getMethodArgsAsByte());

            LOGGER.debug("--------- Sending Control Message with seqNo: " + seqNo + " ----------");

            // getting a Transmitter from the Pool
            ZMQRequester connection = controlTransmittersPool.getConnection();
            result = connection.transmitAndWaitReply(byteStream, (ZMQControlMetaData)metadata, seqNo);

            // putting the Transmitter back to the Pool
            controlTransmittersPool.releaseConnection(connection);
        } catch (InterruptedException ex) {
            LOGGER.info("interrupted " + ex.getMessage());
        }
        
        if (result instanceof ControlPlaneConsumerException) {
            throw ((ControlPlaneConsumerException) result);
        }
        
    return result;    
    }

    // called when a control reply message is received
    @Override
    public Object receivedReply(ByteArrayInputStream bis, MetaData metaData, int seqNo) throws IOException {
        Object result=null;
        
        DataInput dataIn = new XDRDataInputStream(bis);
        
        // check message type
        int type = dataIn.readInt();            
        MessageType mType = MessageType.lookup(type);

        if (mType == null) {
            throw new IOException("Message type is null");
        }

        else if (mType == MessageType.CONTROL_REPLY) {
                LOGGER.debug("-------- Control Reply Message Received ---------");
                LOGGER.debug("From: " + metaData);

                int replyMessageSeqNo = dataIn.readInt();

                String ctrlOperation = dataIn.readUTF();
                ControlOperation ctrlOperationName = ControlOperation.lookup(ctrlOperation);

                byte [] args = new byte[8192];
                dataIn.readFully(args);
                
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(args));
                    result = (Object) ois.readObject();
                    ois.close();
                } catch (ClassNotFoundException e) {
                    throw new IOException(e.getMessage());
                  }

                if (replyMessageSeqNo == seqNo)
                    if (result instanceof Exception )
                        LOGGER.error("Exception received as reply for request with seqNo: " + replyMessageSeqNo + " - Operation: " + ctrlOperationName + " - Result: " + result.toString());
                    else
                        LOGGER.info("Received reply for request with seqNo: " + replyMessageSeqNo + " - Operation: " + ctrlOperationName + " - Result: " + result.toString());
                else
                    // we should not likely arrive here
                    throw new IOException("Message Sequence number mismatch! " + replyMessageSeqNo + " not equal to " + seqNo);
        }
        
        return result;
    }

    
    @Override
    public boolean transmitted(int id) {
        LOGGER.info("just transmitted Control Message with seqNo: " + id);
        return true;
    }
}
