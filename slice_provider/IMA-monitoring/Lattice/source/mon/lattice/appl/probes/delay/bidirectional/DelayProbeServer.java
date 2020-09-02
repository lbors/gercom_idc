/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.delay.bidirectional;

import mon.lattice.core.AbstractProbe;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.Rational;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.slf4j.LoggerFactory;

/**
 * this probe is not meant to collect any values
 * it only listens for UDP packets from the DelayProbeClient
 */

public class DelayProbeServer extends AbstractProbe {
    int port;
    DatagramSocket socket;
    

    public DelayProbeServer(String name, String port) throws SocketException {
        this.port = Integer.valueOf(port);
        socket = new DatagramSocket(this.port);
        
        setName(name);
        setDataRate(new Rational(0,1)); // the probe does not send measurements (rate = 0)
    }
    
    
    @Override
    public void run() {
	beginThreadBody();
        
        LoggerFactory.getLogger(DelayProbeServer.class).info("Starting UDP server");

	while (threadRunning) {
	    if (probeStatus == Probe.Status.OFF) {
		// the probe is OFF
		// so wait

		try {
		    waitWhileOff();
		} catch (InterruptedException ie) {
                    // jump out of wait()
		}

                // should we exit
                if (!threadRunning) {
                    break;
                }

	    } else {
                DatagramPacket request = new DatagramPacket(new byte[1024], 1024);

                try {
                    // Block until the host receives a UDP packet.
                    socket.receive(request);
                
                    // Send reply.
                    InetAddress clientHost = request.getAddress();
                    int clientPort = request.getPort();
                    byte[] buf = request.getData();
                    DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
                    socket.send(reply);

                    LoggerFactory.getLogger(DelayProbeServer.class).debug("Sent reply" );
                } catch (IOException e) {
                   LoggerFactory.getLogger(DelayProbeServer.class).error(e.getMessage());
                    
                }
            }
        }
    }

    @Override
    public ProbeMeasurement collect() {
        throw new UnsupportedOperationException("Not supported on this probe");
    }
    
    
}
