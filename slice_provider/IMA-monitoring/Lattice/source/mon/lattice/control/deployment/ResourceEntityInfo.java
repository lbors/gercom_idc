/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.deployment;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 *
 * @author uceeftu
 */
public abstract class ResourceEntityInfo {
    protected InetSocketAddress address;
    
    protected Long jarDeploymentDate;
    protected boolean jarDeployed;
    
    
    
    public ResourceEntityInfo(String address, int port) throws DeploymentException {
        try {
            this.address = new InetSocketAddress(InetAddress.getByName(address).getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new DeploymentException(e.getMessage()); 
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Long getJarDeploymentDate() {
        return jarDeploymentDate;
    }

    public void setJarDeploymentDate(Long jarDeploymentDate) {
        this.jarDeploymentDate = jarDeploymentDate;
    }
    
    
    public boolean isJarDeployed() {
        return jarDeployed;
    }

    public void setJarDeployed() {
        this.jarDeployed = true;
    }
    
    public abstract String getCredentials();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResourceEntityInfo) {
            return this.address.equals(((ResourceEntityInfo) obj).getAddress());   
        }
        else
            return false;     
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.address);
        return hash;
    }
 

    
    
}
