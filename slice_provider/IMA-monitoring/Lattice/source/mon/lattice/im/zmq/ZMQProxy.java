/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.zmq;

import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQProxy extends Thread {
    
    ZMQ.Context context;
    ZMQ.Socket backend;
    ZMQ.Socket frontend;
    
    String internalURI = "inproc://infoplane";
    
    int localPort;
    
    public ZMQProxy(int localPort) {
        this.localPort = localPort;
        
        context = ZMQ.context(1);
        backend = context.socket(ZMQ.XPUB);
        frontend = context.socket(ZMQ.XSUB);
    }

    public ZMQ.Context getContext() {
        return context;
    }
    
    public String getInternalURI() {
        return internalURI;
    }
    
    public boolean startProxy() {
        this.setName("zmq-info-proxy");
        this.start();
        return true;
    }
    
    public boolean stopProxy() {
        frontend.close();
        backend.close();
        context.term();
        return true;
    }
    
    @Override
    public void run() {
        frontend.bind("tcp://*:" + localPort);
        backend.bind("tcp://*:" + (localPort + 1));
        backend.bind(internalURI);
        ZMQ.proxy(frontend, backend, null);
    }
    
    
}
