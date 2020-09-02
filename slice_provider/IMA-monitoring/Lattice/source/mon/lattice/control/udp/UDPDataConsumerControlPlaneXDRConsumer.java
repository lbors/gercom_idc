/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.udp;

import mon.lattice.control.ControlServiceException;
import mon.lattice.control.ReporterLoader;
import mon.lattice.control.ReporterLoaderException;
import mon.lattice.core.DataConsumerInteracter;
import mon.lattice.core.ID;
import mon.lattice.core.plane.AbstractAnnounceMessage;
import mon.lattice.core.plane.AnnounceMessage;
import mon.lattice.core.plane.DataConsumerControlPlane;
import mon.lattice.core.plane.DeannounceMessage;
import java.io.IOException;
import java.net.InetSocketAddress;
import mon.lattice.core.ControllableDataConsumer;
import mon.lattice.core.EntityType;
import mon.lattice.core.Rational;



public class UDPDataConsumerControlPlaneXDRConsumer extends AbstractUDPControlPlaneXDRConsumer implements DataConsumerControlPlane, DataConsumerInteracter {
    ControllableDataConsumer dataConsumer;
    
    public UDPDataConsumerControlPlaneXDRConsumer(InetSocketAddress localAddress) {
        super(localAddress);
    }
    
    public UDPDataConsumerControlPlaneXDRConsumer(InetSocketAddress localAddress, InetSocketAddress controllerAddress) {
        super(localAddress, controllerAddress);
    }


    @Override
    public boolean announce() {
        LOGGER.debug("Invoking announce for Data Consumer" + dataConsumer.getID());
        AbstractAnnounceMessage message = new AnnounceMessage(dataConsumer.getID(), EntityType.DATACONSUMER);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while announcing Data Consumer" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean dennounce() {
        LOGGER.debug("Invoking deannounce for Data Consumer" + dataConsumer.getID());
        AbstractAnnounceMessage message = new DeannounceMessage(dataConsumer.getID(), EntityType.DATACONSUMER);
        
        try {
            announceSerializer(message);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while deannouncing Data Consumer" + e.getMessage());
            return false;
        }
    }
    
    

    @Override
    public ControllableDataConsumer getDataConsumer() {
        return this.dataConsumer;
    }

    @Override
    public ControllableDataConsumer setDataConsumer(ControllableDataConsumer dc) {
        this.dataConsumer = dc;
        return this.dataConsumer;
    }  
    
    @Override
    public Rational getDCMeasurementsRate(ID dcID) {
        LOGGER.info("** invoking getDCMeasurementsRate **");
        return dataConsumer.getMeasurementsRate();
    }
    
    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws ControlServiceException {
        try {
            LOGGER.info("** invoking loadReporter **");            
            ReporterLoader r = new ReporterLoader(reporterClassName, reporterArgs);
            dataConsumer.addReporter(r.getReporter());
            return r.getReporter().getId();
        } catch (ReporterLoaderException e) {
            throw new ControlServiceException(e);
        }
    }

    @Override
    public boolean unloadReporter(ID reporterID) throws ControlServiceException {
        LOGGER.info("** invoking unloadReporter **");
        dataConsumer.removeReporter(dataConsumer.getReporterById(reporterID));
        return true;
    }
}
