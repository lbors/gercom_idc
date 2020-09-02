/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control;

import java.util.Properties;
import mon.lattice.core.AbstractPlaneInteracter;
import mon.lattice.core.plane.ControllerControlPlane;
import mon.lattice.im.delegate.InfoPlaneDelegate;

/**
 *
 * @author uceeftu
 * @param <ReturnType>
 */
public abstract class AbstractController<ReturnType> extends AbstractPlaneInteracter implements ControlInterface<ReturnType>  {        
    
    protected InfoPlaneDelegate controlInformationManager;    
    protected Properties pr;
  
    abstract public void initPlanes(); 
        
    
    public void setPropertyHandler(Properties pr) {
        this.pr = pr;
    }
    
    
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return controlInformationManager;
    }
    
    
    public ControllerControlPlane getControlHandle() {
        return (ControllerControlPlane)getControlPlane();
    }
}
