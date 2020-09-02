/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control;

import mon.lattice.core.ControllableReporter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public final class ReporterLoader implements Serializable { // to be refactored using an Interface
    
    String reporterClassName;
    
    Class<?> clazz;
    Class<? extends ControllableReporter> reporterClazz;
    Constructor<? extends ControllableReporter> cons0;
    Object [] constructorParameters;
    
    ControllableReporter reporter;
    
    
    
    public ReporterLoader(String reporterClassName, Object ... params) throws ReporterLoaderException {
        this.reporterClassName = reporterClassName;
        constructorParameters = params;
        
        initReporter();
    }
    
    
    public void initReporter() throws ReporterLoaderException {
        try {
            LoggerFactory.getLogger(ReporterLoader.class).info("Loading Class: " + reporterClassName);
            clazz = Class.forName(reporterClassName);
            // check if the class implements the right interface
            reporterClazz = clazz.asSubclass(ControllableReporter.class);
            
            if (constructorParameters == null) {
                cons0 = reporterClazz.getConstructor();
                reporter = cons0.newInstance();
            }
            
            else {
                // we build an array with the Class types of the provided Parameters
                Class [] paramsTypes = new Class[constructorParameters.length];

                for (int i=0; i<constructorParameters.length; i++)
                    paramsTypes[i]=constructorParameters[i].getClass();

                cons0 = reporterClazz.getConstructor(paramsTypes);

                // create an instance of the Reporter
                reporter = cons0.newInstance(constructorParameters);
            }
            
            }  catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException ex) {
                    throw new ReporterLoaderException(ex);
            }  catch (InvocationTargetException itex) {
                    throw new ReporterLoaderException(itex.getCause());
            }
    }

    public ControllableReporter getReporter() {
        return reporter;
    }    
    
    
}
