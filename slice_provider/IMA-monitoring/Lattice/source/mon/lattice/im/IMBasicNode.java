/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im;

/**
 *
 * @author uceeftu
 */
public interface IMBasicNode {
    
     /**
     * Connection methods to the DHT peers.
     */
    
    public boolean connect();

    public boolean disconnect();  
    
    public String getRemoteHostname();
    
    
    
}
