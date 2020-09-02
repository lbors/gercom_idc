/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

import mon.lattice.appl.datasources.BasicDataSource;
import mon.lattice.control.ProbeLoader;

/**
 *
 * @author uceeftu
 */
public class DefaultControllableDataSource extends BasicDataSource implements ControllableDataSource {
   private int myPID;
   
   public DefaultControllableDataSource (String dsName) {
       super(dsName);
   } 
   
   public DefaultControllableDataSource (String dsName, ID id) {
       super(dsName, id);
       // nasty way of getting the PID of the process associated to this Data Source
       // the below string gets the PID splitting PID@hostname
       myPID = Integer.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
   } 
            
   @Override
   public ID addProbe(ProbeLoader p) {  
        addProbe(p.getProbe());
        return p.getProbe().getID();
    } 

    public int getMyPID() {
        return myPID;
    }

    public void setMyPID(int myPID) {
        this.myPID = myPID;
    }
    
}
