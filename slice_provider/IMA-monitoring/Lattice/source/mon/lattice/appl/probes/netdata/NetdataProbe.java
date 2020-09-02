/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.netdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mon.lattice.core.AbstractProbe;
import mon.lattice.core.DefaultProbeValue;
import mon.lattice.core.Probe;
import mon.lattice.core.ProbeAttributeType;
import mon.lattice.core.ProbeMeasurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProducerMeasurement;
import mon.lattice.core.TypeException;
import mon.lattice.core.data.table.DefaultTable;
import mon.lattice.core.data.table.DefaultTableHeader;
import mon.lattice.core.data.table.DefaultTableRow;
import mon.lattice.core.data.table.DefaultTableValue;
import mon.lattice.core.data.table.Table;
import mon.lattice.core.data.table.TableException;
import mon.lattice.core.data.table.TableHeader;
import mon.lattice.core.data.table.TableProbeAttribute;
import mon.lattice.core.datarate.EveryNSeconds;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 *
 * @author celso
 */
public class NetdataProbe extends AbstractProbe implements Probe {

    TableHeader controllerTable;
    Resty dataRequest;
    String sliceId;
    String slicePartId; 
    String URLAddress;
    String queryCPU;
    String queryMEMmb;
    String queryMEMpc;
    String namespace;
    List<String> metrics;
    List<String> metricsQuery;
    List<String> queries;
    
    private org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NetdataProbe.class);

    public NetdataProbe(String host, String port, String probeName, String slicePartId, String sliceId, String dataRate, String[] metrics) {
        LOGGER.info("Iniciou o netdata sem namespace");
        this.sliceId = sliceId;
        
        this.slicePartId = slicePartId;
        
        this.metrics = new ArrayList<>();
        
        this.URLAddress = "http://" + host + ":" + port ;
        
        if (metrics[0].contains("[")){
                metrics[0] = metrics[0].substring(1);
        } 
        
        String[] m = metrics;
        
        for (int i = 0; i < m.length; i++){
        
            this.metrics.add(m[i]);
        }
           
        this.dataRequest = new Resty();
        
        final int intDataRate = Integer.parseInt(dataRate);
        setName(probeName);
        setDataRate(new EveryNSeconds(intDataRate));
        
        controllerTable = new DefaultTableHeader();
 
            for(int j = 0; j < this.metrics.size(); ++j){
                controllerTable.add(this.metrics.get(j), ProbeAttributeType.FLOAT);
            }
        
            controllerTable.add("SliceID", ProbeAttributeType.STRING);
            controllerTable.add("SlicePartID", ProbeAttributeType.STRING);
            controllerTable.add("ResourceID", ProbeAttributeType.STRING);
            controllerTable.add("ResourceType", ProbeAttributeType.STRING);
            
            for (int s = 0; s < controllerTable.size(); ++s){
                LOGGER.info("controller table " + controllerTable.get(s));
            }
         
            addProbeAttribute(new TableProbeAttribute(0, "SlicePart", controllerTable));    
    }

    public NetdataProbe(String host, String port, String probeName, String slicePartId, String sliceId, String dataRate, String[] metrics, String namespace) {
        
        this.namespace = namespace;
        
        this.sliceId = sliceId;
        
        this.slicePartId = slicePartId;
        
        this.metrics = new ArrayList<>();
        
        this.URLAddress = "http://" + host + ":" + port ;
        
        if (metrics[0].contains("[")){
                metrics[0] = metrics[0].substring(1);
        } 
        
        String[] m = metrics;
        
        for (int i = 0; i < m.length; i++){
        
            this.metrics.add(m[i]);
        }
           
        this.dataRequest = new Resty();
        
        final int intDataRate = Integer.parseInt(dataRate);
        setName(probeName);
        setDataRate(new EveryNSeconds(intDataRate));
        
        controllerTable = new DefaultTableHeader();
 
            for(int j = 0; j < this.metrics.size(); ++j){
                controllerTable.add(this.metrics.get(j), ProbeAttributeType.FLOAT);
            }
        
            controllerTable.add("SliceID", ProbeAttributeType.STRING);
            controllerTable.add("SlicePartID", ProbeAttributeType.STRING);
            controllerTable.add("ResourceID", ProbeAttributeType.STRING);
            controllerTable.add("ResourceType", ProbeAttributeType.STRING);
            
            for (int s = 0; s < controllerTable.size(); ++s){
                LOGGER.info("controller table " + controllerTable.get(s));
            }
         
            addProbeAttribute(new TableProbeAttribute(0, "SlicePart", controllerTable));    
    }
  
    void QueryConvertion(){
        
        this.queries = new ArrayList<>();
        
        this.metricsQuery = new ArrayList<>();
        
        queries.add("PERCENT_CPU_UTILIZATION");
        queries.add("/api/v1/data%3Fchart=system.cpu&after=-1&format=array&options=percent");
        queries.add("MEGABYTES_MEMORY_UTILIZATION");
        queries.add("/api/v1/data?chart=system.ram&dimension=used&after=-1&format=array");
        queries.add("TOTAL_BYTES_DISK_IN");
        queries.add("/api/v1/data?chart=system.io&dimension=in&after=-1&format=array");
        queries.add("TOTAL_BYTES_DISK_OUT");
        queries.add("/api/v1/data?chart=system.io&dimension=out&after=-1&format=array");
        queries.add("TOTAL_BYTES_NET_RX");
        queries.add("/api/v1/data?chart=system.net&dimension=received&after=-1&format=array");
        queries.add("TOTAL_BYTES_NET_TX");
        queries.add("/api/v1/data?chart=system.net&dimension=sent&after=-1&format=array");

        for (int i = 0; i < this.metrics.size(); ++i){
            for (int j = 0; j < this.queries.size(); ++j){
                if (metrics.get(i) == null ? queries.get(j) == null : metrics.get(i).equals(queries.get(j))){
                        metricsQuery.add(queries.get(j+1));
                }   
            }           
        }

        for (int h = 0; h < metricsQuery.size(); ++h){
            LOGGER.info("MetricsQuery: " + metricsQuery.get(h));
        }        
        
    }

    public List<String> getInstance() throws IOException, JSONException {
       
        List<String> instance = new ArrayList<>();
        String requestURL = this.URLAddress + "/api/v1/info";
        LOGGER.info("URL: "+ requestURL);
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONArray  jsresult = jsobj.getJSONArray("mirrored_hosts");
        
        LOGGER.info("Chegou a pegar o host: " + jsresult.getString(0));
        
        for (int i = 0; i < jsresult.length(); i++) {
            
            instance.add(i,jsresult.getString(i));
            LOGGER.info("Instance " + instance.get(i));
        }
 
        return instance;
    }
 
    public String getMetrics(String query, String internalHost) throws IOException, JSONException {
        
        
        String requestURL = this.URLAddress + "/host/" + internalHost + query;
        
        LOGGER.info("Request_URL: "+ requestURL);
        
        JSONArray jsArray = dataRequest.json(requestURL).array();
        String metricValue = jsArray.getString(0);
        
        LOGGER.info("Metric " + metricValue); 
        return metricValue;
    }
    
    @Override
    public ProbeMeasurement collect() {
       
        QueryConvertion();
       Table metricsTable = new DefaultTable();
       metricsTable.defineTable(controllerTable);
        try {
            
            List<String> hosts = new ArrayList<>();
            
            hosts = getInstance();
            
            LOGGER.info("Hosts.size: "+ hosts.size());
            
            for (int i=1; i < hosts.size(); i++){
                      
                DefaultTableRow defaultTableRow = new DefaultTableRow();
                
                LOGGER.info("Valor de i: "+ i);
                LOGGER.info("metrics.query.size: "+ metricsQuery.size());
                for (int j=0; j < metricsQuery.size(); j++) {
                    
                    LOGGER.info("Valor de j: "+ j);
                    
                    String query = metricsQuery.get(j);
                    LOGGER.info("query: " + query);
                    
                    String host = hosts.get(i);
                    LOGGER.info("host: " + host);
                    
                    defaultTableRow.add(new DefaultTableValue(Math.abs(Float.parseFloat(getMetrics(query, host)))));
                    //LOGGER.info("metrics row count for " + j +": "+ metricsTable.getRowCount()); 
                    //LOGGER.info("metrics row count for " + j +": "+ metricsTable.getRow(i)); 
                }
                
                defaultTableRow.add(new DefaultTableValue(this.sliceId));
                
                defaultTableRow.add(new DefaultTableValue(this.slicePartId));
                
                defaultTableRow.add(new DefaultTableValue(hosts.get(i)));
                
                defaultTableRow.add(new DefaultTableValue("Host"));
                
                metricsTable.addRow(defaultTableRow);
                
                LOGGER.info("TABLE: " + metricsTable); 
            }    
      
            ArrayList<ProbeValue> list = new ArrayList<>(1);
                
            list.add(new DefaultProbeValue(0, metricsTable));
                
            ProbeMeasurement m = new ProducerMeasurement(this, list, "Table");
            
            LOGGER.info("TABLE M: " + m); 
            
            return m;
    
        } catch (TableException | IOException | JSONException | TypeException ex) {
            Logger.getLogger(NetdataProbe.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    
    }          
    
}
