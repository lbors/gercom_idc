/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

import mon.lattice.im.delegate.ReporterInformationManager;
import mon.lattice.core.AbstractDataConsumer;
import mon.lattice.core.MeasurementReceiver;
import mon.lattice.core.ControllableReporter;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import java.util.HashMap;
import java.util.Map;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.Rational;
import java.util.Collection;

/**
 * Extends AbstractDataConsumer functionalities adding remote control
 it also provides measurements rate reporting as specified in the ControllableDataConsumer Interface
 it comes with a default PrintReporter
 * @author uceeftu
 */
public final class DefaultControllableDataConsumer extends AbstractDataConsumer implements MeasurementReceiver, ControllableDataConsumer {
    /**
     * The DC ID
     */
    ID myID;
    
    private int myPID;
    
    /**
     * The DC name
     */
    String dataConsumerName;
    
    /**
     * An attribute to count the number of measurements taken off the queue
     */
    Long measurementsCounter = 0L;
    
    /**
     * Specifies the period of the measurement reporting
     */
    int mReportingInterval;
    
    /**
     *The last computed measurement rate 
     */
    Rational lastMeasurementRate;
    
    
    /**
    * a Map Reporter id to ControllableReporter
    */
    Map<ID, ControllableReporter> reporters = new HashMap<>();

    
    
    public DefaultControllableDataConsumer(String name) {
        this(name, 30); //default interval every 30 sec
    }
    
    
    public DefaultControllableDataConsumer(String name, int rate) {
        this(name, ID.generate(), rate);
    }
    
    
    public DefaultControllableDataConsumer(String dcName, ID id) {
        this(dcName, id, 30);
    }
    
    
    public DefaultControllableDataConsumer(String dcName, ID id, int rate) {
       dataConsumerName = dcName;
       this.mReportingInterval = rate;
       myID=id;
       
       // gets the PID splitting PID@hostname
       myPID = Integer.valueOf(java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
       init();
    }
    
    private void init() {
        lastMeasurementRate = new Rational(0, 1);
        startMeasurementsRateThread();
    } 

    public Long getMeasurementsCounter() {
        return measurementsCounter;
    }

    public void setMeasurementsCounter(Long measurementCounter) {
        this.measurementsCounter = measurementCounter;
    }
    
    // creates a thread to calculate the number of meaurements taken off the queue
    // in the mReportingInterval time interval
    private void startMeasurementsRateThread() {
        Thread t = new Thread(new Runnable () {
                                    @Override
                                    public void run() { 
                                        Long t1 = System.nanoTime();
                                        while(true) {
                                            try {
                                                Thread.sleep(mReportingInterval*1000);
                                                Long t2 = System.nanoTime();
                                                Long tDelta = t2 - t1;
                                                computeMeasurementsRate(tDelta/(1000*1000*1000));
                                                t1 = t2;
                                                setMeasurementsCounter(0L);
                                            } catch (InterruptedException ex) { }
                                        }
                                    }
                                  }
                             );
        t.start();
    }
    
   
    private void computeMeasurementsRate(Long interval) {
        this.lastMeasurementRate = new Rational (60 * this.getMeasurementsCounter().intValue(), interval.intValue()); // convert to samples / min
    }

    @Override
    public Rational getMeasurementsRate() {
        return this.lastMeasurementRate;
    }

    @Override
    public ID getID() {
        return myID;
    }

    @Override
    public ControllableDataConsumer setID(ID id) {
        this.myID = id;
        return this;
    }

    @Override
    public int getMyPID() {
        return myPID;
    }
    
    @Override
    public String getName() {
        return this.dataConsumerName;
    }

    @Override
    public ControllableDataConsumer setName(String dcName) {
        this.dataConsumerName = dcName;
        return this;
    }

    @Override
    public void removeReporter(ControllableReporter l) {
        super.removeReporter(l);
        reporters.remove(l.getId());
        getInfoPlane().removeReporterInfo(l);
        
    }

    @Override
    public void addReporter(ControllableReporter l) {
        //setting Data Consumer ID in the Reporter
        l.setDcId(myID);
        
        // setting InfoPlane Delegate reference to allow Probe name and
        // attribute resolution
        if (l instanceof AbstractReporterWithInfoPlaneDelegate)
            ((AbstractReporterWithInfoPlaneDelegate)l).setInfoPlaneDelegate(new ReporterInformationManager(this.getInfoPlane()));
        
        super.addReporter(l);
        reporters.put(l.getId(), l);
        getInfoPlane().addReporterInfo(l);
    }

    @Override
    public ControllableReporter getReporterById(ID reporterID) {
        return reporters.get(reporterID);
    }
    
    public Collection<ControllableReporter> getReportersCollection() {
	return reporters.values();
    }
    
    // this overriden method from the super class only adds a measurementsCounter
    @Override
    public void run() {
	beginThreadBody();
	while (threadRunning) {
	    Measurement m = null;
	    try {
		m = (Measurement)measurementQueue.take();
	    } catch (InterruptedException ie) {
		continue;
	    }
	    fireEvent(m);
            // counts the number of measurements taken off the queue
            measurementsCounter++;
	}
	endThreadBody();
   }  
}
