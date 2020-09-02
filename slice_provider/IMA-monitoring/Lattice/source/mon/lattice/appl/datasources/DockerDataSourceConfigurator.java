/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.datasources;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 *
 * @author uceeftu
 * This Class define the logic to configure the planes when running a DS within
 * a container in order to overcome NAT issues
 */
public class DockerDataSourceConfigurator {
    String containerId;
    
    String dockerHost;
    int dockerPort;
    
    String dockerURI;
    
    Resty rest;
    
    JSONObject containerInfo;
    
    Set<String> udpPorts = new HashSet<>();
    Set<String> tcpPorts = new HashSet<>();

    int controlPort;
    int controlForwardedPort;
    int infoUDPPort;
    int infoTCPPort;
    
    
    private Logger LOGGER = LoggerFactory.getLogger(DockerDataSourceConfigurator.class);
    
    public DockerDataSourceConfigurator(String dockerHost, int dockerPort, String containerId) throws IOException {
        this.containerId = containerId;
        this.dockerHost = dockerHost;
        this.dockerPort = dockerPort;
        initialize();
    }
    
    private void initialize() throws IOException {
        this.dockerURI = "http://" + InetAddress.getByName(dockerHost).getHostAddress() + ":" + String.valueOf(dockerPort) + "/containers/" + containerId + "/json" ;
       
        this.rest = new Resty();
        
        getInfo();
        parsePorts();
    }
    
    
    private void getInfo() throws IOException {
        try {
            JSONObject allInfo = rest.json(dockerURI).toObject();
            containerInfo = allInfo.getJSONObject("HostConfig").getJSONObject("PortBindings");
            containerInfo.toString(3);
        } catch (JSONException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void parsePorts() throws IOException {
        
        Iterator<String> mappings = containerInfo.keys();
        String rawPort;
        String port;
        String protocol;
        
        while (mappings.hasNext()) {
            rawPort = mappings.next();
            port = rawPort.split("/")[0];
            protocol = rawPort.split("/")[1];
            if (protocol.equals("udp"))
                udpPorts.add(port);
            else
                if (!port.equals("22"))
                    tcpPorts.add(port);
        }
        
        this.setControlPorts();
        this.setTCPInfoPort();
        this.setUDPInfoPort();
    }
    
    
    private void setControlPorts() throws IOException {
        String port;
        String mappedPort = null;
        
        do {
            port = this.udpPorts.iterator().next();
            try {
                mappedPort = containerInfo.getJSONArray(port + "/udp").getJSONObject(0).getString("HostPort");
            } catch (JSONException e) {
                throw new IOException("Error while getting ports mapping for" + port + "/udp: " + e.getMessage());
            }
        } while (port.equals(mappedPort)); // we do not use port == mappedPort as they are intended to be used by the DHT
        udpPorts.remove(port);
        controlPort = Integer.valueOf(port);
        controlForwardedPort = Integer.valueOf(mappedPort);
    }
    
    
    private void setUDPInfoPort() throws IOException {
        String port;
        String mappedPort = null;
        
        do {
            port = this.udpPorts.iterator().next();
            try {
                mappedPort = containerInfo.getJSONArray(port + "/udp").getJSONObject(0).getString("HostPort");
            } catch (JSONException e) {
                throw new IOException("Error while getting ports mapping for" + port + "/udp: " + e.getMessage());
            }
        } while (!port.equals(mappedPort));
        udpPorts.remove(port);
        infoUDPPort = Integer.valueOf(mappedPort);
    }
     
    
    private void setTCPInfoPort() throws IOException {
        String port;
        String mappedPort = null;
        
        do {
            port = this.tcpPorts.iterator().next();
            try {
                mappedPort = containerInfo.getJSONArray(port + "/tcp").getJSONObject(0).getString("HostPort");
            } catch (JSONException e) {
                throw new IOException("Error while getting ports mapping for" + port + "/tcp: " + e.getMessage());
            }
        } while (!port.equals(mappedPort));
        tcpPorts.remove(port);
        infoTCPPort = Integer.valueOf(mappedPort);
    }
    
    
    
    public String getDockerHost() {
        return dockerHost;
    }

    public int getControlPort() {
        return controlPort;
    }

    public int getControlForwardedPort() {
        return controlForwardedPort;
    }

    public int getInfoUDPPort() {
        return infoUDPPort;
    }

    public int getInfoTCPPort() {
        return infoTCPPort;
    }
    
    
    
    
}
