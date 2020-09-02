// MapTest1.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2012

package mon.lattice.appl.demo;

import mon.lattice.core.data.map.MMap;
import mon.lattice.core.data.map.DefaultMMap;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;

/**
 * A test of the MMap type.
 */
public class MapTest1 {
    public static void main(String[] args) throws TypeException {
	// allocate a map
	MMap mMap = new DefaultMMap(ProbeAttributeType.STRING, ProbeAttributeType.INTEGER);

	mMap.put("one", 1);
        mMap.put("two", 2).put("three", 3);

	System.out.println(mMap);
    }
}

