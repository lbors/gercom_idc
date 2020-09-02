/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im;

import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceEventListener;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractIMNode implements IMBasicNode {
    
    // the remote host
    protected String remoteHost;
    
    @Override
    public String getRemoteHostname() {
        return this.remoteHost;
    }
    
    public abstract void sendMessage(AbstractAnnounceMessage m);
    
    public abstract void addAnnounceEventListener(AnnounceEventListener l);
    
}
