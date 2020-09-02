/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.docker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 *
 * @author uceeftu
 */

//to be refoctored with an interface

public class DockerDataCollector {
    long containerCpuTime;
    long systemCpuTime;
    int coresNumber;
    long usedMemBytes;
    long maxMemBytes;
    long txBytes;
    long rxBytes;
    
    String containerId;
    
    String dockerHost;
    String dockerURI;
    int dockerPort;
    
    Resty rest;
    
    private Logger LOGGER = LoggerFactory.getLogger(DockerDataCollector.class);
    
    
    public DockerDataCollector(String dockerHost, int dockerPort, String cId) throws UnknownHostException {
        this.dockerHost = dockerHost;
        this.dockerPort = dockerPort;
        this.containerId = cId;
        initialize(InetAddress.getByName(this.dockerHost), this.dockerPort);
    }
    
    private synchronized void initialize(InetAddress addr, int port) {
        this.dockerPort = port;
        this.dockerURI = "http://" + addr.getHostName() + ":" + Integer.toString(port);
        rest = new Resty();
    }
    
    
    void collectValues() throws JSONException, IOException {
        String uri = dockerURI + "/containers/" + containerId + "/stats?stream=0";

        JSONObject jsobj = rest.json(uri).toObject();

        this.containerCpuTime=jsobj.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getLong("total_usage");

        this.systemCpuTime=jsobj.getJSONObject("cpu_stats").getLong("system_cpu_usage");

        this.usedMemBytes=jsobj.getJSONObject("memory_stats").getLong("usage");

        this.maxMemBytes=jsobj.getJSONObject("memory_stats").getLong("limit");

        this.coresNumber=jsobj.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getJSONArray("percpu_usage").length();

        //this.txBytes=jsobj.getJSONObject("networks").getJSONObject("eth0").getLong("tx_bytes");

        //this.rxBytes=jsobj.getJSONObject("networks").getJSONObject("eth0").getLong("rx_bytes");
    }
    
    
    public long getContainerCpuTime() {
       return containerCpuTime;
    }
     
    public long getSystemCpuTime() {
       return systemCpuTime;
    } 

    public int getCoresNumber() {
       return coresNumber;
    }
    
    public long getUsedMemBytes() {
       return usedMemBytes;
    }
    
    public long getMaxMemBytes() {
       return maxMemBytes;
    }
    
    public long getTxBytes() {
       return txBytes;
    }
    
    public long getRxBytes() {
       return rxBytes;
    }
}
