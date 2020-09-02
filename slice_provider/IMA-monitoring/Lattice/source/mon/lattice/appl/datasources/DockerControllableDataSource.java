/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.datasources;

import mon.lattice.control.ProbeLoader;
import mon.lattice.core.ControllableDataSource;
import mon.lattice.core.ID;

/**
 *
 * @author uceeftu
 */
public class DockerControllableDataSource extends BasicDataSource implements ControllableDataSource, DockerDataSource {
   private int myPID;
   private DockerDataSourceConfigurator dataSourceConfigurator;
   
   public DockerControllableDataSource (String dsName) {
       super(dsName);
   } 
   
   public DockerControllableDataSource (String dsName, ID id) {
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

   @Override
    public DockerDataSourceConfigurator getDataSourceConfigurator() {
        return dataSourceConfigurator;
    }

   @Override
    public void setDataSourceConfigurator(DockerDataSourceConfigurator dataSourceConfigurator) {
        this.dataSourceConfigurator = dataSourceConfigurator;
    }
    
}
