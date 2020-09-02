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
public class ControlServiceException extends Exception {
    /**
     * Creates a new instance of <code>ControlServiceException</code> without detail
     * message.
     */
    public ControlServiceException() {
    }

    /**
     * Constructs an instance of <code>ControlServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ControlServiceException(String msg) {
        super(msg);
    }
    
    public ControlServiceException(Throwable ex) {
        super(ex);
    }
    
    public ControlServiceException(String msg, Throwable ex) {
        super(msg, ex);
    }
    
}
