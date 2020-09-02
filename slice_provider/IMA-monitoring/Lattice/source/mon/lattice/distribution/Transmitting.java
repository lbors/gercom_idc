/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.distribution;

/**
 *
 * @author uceeftu
 */
public interface Transmitting {
    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id);
    
}
