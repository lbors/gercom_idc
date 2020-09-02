/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.hypervisor.libvirt;

import java.net.UnknownHostException;
import mon.lattice.appl.datasources.ZMQDataSourceDaemon;
import mon.lattice.core.Probe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public final class HypervisorDataSourceDaemon extends ZMQDataSourceDaemon {
    
    HypervisorCache hypervisor;
    private static Logger LOGGER;
    
    public HypervisorDataSourceDaemon(String myID, String myDSName, String dataConsumerName, int dataConsumerPort, String infoPlaneRootName, int infoPlaneRootPort, String controlHostAddress, int controlHostPort) throws UnknownHostException {
        super(myID, myDSName, dataConsumerName, dataConsumerPort, infoPlaneRootName, infoPlaneRootPort, controlHostAddress, controlHostPort);
        LOGGER = LoggerFactory.getLogger(HypervisorDataSourceDaemon.class);
    }
    
    public void setHypervisor(HypervisorCache hyp) {
	hypervisor = hyp;
    }

    /**
     *  Add a new probe for a vee.
     */
    public boolean addVEEProbe(int id, String vee, String currentHost) {
	LOGGER.info("NEW PROBE: " + vee);
	// try and get the probe
	Probe veeProbe = dataSource.getProbeByName(vee);

	if (veeProbe == null) {
	    // it doesn't exists, so add it
	    Probe p = new HypervisorProbe(id, vee, currentHost, hypervisor);
	    dataSource.addProbe(p);
	    dataSource.activateProbe(p);
	    dataSource.turnOnProbe(p);

	    System.out.println("Probe: " + p);
	    return true;
	} else {
	    // it does exist, so we've added this one before
	    return false;
	}
    }

    /**
     *  Delete a probe for a vee.
     */
    public boolean deleteVEEProbe(String vee) {
	LOGGER.info("DELETE PROBE: " + vee);
	// try and get the probe
	Probe veeProbe = dataSource.getProbeByName(vee);

	if (veeProbe != null) {
	    dataSource.turnOffProbe(veeProbe);
	    dataSource.deactivateProbe(veeProbe);
	    dataSource.removeProbe(veeProbe);
	    return true;
	} else {
	    // it doesn't exist
	    return false;
	}
    }
}
