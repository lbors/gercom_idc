/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



public class ControlPlaneMessage {
    private final ControlOperation methodName;
    private final List<Object> methodArgs;
    private final int messageSeqNo;
    private final MessageType type;
    private final static AtomicInteger sequenceNumberCounter = new AtomicInteger(0);
    
    public ControlPlaneMessage(ControlOperation m, List<Object> args) {
        type = MessageType.CONTROL;
        methodName = m;
        methodArgs = args;
        messageSeqNo = sequenceNumberCounter.getAndAdd(1);
    } 

    public int getSequenceNumber() {
        return messageSeqNo;
    }
    
    public ControlOperation getControlOperation() {
        return methodName;
    }
        
    public List<Object> getMethodArgs() {
            return methodArgs;
        }
    
    
    public byte[] getMethodArgsAsByte() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(methodArgs);
        byte[] bytes = bos.toByteArray();
        return bytes;
        }

    public MessageType getType() {
        return type;
    }
    
    }