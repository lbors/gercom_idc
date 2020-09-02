// InfoService.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package mon.lattice.core.plane;

import mon.lattice.core.ProbeAttribute;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.Probe;
import mon.lattice.core.ID;
import mon.lattice.core.DataSource;
import mon.lattice.core.Reporter;
import java.io.Serializable;

/**
 * The InfoService
 * This has the common methods for getting info.
 */
public interface InfoService {
    /*
     * Look up things.
     */

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupDataSourceInfo(DataSource dataSource, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses DataSource ID.
     */
    public Object lookupDataSourceInfo(ID dataSourceID, String info);

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupProbeInfo(Probe probe, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeInfo(ID probeID, String info);

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info);
    
    /**
     * Lookup some DC info in the InfoPlane.
     * Mostly used at the management end, as it uses DC ID.
     */
    public Object lookupDataConsumerInfo(ID dataConsumerID, String info);
    
    /**
     * Lookup some Reporter info in the InfoPlane.
     */
    public Object lookupReporterInfo(ID reporterID, String info);
    
    
    
    public Object lookupProbesOnDS(ID dataSourceID);

    /*
     * Add things
     */

    /**
     * Add DataSource info.
     */
    public boolean addDataSourceInfo(DataSource ds);

    /**
     * Add Probe info.
     */
    public boolean addProbeInfo(Probe p);

    /**
     * Add ProbeAttribute Info to a Probe Info.
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa);
    
    /**
     * Add ControllableDataConsumer info.
     */
    public boolean addDataConsumerInfo(ControllableDataConsumer dc);
    
    /**
     * Add Reporter info.
     */
    public boolean addReporterInfo(Reporter r);
    

    /*
     * Modify things
     */

    /**
     * Modify DataSource Info.
     */
    public boolean modifyDataSourceInfo(DataSource ds);

    /**
     * Modify Probe Info.
     */
    public boolean modifyProbeInfo(Probe p);

    /**
     * Modify  ProbeAttribute Info for a Probe.
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa);

    /*
     * Remove things
     */

    /**
     * Remove DataSource Info.
     */
    public boolean removeDataSourceInfo(DataSource ds);

    /**
     * Remove Probe Info.
     */
    public boolean removeProbeInfo(Probe p);

    /**
     * Remove ProbeAttribute Info for a Probe.
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa);
    
    /**
     * Remove ControllableDataConsumer Info.
     */
    public boolean removeDataConsumerInfo(ControllableDataConsumer dc);
    
    
    /**
     * Remove Reporter Info.
     */
    public boolean removeReporterInfo(Reporter r);
    

    /**
     * Check if a Data Source is in the Info Plane
     */
    public boolean containsDataSource(ID dataSourceID, int timeOut);
    
    
    /**
     * Check if a Data Consumer is in the Info Plane
     */
    public boolean containsDataConsumer(ID dataConsumerID, int timeOut);
    
    
    /*
     * General .
     */

    /**
     * Put a value in the InfoPlane.
     */
    public boolean putInfo(String key, Serializable value);

    /**
     * Get a value from the InfoPlane.
     */
    public Object getInfo(String key);

    /**
     * Remove a value from the InfoPlane.
     */
    public boolean removeInfo(String key);

}