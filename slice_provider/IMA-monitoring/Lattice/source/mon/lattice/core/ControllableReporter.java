/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

/**
 * An interface for a Controllable Reporter
 * The ID of the DC the reporter is bound to should be set with
 * setDcId
 * 
 * @author uceeftu
 * 
 */
public interface ControllableReporter extends Reporter {
    public ID getId();
       
    public void setId(ID id);

    public String getName();
    
    public void setName(String name);
    
    /* set the ID of the Data Consumer the reporter is bound to */
    public void setDcId(ID dcId);
    
    public ID getDcId();
}
