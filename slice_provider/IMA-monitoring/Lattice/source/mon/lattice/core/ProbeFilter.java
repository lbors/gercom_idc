// ProbeFilter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package mon.lattice.core;

/**
 * This interface is used to define a filter for a Probe.
 * The filter is called if a filter has been set and
 * filtering is turned on.
 * The filter() method of the ProbeFilter is applied by the Probe to values
 * that are collected. 
 * <p>
 * If the filter returns true, then the value then it is 
 * reported otherwise the value is not reported.
 */
public interface ProbeFilter {
    /**
     * Get the name of the filter.
     */
    public String getName();

    /**
     * Filter a Measurement.
     * Gets passed the Probe this is a filter for.
     * Returns true on success, false if the filtering fails.
     */
    public boolean filter(Probe p, Measurement m);
}
