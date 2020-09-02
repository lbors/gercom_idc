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
public class ReporterLoaderException extends Exception {
    public ReporterLoaderException(String msg) {
           super(msg);
    }
    
    public ReporterLoaderException(Throwable ex) {
        super(ex);
    }
    
    public ReporterLoaderException(String msg, Throwable ex) {
        super(msg, ex);
    }
    
    
}
