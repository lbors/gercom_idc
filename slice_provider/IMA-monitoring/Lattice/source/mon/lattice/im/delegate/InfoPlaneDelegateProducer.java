/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

import mon.lattice.control.deployment.ControllerAgentInfo;
import mon.lattice.control.deployment.DataConsumerInfo;
import mon.lattice.control.deployment.DataSourceInfo;
import mon.lattice.control.deployment.ResourceEntityInfo;

/**
 *
 * @author uceeftu
 */
public interface InfoPlaneDelegateProducer {
    void addDataSource(DataSourceInfo dataSource, ResourceEntityInfo resource, int timeout) throws InterruptedException, DSNotFoundException;

    void addDataConsumer(DataConsumerInfo dataConsumer, ResourceEntityInfo resource, int timeout) throws InterruptedException, DCNotFoundException;
    
    void addControllerAgent(ControllerAgentInfo controllerAgent, ResourceEntityInfo resource, int timeout) throws InterruptedException, ControllerAgentNotFoundException; 
}
