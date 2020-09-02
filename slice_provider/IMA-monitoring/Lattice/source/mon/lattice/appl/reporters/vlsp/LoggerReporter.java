package mon.lattice.appl.reporters.vlsp;

import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableRow;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A PrintReporter just prints a Measurement.
 */
public final class LoggerReporter extends AbstractReporter {
    /**
     * In this LoggerReporter, report() logs and formats the Measurement (from VLSP) to the log file.
     */
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);
    
    
    public LoggerReporter(String reporterName) {
        super(reporterName); 
    }
    
    
    @Override
    public void report(Measurement m) {
	//LOGGER.info(m.toString());
        
        List<ProbeValue> values = m.getValues();
        
        // we get the first Probe value containing the whole table
        ProbeValue tableValue = values.get(0);
        
        // we get the whole table and the table header
        Table table = (Table)tableValue.getValue();
        TableHeader columnDefinitions = table.getColumnDefinitions();

        int rowsNumber = table.getRowCount();
        int columnsNumber = table.getColumnCount();
        TableRow row;
        StringBuilder lcs = new StringBuilder();
        
        for (int i=0; i < rowsNumber; i++) {
            LOGGER.info("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            
            row = table.getRow(0);
            
            int j;
            
            lcs.append("Slice ID: ");
            lcs.append(m.getGroupID());
            
            for (j=0; j < 2; j++) {
                lcs.append(" ");
                lcs.append(columnDefinitions.get(j).getName());
                lcs.append(": ");
                lcs.append(row.get(j).getValue());
            }
            
            LOGGER.info(lcs.toString());
            lcs.setLength(0);
            
            
            for (; j < columnsNumber-1; j++) {
                lcs.append(columnDefinitions.get(j).getName());
                lcs.append(": ");
                lcs.append(row.get(j).getValue());
                lcs.append(" ");
            }
            
            // print routers table now
            Table routersTable = (Table)row.get(j).getValue();
            columnDefinitions = routersTable.getColumnDefinitions();
            
            rowsNumber = routersTable.getRowCount();
            columnsNumber = routersTable.getColumnCount();
            
            LOGGER.info(lcs.toString());
            
            StringBuilder router = new StringBuilder();
            
            for (i=0; i < rowsNumber; i++) {
                row = routersTable.getRow(i);
                
                for (j=0; j < columnsNumber; j++) {
                    router.append("\t");
                    router.append(columnDefinitions.get(j).getName());
                    router.append(": ");
                    router.append(row.get(j).getValue());
                }
                
            LOGGER.info(router.toString());    
            router.setLength(0);
            }
        
        LOGGER.info("---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        LOGGER.info("\n");
        }        
    }
}