/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control;

/**
 *
 * @author uceeftu
 */
public interface ControlInterface<ReturnType> {
    ReturnType getDataSourceInfo(String dsID) throws Exception;
    
    ReturnType loadProbe(String id, String probeClassName, String probeArgs) throws Exception;
    
    ReturnType unloadProbe(String id) throws Exception;
    
    ReturnType turnOffProbe(String id) throws Exception;
    
    ReturnType turnOnProbe(String id) throws Exception;
     
    ReturnType setProbeServiceID(String probeID, String serviceID) throws Exception;
    
    ReturnType setProbeGroupID(String probeID, String groupID) throws Exception;
    
    ReturnType setProbeDataRate(String probeID, String dataRate) throws Exception;
    
    ReturnType getProbeDataRate(String probeID) throws Exception;
    
    ReturnType getProbeServiceID(String probeID) throws Exception;
    
    ReturnType getDataConsumerMeasurementRate(String dcID) throws Exception;
    
    ReturnType loadReporter(String id, String reporterClassName, String reporterArgs) throws Exception;
    
    ReturnType unloadReporter(String id) throws Exception;
    
    ReturnType getDataSources() throws Exception;
    
    ReturnType getDataConsumers() throws Exception;   
}