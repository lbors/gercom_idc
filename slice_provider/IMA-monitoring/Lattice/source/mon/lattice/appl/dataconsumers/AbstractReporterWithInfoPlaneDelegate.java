/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.dataconsumers;

import mon.lattice.core.ControllableReporter;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.im.delegate.ReporterInformationManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractReporterWithInfoPlaneDelegate implements ControllableReporter {
    /**
    * The Reporter ID
    */
    ID myId;
    
    /**
    * The Data Consumer ID the reporter is bound to
    */
    ID dcId;
        
    protected ReporterInformationManager reporterInfoPlaneDelegate;
    
    
    public AbstractReporterWithInfoPlaneDelegate(String name) {
        myId = ID.generate();
        LoggerFactory.getLogger(AbstractReporterWithInfoPlaneDelegate.class).debug("Reporter ID: " + myId);
        this.name = name;
    }
    
    public void setInfoPlaneDelegate(ReporterInformationManager infoManager) {
        reporterInfoPlaneDelegate = infoManager;
    }
    
    public ReporterInformationManager setInfoPlaneDelegate() {
        return reporterInfoPlaneDelegate;
    }
    
    @Override
    public ID getId() {
        return myId;
    }

    @Override
    public void setId(ID id) {
        this.myId = id;
    }
    String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
  
    
    @Override
    public ID getDcId() {
        return dcId;
    }

    @Override
    public void setDcId(ID dcId) {
        this.dcId = dcId;
    }
    
    
    @Override
    public abstract void report(Measurement m);
    
}
