/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.udp;

import mon.lattice.distribution.Transmitting;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author uceeftu
 */
public final class UDPTransmitterPool {
    volatile Integer usedObjects;
    int maxSize;
    LinkedBlockingQueue<UDPSynchronousTransmitter> socketQueue;
    Transmitting transmitting;

    public UDPTransmitterPool(Transmitting transmitting, int size) throws IOException {
        this.transmitting = transmitting;
        this.maxSize = size;
        this.socketQueue = new LinkedBlockingQueue(size);
        this.usedObjects = 0;
    }
    
    public void disconnect() throws IOException {
        for (UDPSynchronousTransmitter t: socketQueue)
            t.end();
    }
    
    public UDPSynchronousTransmitter getConnection() throws IOException, InterruptedException {
        synchronized (usedObjects) {
            if (socketQueue.isEmpty() && usedObjects < maxSize) {
                usedObjects++;
                return new UDPSynchronousTransmitter(transmitting);
            }
        }
        UDPSynchronousTransmitter t = socketQueue.take();
        synchronized (usedObjects) {
            usedObjects++;
        }
        return t;
    }
    
    public void releaseConnection(UDPSynchronousTransmitter conn) throws InterruptedException {
        socketQueue.put(conn);
        synchronized (usedObjects) {
            usedObjects--;
        }
    }   
}
