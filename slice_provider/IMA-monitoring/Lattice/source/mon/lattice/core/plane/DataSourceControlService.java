// DataSourceControlService.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package mon.lattice.core.plane;

import mon.lattice.core.Timestamp;
import mon.lattice.core.Measurement;
import mon.lattice.core.ID;
import mon.lattice.core.Rational;
import mon.lattice.control.ControlServiceException;

/**
 * An interface for control.
 * TODO: Determine the methods of the DataSourceControlService.
 */
public interface DataSourceControlService {
    // Probe methods

    /*
     * The are methods that are also in the ProbeInfo
     * interface in the 'core' package.
     */

    /**
     * Get the ID of the Probe.
     */
    // NOT NOW // public ID getID(ID probeID);

    /**
     * Set the Probe ID
     */
    // NOT NOW // public Probe setID(ID probeID, ID id);
    
    
    /**
     * Load a probe given its description as a ProbeLoader object 
     */
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws ControlServiceException;
    
    
    /**
     * unload a probe given its probeID 
     */
    public boolean unloadProbe(ID probeID) throws ControlServiceException;
    
    
    /**
     * Get the name of the Probe
     */
    public String getProbeName(ID probeID);

    /**
     * Set the name of the Probe
     */
    public boolean setProbeName(ID probeID, String name);

    /**
     * Get the Service ID of the Probe.
     */
    public ID getProbeServiceID(ID probeID) throws ControlServiceException;

    /**
     * Set the Service ID for a Probe
     */
    public boolean setProbeServiceID(ID probeID, ID id) throws ControlServiceException;

    /**
     * Get the Group ID of the Probe.
     */
    public ID getProbeGroupID(ID probeID);

    /**
     * Set the Group ID for a Probe
     */
    public boolean setProbeGroupID(ID probeID, ID id) throws ControlServiceException;


    /**
     * Get the data rate for a Probe 
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(ID probeID) throws ControlServiceException;

    /**
     * Set the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public boolean setProbeDataRate(ID probeID, Rational dataRate) throws ControlServiceException;

    /**
     * Get the last measurement that was collected.
     */
    public Measurement getProbeLastMeasurement(ID probeID);

    /**
     * Get the last time a measurement was collected.
     */
    public Timestamp getProbeLastMeasurementCollection(ID probeID);

    /*
     * The are methods that are also in the ProbeLifecycle
     * interface in the 'core' package.
     */

    /**
     * Turn on a Probe
     */
    public boolean turnOnProbe(ID probeID) throws ControlServiceException;

    /**
     * Turn off a Probe
     */
    public boolean turnOffProbe(ID probeID) throws ControlServiceException;

    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(ID probeID);

    /**
     * Activate the probe
     */
    public boolean activateProbe(ID probeID);

    /**
     * Deactivate the probe
     */
    public boolean deactivateProbe(ID probeID);

    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe actually running. 
     */
    public boolean isProbeActive(ID probeID);


    // Data Source methods

    /**
     * Get the name of the DataSource
     */
    public String getDataSourceInfo(ID dataSourceID) throws ControlServiceException;

    /**
     * Set the name of the DataSource
     */
    public boolean setDataSourceName(String name);


}
