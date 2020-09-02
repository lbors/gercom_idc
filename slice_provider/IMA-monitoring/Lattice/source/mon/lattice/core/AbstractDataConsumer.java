// AbstractDataConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.core;

import mon.lattice.core.plane.InfoPlane;
import mon.lattice.core.plane.ControlPlane;
import mon.lattice.core.plane.DataPlane;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

import javax.swing.event.EventListenerList;  // who knows why this in swing


/**
 * An AbstractDataConsumer interacts with the data plane, control plane,
 * and the info plane and collects measurements.
 * The measurements are passed on to all the Reporters added to the Consumer.
 */
public abstract class AbstractDataConsumer extends AbstractPlaneInteracter implements MeasurementReceiver, Runnable {  
    /*
     * EventListenerList for Reporters.
     * It behaves similarly to may Java Event distribution mechansims:
     * you add listeners and they all get the Event.  In this case the
     * Event is a Measurement. Usage based on javax.swing.event.EventListenerList.
     * @see javax.swing.event.EventListenerList
     */
    EventListenerList listenerList = null;


    /*
     * The queue of measurements that have been received from the network.
     * They measurements are queued up and then send to the Reporters
     * in this thread.
     * By using a BlockingQueue we get locking and synchronization built-in,
     * and saves having to build it ourselves.
     */
    protected LinkedBlockingQueue measurementQueue;

    /*
     * My Thread.
     */
    Thread myThread = null;

    /*
     * Thread running?
     */
    protected boolean threadRunning = false;



    /**
     * Construct an AbstractDataConsumer.
     */
    public AbstractDataConsumer() {
	listenerList = new EventListenerList();

	// set up queue
	measurementQueue = new LinkedBlockingQueue();

	// start QueueHandling
	startQueueHandlingThread();
    }


    /**
     * Activate the forwarding from the queue to the Reporters by starting the thread.
     */
    protected synchronized void startQueueHandlingThread() {
	if (!threadRunning) {
	    myThread = new Thread(this, "DataConsumer");
	    threadRunning = true;
	    myThread.start();
	}
    }

    /**
     * Deactivate the forwarding from the queue to the Reporters by stopping the thread.
     */
    protected synchronized void stopQueueHandlingThread() {
	if (threadRunning) {
	    threadRunning = false;
	    myThread.interrupt();
	}
    }

    /**
     * Set the DataPlane this is a delegate for.
     */
    public PlaneInteracter setDataPlane(DataPlane dataPlane) {
	// set dataPlane
	this.dataPlane = dataPlane;

	// bind the DataPlane to the receiver
	if (dataPlane instanceof MeasurementReporting) {
	    ((MeasurementReporting)dataPlane).setMeasurementReceiver(this);
	}

	return this;
    }

    /**
     * Set the ControlPlane this is a delegate for.
     */
    public PlaneInteracter setControlPlane(ControlPlane controlPlane) {
	// set controlPlane
	this.controlPlane = controlPlane;


	// bind the ControlPlane to the receiver
	if (controlPlane instanceof MeasurementReporting) {
	    ((MeasurementReporting)controlPlane).setMeasurementReceiver(this);
	}

	return this;
    }

    /**
     * Set the InfoPlane this is a delegate for.
     */
    public PlaneInteracter setInfoPlane(InfoPlane infoPlane) {
	// set infoPlane
	this.infoPlane = infoPlane;


	// bind the InfoPlane to the receiver
	if (infoPlane instanceof MeasurementReporting) {
	    ((MeasurementReporting)infoPlane).setMeasurementReceiver(this);
	}

	return this;
    }

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	// start QueueHandling
	startQueueHandlingThread();

        return super.connect();
    }

    /**
     * Is this connected to a delivery mechansim.
     */
    public boolean isConnected() {
	return super.isConnected();
    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
        // stop QueueHandling
        stopQueueHandlingThread();

	return super.disconnect();
    }


    /**
     * Set the Reporter object.
     * This throws away all the other listeners first.
     * It behaves like: clearReporters(); addReporter(reporter);
     */
    public Reporter setReporter(Reporter reporter) {
	// remove all existing listeners
	clearReporters();

	// add the specified one
	addReporter(reporter);
	return reporter;
    }

    /*
     * TODO:  this should run it its own thread so that
     * the network receivers can return immediately
     * and let this deal with reporting independently
     * of the network layer.
     */

    /**
     * Receiver of a measurement, with the measurement.
     * It passes the Measurement on to all of the Reporters.
     */
    public Measurement report(Measurement m) {
	try {
	    // add the Measurement to the queue
	    //System.err.println("+" +  m.getProbeID() + "." + m.getSequenceNo());
	    measurementQueue.put(m);
	    return m;
	} catch (InterruptedException ie) {
	    System.err.println("Can't add Measurement " + m + " to queue");
	    return null;
	}
    }

    /**
     * The thread body.
     * It sends stuff from the queue onto the Reporters.
     */
    public void run() {
	// code to run at begining of thread
	beginThreadBody();

	while (threadRunning) {
	    
	    // get Measurement off queue
	    Measurement m = null;

	    try {
		// by doing a take() this waits for the queue have
		// something in it. this means we don't have to build
		// our own locking and synchronization mechanism
		m = (Measurement)measurementQueue.take();
		//System.err.println("-" +  m.getProbeID() + "." + m.getSequenceNo());
	    } catch (InterruptedException ie) {
		//System.err.println("Can't take Measurement " + m + " from queue");
		// loop round
		continue;
	    }
    
	    fireEvent(m);
	}
	 
	// code to run at end of thread
	endThreadBody();

	//System.out.println("exit thread loop for " + this);
   }


    /**
     * Add a Reporter.
     */
    public void addReporter(Reporter l) {
	listenerList.add(Reporter.class, l);
    }

    /**
     * Remove a Reporter.
     */
    public void removeReporter(Reporter l) {
	listenerList.remove(Reporter.class, l);
    }

    /**
     * List all Reporters.
     */
    public Object[] getReporters() {
	return (Object[])listenerList.getListenerList();
    }

    /**
     * Count the Reporters.
     */
    public int getReporterCount() {
	return listenerList.getListenerCount();
    }

    /**
     * Clear away all the Reporters.
     */
    public void clearReporters() {
	Object[] oldReporters = getReporters();
	for (Object r : oldReporters) {
	    if (r instanceof Reporter) {
		removeReporter((Reporter)r);
	    }
	}
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on Measurements.
     */
    protected void fireEvent(Measurement measurement) {
	// Guaranteed to return a non-null array
	Object[] listeners = getReporters();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i] == Reporter.class) {
		Reporter reporter = (Reporter)listeners[i+1];

                if (reporter instanceof ReporterMeasurementType) {
                    // can we find out if the Reporter will accept this measurement
                    List<String> types = ((ReporterMeasurementType)reporter).getMeasurementTypes();

                    if (types == null) {
                        // no info, so just pass on
                        reporter.report(measurement);

                    } else {
                        if (types.contains(measurement.getType())) {
                            // it does accept it
                            // so just pass on
                            reporter.report(measurement);

                        } else {
                            // it doesnt accept it
                            // so do notihng
                        }
                    }
                } else {
                    reporter.report(measurement);
                }


	    }
	}
    }

    /**
     * The code to run at the begining of the thread body.
     * Used to set things up.
     */
    public void beginThreadBody() {}


    /**
     * The code to run at the end of the thread body.
     * Used to tidy things up.
     */
    public void endThreadBody() {}


}
