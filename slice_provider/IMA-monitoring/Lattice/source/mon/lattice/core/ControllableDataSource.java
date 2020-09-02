/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

import mon.lattice.control.ProbeLoader;

/**
 *
 * @author uceeftu
 */
public interface ControllableDataSource extends DataSource, DataSourceDelegateInteracter {
    public ID addProbe(ProbeLoader p);
    
    public int getMyPID();
      
    public void setMyPID(int myPID);
}
