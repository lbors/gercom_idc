/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control;

import mon.lattice.core.plane.ControlPlaneMessage;
import mon.lattice.distribution.MetaData;
import mon.lattice.distribution.Transmitting;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * An interface for distribution components that need
 * to do transmitting of ControlPlaneMessage objects.
 * A Class implementing this interface is basically a transmitter of control messages 
   and receiver of control message replies 
 */
public interface SynchronousTransmitting extends Transmitting {
    public Object synchronousTransmit(ControlPlaneMessage dpm, MetaData metaData) throws IOException, ControlPlaneConsumerException;
    
    public Object receivedReply(ByteArrayInputStream bis, MetaData metaData, int seqNo) throws IOException;
}
