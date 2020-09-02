/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.udp;

import mon.lattice.distribution.Transmitting;

/**
 *
 * @author uceeftu
 */
public interface TransmittingAnnounce extends Transmitting {
    public boolean announce();
    
}
