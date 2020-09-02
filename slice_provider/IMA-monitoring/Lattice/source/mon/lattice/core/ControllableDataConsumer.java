/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

import java.util.Collection;

/**
 *
 * @author uceeftu
 */
public interface ControllableDataConsumer extends PlaneInteracter {
    /**
     * Get the ID of the ControllableDataConsumer.
     */
    public ID getID();


    /**
     * Set the ControllableDataConsumer ID
     */
    public ControllableDataConsumer setID(ID id);
    
    
    /**
     * Get the ControllableDataConsumer ID
     */
    public String getName();
    
    
    /**
     * Get the ControllableDataConsumer ID
     */
    public ControllableDataConsumer setName(String dcName);
    
    
    /**
     * Get the current rate of received measurements
     */
    public Rational getMeasurementsRate(); 
    
    
    /**
     * Add a reporter to this Data Consumer
     */
    public void addReporter(ControllableReporter l);
    
    
    /**
     * Remove a reporter from this Data Consumer
     */
    public void removeReporter(ControllableReporter l);
	

    /**
     * List all Reporters.
     */
    public Collection<ControllableReporter> getReportersCollection();
    
    
    
    /**
     * Return the reporter object given the related ID
     */
    public ControllableReporter getReporterById(ID reporterID);
    
    
    /**
     * Return the PID of this Data Consumer
     */
    public int getMyPID();
    
}
