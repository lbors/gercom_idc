/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 *
 * @author uceeftu
 */
public class ControlPlaneReplyMessage {
    private final ControlOperation methodName;
    private final Object Payload;
    private final int sourceSequenceNo;
    private final MessageType type;

    public int getReplyToMessageID() {
        return sourceSequenceNo;
    }
    
    public ControlPlaneReplyMessage(Object Payload, ControlOperation m, int sourceSeqNo) {
        type = MessageType.CONTROL_REPLY;
        methodName = m;
        this.Payload = Payload; 
        this.sourceSequenceNo =sourceSeqNo;
    }
    
     public byte[] getPayloadAsByte() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(Payload);
        byte[] bytes = bos.toByteArray();
        return bytes;
        }
    
     public ControlOperation getControlOperation() {
        return methodName;
    }

    public MessageType getType() {
        return type;
    }
}
