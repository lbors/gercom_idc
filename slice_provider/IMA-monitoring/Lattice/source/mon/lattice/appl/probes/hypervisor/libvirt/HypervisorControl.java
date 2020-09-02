// HypervisorControl.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.probes.hypervisor.libvirt;

import mon.lattice.appl.demo.DynamicControl;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;
import mon.lattice.core.ID;

/**
 * This class monitors a user's processes.
 */
public class HypervisorControl extends DynamicControl {
    // The hypervisor cache
    HypervisorCache hypervisor;

    // the Hypervisor URI
    String hypervisorURI;

    // the start time
    long startTime = 0;

    // the HypervisorDataSource
    HypervisorDataSourceDaemon dataSource;

    // a list of seen vees
    LinkedList<String> seenVEEs;

    // Current hostname
    String currentHost = "localhost";
    

    /**
     * Construct a HypervisorControl, given a specific HypervisorDataSource 
     * and a hypervisor URI such as "xen:///"
     */
    public HypervisorControl(String hypervisorURI) {
	super("hypervisor_control");
	this.hypervisorURI = hypervisorURI;

	// set up the hypervisor connection
	hypervisor = new HypervisorCache(hypervisorURI);
	hypervisor.activateControl();

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}
    }

    public HypervisorControl() {
	super("proc_control");

	// set up the hypervisor connection
	hypervisor = new HypervisorCache(hypervisorURI);
	hypervisor.activateControl();

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}
    }

    
    /**
     * Set the DataSource which creates the probes.
     */
    public HypervisorControl setDataSource(HypervisorDataSourceDaemon dataSource) {
	this.dataSource = dataSource;
	return this;
    }

    /**
     * Initialize this hypervisor control.
     * It connects to the hypervisor
     */
    protected void controlInitialize() {
	System.err.println("HypervisorControl: controlInitialize()");

	// sleep time is 30 secs
	setSleepTime(10);  // was 10

	startTime = System.currentTimeMillis();
	seenVEEs = new LinkedList<String>();

	// give the DataSource the hypervisor
	dataSource.setHypervisor(hypervisor);

	// wait for the hypervisor cache to be ready
	hypervisor.waitForCacheReady();
    }

    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
	System.err.println("HypervisorControl: controlEvaluate() START");

	// Get the domain list from the hypervisor
	// This determines how many vees we have open
	// Each probe will do detailed analysis
	long now = System.currentTimeMillis();
	long diff = (now - startTime) / 1000;
	System.err.println(diff + ": " + this + " seen " + seenVEEs.size());

	// get the list of current VEEs
	int[] activeDoms = hypervisor.listDomains();

	LinkedList<String> seenThisTime = new LinkedList<String>();

	// get the domain info
	if (hypervisor.numOfDomains() > 0) {
	    for(int d: activeDoms) {
		CachedDomain domain = hypervisor.domainLookupByID(d);
		String vee = domain.getName();

		if (seenVEEs.contains(vee)) {
		    // we've already seen this vee
		    seenThisTime.add(vee);
		} else {
		    // we've not seen this vee
		    // so we need to add a probe for it
		    seenThisTime.add(vee);

		    dataSource.addVEEProbe(d, vee, currentHost);
		}
	    }
	}


	// we've got to the end, so now we determine
	// if there are vees we have in the list that are
	// not used any more
	seenVEEs.removeAll(seenThisTime);

	if (seenVEEs.size() == 0) {
	    // there are no vees 
	} else {
	    // the are some residual vees synchronized
	    // delete the probe for each one
	    for (String aVEE : seenVEEs) {
		dataSource.deleteVEEProbe(aVEE);
	    }
	}

	// save the current list
	seenVEEs = seenThisTime;

	System.err.println("HypervisorControl: controlEvaluate() END");

    }

    /**
     * Cleanup
     */
    protected void controlCleanup() {
   }


    /**
     * Main entry point.
     */
    public static void main(String[] args) {
	try {
            String dsID = ID.generate().toString();
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            String controllerHost = null;
            int controllerPort = 5555;
            
            String hypervisorURI = "qemu:///system";
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    dsName = dataConsumerAddr = controllerHost = loopBack;
                    infoHost = InetAddress.getLocalHost().getHostName();
                    break;
                case 6:
                    dataConsumerAddr = args[0];
                    sc = new Scanner(args[1]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    hypervisorURI = args[5];
                    break;
                case 7:
                    dsID = args[0];
                    dataConsumerAddr = args[1];
                    sc = new Scanner(args[2]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = controllerHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controllerPort = sc.nextInt();
                    dsName = InetAddress.getLocalHost().getHostName();
                    hypervisorURI = args[6];
                    break;
                default:
                    System.err.println("use: HypervisorControl [UUID] dcAddress dcPort infoHost infoPort controllerHost controllerPort");
                    System.exit(1);
            }

	// allocate a HypervisorControl to interact with the HypervisorDataSource
	HypervisorControl control = new HypervisorControl(hypervisorURI);

	// allocate a DataSource that can add and delete new PS probes
	HypervisorDataSourceDaemon dataSource = new HypervisorDataSourceDaemon(
                                                            dsID,
                                                            dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort,
                                                            controllerHost, 
                                                            controllerPort);

	control.setDataSource(dataSource);
	control.activateControl();
        
        } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Error while starting the Data Source " + ex.getMessage());
        }
    }
}