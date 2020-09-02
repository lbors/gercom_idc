/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import java.net.InetAddress;

/**
 *
 * @author uceeftu
 */
public class SocketControlEndPointMetaData extends ControlEndPointMetaData {
    private InetAddress host = null;
    private int port;

    
    public SocketControlEndPointMetaData(String type, InetAddress host, int port) {
        super(type);
        this.host = host;
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public InetAddress getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
}
