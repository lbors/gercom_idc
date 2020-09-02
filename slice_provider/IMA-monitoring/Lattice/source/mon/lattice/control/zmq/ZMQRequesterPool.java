/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.distribution.Transmitting;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public final class ZMQRequesterPool {
    /*volatile*/ Integer usedObjects;
    int maxSize;
    LinkedBlockingQueue<ZMQRequester> socketQueue;
    Transmitting transmitting;
    ZMQ.Context context;

    public ZMQRequesterPool(Transmitting transmitting, int size, ZMQ.Context ctx) throws IOException {
        this.transmitting = transmitting;
        this.maxSize = size;
        this.socketQueue = new LinkedBlockingQueue(size);
        this.usedObjects = 0;
        this.context = ctx;
    }
    
    public void disconnect() throws IOException {
        for (ZMQRequester t: socketQueue)
            t.end();
    }
    
    public ZMQRequester getConnection() throws IOException, InterruptedException {
        synchronized (usedObjects) {
            if (socketQueue.isEmpty() && usedObjects < maxSize) {
                usedObjects++;
                ZMQRequester requester = new ZMQRequester(transmitting, context);
                requester.connect();
                return requester;
            }
        }
        ZMQRequester t = socketQueue.take();
        synchronized (usedObjects) {
            usedObjects++;
        }
        return t;
    }
    
    public void releaseConnection(ZMQRequester conn) throws InterruptedException {
        socketQueue.put(conn);
        synchronized (usedObjects) {
            usedObjects--;
        }
    }   
}
