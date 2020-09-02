/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import java.util.HashSet;
import java.util.Set;
import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQRouter implements Runnable {
    ZMQ.Context context;
    ZMQ.Socket frontend;
    ZMQ.Socket backend;
    
    int backendPort;
    
    Set<String> workers = new HashSet<>();
    
    Thread router;

    public ZMQRouter(int port) {
        backendPort = port;
        context = ZMQ.context(1);
        frontend = context.socket(ZMQ.ROUTER);
        backend = context.socket(ZMQ.ROUTER);
    }
    
    public void bind() {
        frontend.bind("inproc://frontend");
        backend.bind("tcp://*:" + backendPort);
        router = new Thread(this, "zmq-router");
        router.start();
    }
    
    public ZMQ.Context getContext() {
        return this.context;
    }
    
    
    public void disconnect() {
        frontend.close();
        backend.close();
        context.term();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ZMQ.Poller items = new ZMQ.Poller (2);
            items.register(backend, ZMQ.Poller.POLLIN);
            items.register(frontend, ZMQ.Poller.POLLIN);
            
            if (items.poll() < 0)
                break;
            
            if (items.pollin(0)) {
                // we have message on the backend, it is a worker identity
                // identity is added to the workers set
                String workerIdentity = backend.recvStr(); 
                workers.add(workerIdentity);
                
                backend.recvStr(); // empty frame
                
                // worker sends the client identity to which the reply message
                // has to be sent to (or READY)
                String clientToReplyAddress = backend.recvStr();
                
                if (!clientToReplyAddress.equals("READY")) { // it is an actual identity
                    backend.recvStr(); // empty frame
                    
                    // reply message from the worker
                    byte[] reply = backend.recv();
                    
                    // sending the reply to the specified client identity
                    frontend.sendMore(clientToReplyAddress);
                    frontend.sendMore("");
                    
                    // sending worker identity
                    frontend.sendMore(workerIdentity);
                    frontend.sendMore("");
                    
                    frontend.send(reply);
                }
            }
            
            if (items.pollin(1)) {
                String clientAddress = frontend.recvStr(); // clientAddress
                frontend.recvStr(); // empty delimiter frame
                
                String workerAddress = frontend.recvStr();
                
                frontend.recvStr(); // empty frame
                byte[] message = frontend.recv();
                
                if (workers.contains(workerAddress)) { // if we can find the worker
                    // first we specify the worker 
                    backend.sendMore(workerAddress);
                    backend.sendMore("");
                    
                    // we send information about the client sending the message
                    backend.sendMore(clientAddress);
                    
                    // we send the actual message
                    backend.sendMore("");
                    backend.send(message);
                }
            }
        }
        
    }
    
    
            
    
}
