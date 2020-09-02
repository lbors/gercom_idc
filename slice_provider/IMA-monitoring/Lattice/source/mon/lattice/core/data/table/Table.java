// Table.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package mon.lattice.core.data.table;

import mon.lattice.core.TypeException;
import java.util.List;
import java.io.Serializable;

/**
 * An interface for a Table that can be used as a ProbeAttributeType.
 */
public interface Table extends Serializable {
    /**
     * Define all columns
     */
    public Table defineTable(TableHeader header);

    /**
     * Define all columns.
     */
    public Table defineTable(List<TableAttribute> attrs);

    /**
     * Add a row to the table.
     */
    public Table addRow(TableRow row) throws TableException, TypeException;

    /**
     * Add a row to the table.
     * Pass in a List of TableValue.
     */
    public Table addRow(List<TableValue> row) throws TableException, TypeException;

    /**
     * Get a row.
     */
    public TableRow getRow(int rowNo);

    /**
     * Delete a row from the Table.
     * Passed in as a TableRow.
     */
    public Table deleteRow(TableRow row);
     
    /**
     * Delete a row from the Table.
     * Namely, the nth row.
     */
    public Table deleteRow(int rowNo);
     
    /**
     * Get the number of columns in the table.
     */
    public int getColumnCount();

    /**
     * Get the number of rows in the table.
     */
    public int getRowCount();


    /**
     * Get the list of all the column definitions.
     */
    public TableHeader getColumnDefinitions();

    /**
     * Convert the Table to a List of TableRows.
     */
    public List<TableRow> toList();

}
