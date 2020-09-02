/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.deployment;

import mon.lattice.core.ID;
import mon.lattice.core.EntityType;


public class ControllerAgentInfo extends LatticeEntityInfo {

    public ControllerAgentInfo(String className, String args) {
        super(EntityType.CONTROLLERAGENT, className, args);
        this.id = ID.generate();
    }
    
}
