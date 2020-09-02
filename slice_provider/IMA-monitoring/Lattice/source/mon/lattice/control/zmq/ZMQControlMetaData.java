/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.distribution.MetaData;

/**
 *
 * @author uceeftu
 */
public class ZMQControlMetaData implements MetaData {
    private String destinationIdentity;
    private int length=0;
    
    public ZMQControlMetaData(String identity) {
        destinationIdentity = identity; 
    }
    
    public ZMQControlMetaData(String identity, int length) {
        destinationIdentity = identity; 
        this.length = length;
    }
    
    public String getDestination() {
        return destinationIdentity;
    }
    
    public int getLength() {
        return length;
    }
}
