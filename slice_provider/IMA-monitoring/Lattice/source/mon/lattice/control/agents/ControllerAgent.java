/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.agents;

import mon.lattice.core.ID;
import mon.lattice.core.PlaneInteracter;

/**
 *
 * @author uceeftu
 */
public interface ControllerAgent extends ControllerAgentService, PlaneInteracter {
    public ID getID();
    
    public void setID(ID id);

    public String getName();

    public void setName(String name);
    
    public int getPID();
    
}
