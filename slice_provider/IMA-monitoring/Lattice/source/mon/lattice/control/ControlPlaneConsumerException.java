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
public class ControlPlaneConsumerException extends Exception {

    /**
     * Creates a new instance of <code>ControlPlaneException</code> without
     * detail message.
     */
    public ControlPlaneConsumerException() {
    }

    /**
     * Constructs an instance of <code>ControlPlaneException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ControlPlaneConsumerException(String msg) {
        super(msg);
    }
    
    public ControlPlaneConsumerException(Throwable ex) {
        super(ex);
    }
    
    public ControlPlaneConsumerException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
