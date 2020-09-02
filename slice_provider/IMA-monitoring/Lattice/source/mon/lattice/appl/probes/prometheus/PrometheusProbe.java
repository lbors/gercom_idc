/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.prometheus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import org.slf4j.LoggerFactory;
/**
 *
 * @author celso
 */

public class PrometheusProbe extends AbstractProbe implements Probe {

    TableHeader controllerTable;
    Resty dataRequest;
    String sliceId;
    String slicePartId;
    String URLAddress;
    String queryCPU;
    String queryMEMmb;
    String queryMEMpc;
    String namespace;
    List<String> queries;
    List<String> queries_containers;
    List<String> metrics;
    List<String> metricsQuery;
    
    private org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PrometheusProbe.class);

        public PrometheusProbe(String host, String port, String probeName, String slicePartId, String sliceId, String dataRate, String[] metrics) {
             
            this.sliceId = sliceId;
            
            this.slicePartId = slicePartId;
        
            this.metrics = new ArrayList<>();
        
            this.URLAddress = "http://" + host + ":" + port + "/api/v1/query?query=";
        
            this.namespace = null;

            for (int s = 0; s < metrics.length; ++s){
                LOGGER.info("Metrica_Chegada: " + metrics[s]);
            }
            
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
 
    public PrometheusProbe(String host, String port, String probeName, String slicePartId, String sliceId, String dataRate, String[] metrics, String namespace) {
        
        this.sliceId = sliceId;
        
        this.slicePartId = slicePartId;
        
        this.metrics = new ArrayList<>();
        
        this.URLAddress = "http://" + host + ":" + port + "/api/v1/query?query=";
        
        this.namespace = namespace;
        
        if (metrics[0].contains("[")){
                metrics[0] = metrics[0].substring(1);
        } 
        
        String[] m = metrics;
        
        for (int i = 0; i < m.length; i++){
                    this.metrics.add(m[i]);
                    LOGGER.info("Metrics position " + i + ": "+this.metrics.get(i));
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
                
        addProbeAttribute(new TableProbeAttribute(0, "SlicePart", controllerTable));
    }
  
    void QueryConvertion() throws UnsupportedEncodingException{
        
        this.queries = new ArrayList<>();
        this.queries_containers = new ArrayList<>();
        this.metricsQuery = new ArrayList<>();
        
        queries.add("PERCENT_CPU_UTILIZATION");
        queries.add("100*(1-avg by(instance)(irate(node_cpu_seconds_total{mode='idle'}[5m])))");
        queries.add("MEGABYTES_MEMORY_UTILIZATION");
        queries.add("sum by(instance) (((node_memory_MemTotal_bytes-(node_memory_MemFree_bytes+node_memory_Cached_bytes+node_memory_Buffers_bytes))/1024)/1024)");
//        queries.add("node_memory_MemTotal_bytes-(node_memory_MemFree_bytes+node_memory_Cached_bytes+node_memory_Buffers_bytes)");
        queries.add("TOTAL_BYTES_DISK_IN");
        queries.add("sum by(instance)(rate(node_disk_written_bytes_total{device!='sr0'}[5m]))");
//        queries.add("rate(node_disk_written_bytes_total{device!='sr0'}[5m])");
        queries.add("TOTAL_BYTES_DISK_OUT");
        queries.add("sum by(instance)(rate(node_disk_read_bytes_total{device!='sr0'}[5m]))");
//        queries.add("rate(node_disk_read_bytes_total{device!='sr0'}[5m])");
        queries.add("TOTAL_BYTES_NET_RX");
        queries.add("sum by(instance)(rate(node_network_receive_bytes_total[5m]))");
//        queries.add("rate(node_network_receive_bytes_total{device='eth0'}[5m])");
        queries.add("TOTAL_BYTES_NET_TX");
        queries.add("sum by(instance)(rate(node_network_transmit_bytes_total[5m]))");
//        queries.add("rate(node_network_transmit_bytes_total{device='eth0'}[5m])");
       
        queries_containers.add("PERCENT_CPU_UTILIZATION");
        queries_containers.add("sum by(pod_name)(100*((irate(container_cpu_usage_seconds_total{namespace='" + this.namespace + "'}[5m]))))");
        queries_containers.add("MEGABYTES_MEMORY_UTILIZATION");
        queries_containers.add("sum by(pod_name) (((container_memory_usage_bytes{namespace='" + this.namespace + "'})/1024)/1024)");
        queries_containers.add("TOTAL_BYTES_DISK_IN");
        queries_containers.add("sum by(pod_name) ((rate(container_fs_writes_total{namespace='" + this.namespace + "'}[5m]))*1000000)");
        queries_containers.add("TOTAL_BYTES_DISK_OUT");
        queries_containers.add("sum by(pod_name) ((rate(container_fs_reads_total{namespace='" + this.namespace + "'}[5m]))*1000000)");
        queries_containers.add("TOTAL_BYTES_NET_RX");
        queries_containers.add("sum by(pod_name) (rate(container_network_receive_bytes_total{namespace='" + this.namespace + "'}[5m]))");
        queries_containers.add("TOTAL_BYTES_NET_TX");
        queries_containers.add("sum by(pod_name) (rate(container_network_transmit_bytes_total{namespace='" + this.namespace + "'}[5m]))");
     
        for (int i = 0; i < this.metrics.size(); ++i){
            if (this.namespace != null){
              
                for (int j = 0; j < this.queries_containers.size(); ++j){
                    if (metrics.get(i) == null ? queries_containers.get(j) == null : metrics.get(i).equals(queries_containers.get(j))){
                        metricsQuery.add(java.net.URLEncoder.encode(queries_containers.get(j+1), "UTF-8"));
                    
                    }
                }
            }
            else {
                
                for (int j = 0; j < this.queries.size(); ++j){
                    if (metrics.get(i) == null ? queries.get(j) == null : metrics.get(i).equals(queries.get(j))){
                        metricsQuery.add(java.net.URLEncoder.encode(queries.get(j+1), "UTF-8"));
                    
                    }
                }
                
            }
        }

        for (int h = 0; h < metricsQuery.size(); ++h){
            LOGGER.info("MetricsQuery: " + metricsQuery.get(h));
        }        
        
    } 
 
    public List<String> getInstance() throws IOException, JSONException {
       
        List<String> instance = new ArrayList<>();
        String requestURL = this.URLAddress + metricsQuery.get(0);
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONObject data = jsobj.getJSONObject("data");
        JSONArray  jsresult = data.getJSONArray("result");
        JSONObject jsmetric;
        
        for (int i = 0; i < jsresult.length(); ++i) {   
            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
            instance.add(i,jsmetric.getString("instance"));
        }
        LOGGER.info("Instance " + instance);
        return instance;
    }
   
    public List<String> getPod_name() throws IOException, JSONException {
       
        List<String> instance = new ArrayList<>();
        String requestURL = this.URLAddress + metricsQuery.get(0);           
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONObject data = jsobj.getJSONObject("data");
        JSONArray  jsresult = data.getJSONArray("result");
        JSONObject jsmetric;
        LOGGER.info("Tamanho " + jsresult.length());
        for (int i = 0; i < jsresult.length(); i++) {  
            LOGGER.info("Valor de I " + i);
            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
            instance.add(i,jsmetric.getString("pod_name"));
        }
        LOGGER.info("Pod_Name " + instance);
        return instance;
    }
 
    public String getHost(String instance) throws IOException, JSONException {
       
        String requestURL = this.URLAddress + "node_uname_info";
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONObject data = jsobj.getJSONObject("data");
        JSONArray  jsresult = data.getJSONArray("result");
        JSONObject jsmetric;
        
        for (int i = 0; i < jsresult.length(); ++i) {   
            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
            
            if (instance.equals(jsmetric.getString("instance"))){
                LOGGER.info("Host " + jsmetric.getString("nodename"));
                return jsmetric.getString("nodename");
            }
        }
        LOGGER.info("Host Not Found");
        return null;
    }

//    public List<String> getMetrics(String query) throws IOException, JSONException {
//        
//        List<String> metricValue = new ArrayList<>();
//        String requestURL = this.URLAddress + query;           
//        JSONObject jsobj = dataRequest.json(requestURL).toObject();
//        JSONObject data = jsobj.getJSONObject("data");
//        JSONArray  jsresult = data.getJSONArray("result");
//        JSONObject jsmetric;
//        JSONArray jsvalue;
//        
//        for (int i = 0; i < jsresult.length(); ++i) {   
//            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
//            jsvalue = jsresult.getJSONObject(i).getJSONArray("value");
//            metricValue.add(i,jsvalue.getString(1));
//            
//        }
//        LOGGER.info("Metric " + metricValue); 
//        return metricValue;
//    }
    

    public String getMetrics(String query, String instance) throws IOException, JSONException {
        
        String requestURL = this.URLAddress + query;           
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONObject data = jsobj.getJSONObject("data");
        JSONArray  jsresult = data.getJSONArray("result");
        JSONObject jsmetric;
        JSONArray jsvalue;
        
        for (int i = 0; i < jsresult.length(); i++) {   
            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
            
            if (instance.equals(jsmetric.getString("instance"))){
            jsvalue = jsresult.getJSONObject(i).getJSONArray("value");
            LOGGER.info("Metric " + jsvalue.getString(1));
            return jsvalue.getString(1);
            }
        }
        LOGGER.info("Metric Not Found");
        return null;
    }
        
    public String getMetricsContainer(String query, String pod_name) throws IOException, JSONException {
        
        String requestURL = this.URLAddress + query;           
        JSONObject jsobj = dataRequest.json(requestURL).toObject();
        JSONObject data = jsobj.getJSONObject("data");
        JSONArray  jsresult = data.getJSONArray("result");
        JSONObject jsmetric;
        JSONArray jsvalue;
        
        for (int i = 0; i < jsresult.length(); i++) {   
            jsmetric = jsresult.getJSONObject(i).getJSONObject("metric");
            LOGGER.info("Valor de I " + i);
            LOGGER.info("Nome passado " + pod_name);
            LOGGER.info("Nome da busca " + jsmetric.getString("pod_name"));
            
            if (pod_name.equals(jsmetric.getString("pod_name"))){
            jsvalue = jsresult.getJSONObject(i).getJSONArray("value");
            LOGGER.info("Metric " + jsvalue.getString(1));
            return jsvalue.getString(1);
            }
        }
        LOGGER.info("Metric Not Found");
        return null;
    }
   
    @Override
    public ProbeMeasurement collect(){
        
        Table metricsTable = new DefaultTable();
        metricsTable.defineTable(controllerTable);
                
        try {   
            
            QueryConvertion();
            
                if (this.namespace != null){
                    List<String> pod_names = new ArrayList<>();
                    pod_names = getPod_name();
                    
                    for (int i=0; i < pod_names.size(); i++){
                        
                        DefaultTableRow defaultTableRow = new DefaultTableRow();
                        
                        for (int j=0; j < metricsQuery.size(); j++) {
                            defaultTableRow.add(new DefaultTableValue(Float.parseFloat(getMetricsContainer(metricsQuery.get(j), pod_names.get(i)))));
                        }
                        
                        defaultTableRow.add(new DefaultTableValue(this.sliceId));
                        
                        defaultTableRow.add(new DefaultTableValue(this.slicePartId));
                        
                        defaultTableRow.add(new DefaultTableValue(this.namespace + "/" + pod_names.get(i)));
                        
                        defaultTableRow.add(new DefaultTableValue("Container"));
                        
                        metricsTable.addRow(defaultTableRow);
                        
                    }    
                } else {
                    
                    List<String> instances = new ArrayList<>();
                    instances = getInstance();
                    
                    for (int i=0; i < instances.size(); i++){
                      
                        DefaultTableRow defaultTableRow = new DefaultTableRow();
                        
                        for (int j=0; j < metricsQuery.size(); j++) {
                            defaultTableRow.add(new DefaultTableValue(Float.parseFloat(getMetrics(metricsQuery.get(j), instances.get(i)))));
                        }
                        
                        defaultTableRow.add(new DefaultTableValue(this.sliceId));
                        
                        defaultTableRow.add(new DefaultTableValue(this.slicePartId));
                        
                        defaultTableRow.add(new DefaultTableValue(getHost(instances.get(i))));
                                                                       
                        defaultTableRow.add(new DefaultTableValue("Host"));
                        
                        metricsTable.addRow(defaultTableRow);  
                    }
                }    
               
                ArrayList<ProbeValue> list = new ArrayList<>(1);
                
                list.add(new DefaultProbeValue(0, metricsTable));
                
                ProbeMeasurement m = new ProducerMeasurement(this, list, "Table");
                LOGGER.info("TABLE M: " + m); 
                
                return m;
                    
                } catch (IOException | JSONException | TableException | TypeException ex) {
                    Logger.getLogger(PrometheusProbe.class.getName()).log(Level.SEVERE, null, ex);
                }
        
        return null;
    }            
}               