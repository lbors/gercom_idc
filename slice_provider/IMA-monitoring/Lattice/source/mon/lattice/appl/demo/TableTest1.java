// TableTest1.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.appl.demo;

import mon.lattice.core.data.table.DefaultTableValue;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.TableRow;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.TableException;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.TypeException;
import java.util.*;
import java.io.IOException;

/**
 * A test of the Table type.
 */
public class TableTest1 {
    public static void main(String[] args) throws TableException, TypeException {
	// allocate a table
	Table table1 = new DefaultTable();

	// define the header
	TableHeader header = new DefaultTableHeader().
	    add("name", ProbeAttributeType.STRING).
	    add("type", ProbeAttributeType.STRING);

	table1.defineTable(header);

	// add a row of values
	TableRow r0 = new DefaultTableRow().
	    add(new DefaultTableValue("stuart")).
	    add(new DefaultTableValue("person"));

	table1.addRow(r0);

	table1.addRow(new DefaultTableRow().add("hello").add("world"));


	System.out.println(table1.getColumnDefinitions());
	System.out.println(table1.getRowCount());

	System.out.println(table1);
    }
}