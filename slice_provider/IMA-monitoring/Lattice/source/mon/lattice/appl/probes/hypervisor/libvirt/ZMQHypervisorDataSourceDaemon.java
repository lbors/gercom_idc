/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.hypervisor.libvirt;

import java.net.UnknownHostException;
import mon.lattice.appl.datasources.ZMQDataSourceDaemon;

/**
 *
 * @author celso
 */
public class ZMQHypervisorDataSourceDaemon  extends ZMQDataSourceDaemon {
    
    HypervisorCache hypervisor;

    public ZMQHypervisorDataSourceDaemon (
                           String myID,
                           String myDSName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           String controlHostAddress,
                           int controlHostPort
                           ) throws UnknownHostException {
        
        
        super(myID,myDSName,dataConsumerName,dataConsumerPort,infoPlaneRootName, infoPlaneRootPort, controlHostAddress, controlHostPort);
    
    }
    
    
    
    
    
}
