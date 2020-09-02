/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.reporters;

/**
 *
 * @author uceeftu
 */
public class ReporterException extends Exception {

    /**
     * Creates a new instance of <code>ReporterException</code> without detail
     * message.
     */
    public ReporterException() {
    }

    /**
     * Constructs an instance of <code>ReporterException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ReporterException(String msg) {
        super(msg);
    }
    
    public ReporterException(String msg, Throwable t) {
        super(msg, t);
    }
    
    public ReporterException(Throwable t) {
        super(t);
    }
}
