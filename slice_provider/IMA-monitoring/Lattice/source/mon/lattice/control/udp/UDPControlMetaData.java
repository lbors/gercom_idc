/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.udp;

import mon.lattice.distribution.MetaData;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 *
 * @author uceeftu
 */
public class UDPControlMetaData implements MetaData, Serializable{
    private InetAddress address;
    private int port;
    private int length=0;

    public UDPControlMetaData(InetAddress srcAddress, int port) {
        this.address = srcAddress;
        this.port = port;
    }
    
    public UDPControlMetaData(InetAddress srcAddress, int port, int length) {
        this(srcAddress, port);
        this.length = length;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(address,port);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
    
    @Override
    public String toString() {
        return address + ":" + port + " => " + length;
    }
    
    
    
}
