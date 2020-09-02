/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.reporters;

import mon.lattice.core.AbstractReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.core.Timestamp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.form;

/**
 *
 * @author uceeftu
 */
public class InfluxDBReporter extends AbstractReporter  {
    String serverAddress;
    String serverPort;
    String influxDBURI;
    String database;
    
    Resty resty = new Resty();
    
    private Logger LOGGER = LoggerFactory.getLogger(InfluxDBReporter.class);

    public InfluxDBReporter(String address, String port, String database) {
        super("influxDB-reporter");
        this.serverAddress = address;
        this.serverPort = port;
        this.database= database;
        this.influxDBURI = "http://" + serverAddress + ":" + serverPort + "/write?db=" + database + "&precision=ms";       
        // should check it the DB exists and create it in case   
    }
    
    @Override
    public void report(Measurement m) {
        //should create a buffer and flushing it every N received measurements
        LOGGER.debug("Received measurement: " + m.toString());
        LOGGER.info("Received measurement: " + m.toString());
        
        Timestamp timestamp = m.getTimestamp();

        if ("Table".equals(m.getType())){
            
            List<ProbeValue> tValues = m.getValues();
        
            // we get the first Probe value containing the whole table
            ProbeValue tableValue = tValues.get(0);
        
            // we get the whole table and the table header
            Table table = (Table)tableValue.getValue();
            TableHeader columnDefinitions = table.getColumnDefinitions();
            
            int rowsNumber = table.getRowCount();
            int columnsNumber = table.getColumnCount();
            TableRow row;
                 
            for (int i=0; i < rowsNumber; i++) {
                
                row = table.getRow(i);
                
                List<Integer> metrics = new ArrayList<Integer>();
                List<Integer> tags = new ArrayList<Integer>();

                for (int j=0; j < row.size(); j++) {
                    
                   
                    if ( IsMetric(columnDefinitions.get(j).getName()) == true){
                        metrics.add(j);
                    }else{
                        tags.add(j);
                    }
                }    
                    
                for (int k=0; k < metrics.size(); k++) {
                    
                    StringBuilder formattedMeasurement = new StringBuilder();
                    
                    formattedMeasurement.append(columnDefinitions.get(metrics.get(k)).getName());
                    LOGGER.info("metrics.get: "+metrics.get(k));  
                    for (int l=0; l < tags.size(); l++) {
                        LOGGER.info("tags.get: "+tags.get(l));
                        formattedMeasurement.append("," + columnDefinitions.get(tags.get(l)).getName()+"=")
                                            .append(row.get(tags.get(l)).getValue());
                    }
                    
                    //                    .append("," + "SliceID=")
                    //                    .append(m.getGroupID())
                    formattedMeasurement.append(" " + "value=")
                                        .append(row.get(metrics.get(k)).getValue())
                                        .append(" ")
                                        .append(timestamp)
                                        .append("\n");
                    
                    LOGGER.debug(formattedMeasurement.toString());
                    LOGGER.info("teste formato: "+formattedMeasurement.toString());
                    LOGGER.info("InfluxDB Entry: " + m.toString());
                    try {
                        resty.json(influxDBURI, form(formattedMeasurement.toString()));
                    } catch (IOException e) {
                        LOGGER.error("Error while writing measurement to the DB: " + e.getMessage());
                        for (int l=0; l< e.getStackTrace().length; l++)
                            LOGGER.error(e.getStackTrace()[l].toString());
                    }
                }

                }            
            } 
            else{

            StringBuilder formattedMeasurement = new StringBuilder();
            List<ProbeValue> values = m.getValues();    
                //LOGGER.info(""+(ProbeValueWithName)attribute).getName());

                for(int i = 4; i < values.size(); i++){
                    ProbeValue sliceID = values.get(0);
                    ProbeValue slicepartID = values.get(1);
                    ProbeValue instance = values.get(2);
                    ProbeValue type = values.get(3);
                    ProbeValue attribute = values.get(i);
                    LOGGER.info("Atribute: " + attribute.toString());
                    LOGGER.info("Name: " + ((ProbeValueWithName)attribute).getName());
                    formattedMeasurement.append(((ProbeValueWithName)attribute).getName())
                                    .append("," + "SliceID=")
                                    .append(sliceID.getValue())
                                    .append("," + "SlicePartID=")
                                    .append(slicepartID.getValue())
                                    .append("," + "Instance=")
                                    .append(instance.getValue())
                                    .append(" " + "value=")
                                    .append(attribute.getValue())
                                    .append(" ")
                                    .append(timestamp)
                                    .append("\n");
                }
            
                LOGGER.info("Measurement: " + formattedMeasurement);
         
            /*StringBuilder formattedMeasurement = new StringBuilder();
            Iterator<ProbeValue> values = m.getValues().iterator();
            
        
            while (values.hasNext()) {
        
                
                ProbeValue attribute = values.next();
                
                //LOGGER.info(""+(ProbeValueWithName)attribute).getName());
                formattedMeasurement.append(((ProbeValueWithName)attribute).getName())
                                    .append("," + ((ProbeValueWithName)attribute).getName())
                                    .append(m.getServiceID())
                                    .append("," + "resourceid=")
                                    .append(resourceId)
                                    .append(" " + "value=")
                                    .append(attribute.getValue())
                                    .append(" ")
                                    .append(timestamp)
                                    .append("\n");
            } */ 
     
            LOGGER.debug(formattedMeasurement.toString());
            LOGGER.info("InfluxDB Entry: " + m.toString());
            try {
                resty.json(influxDBURI, form(formattedMeasurement.toString()));
            }catch (IOException e) {
                LOGGER.error("Error while writing measurement to the DB: " + e.getMessage());
             for (int i=0; i< e.getStackTrace().length; i++)
                   LOGGER.error(e.getStackTrace()[i].toString());
            }
        }
   }

    public boolean IsMetric (String metric){
        
        if ("PERCENT_CPU_UTILIZATION".equals(metric) || "MEGABYTES_MEMORY_UTILIZATION".equals(metric) || "TOTAL_BYTES_DISK_IN".equals(metric) || "TOTAL_BYTES_DISK_OUT".equals(metric) || "TOTAL_BYTES_NET_RX".equals(metric) || "TOTAL_BYTES_NET_TX".equals(metric)){        
            return true;  
        }
        return false;
    }

}
