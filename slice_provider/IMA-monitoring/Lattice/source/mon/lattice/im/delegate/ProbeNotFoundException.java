/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.delegate;

/**
 *
 * @author uceeftu
 */
public class ProbeNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>ProbeIDNotFoundException</code> without
     * detail message.
     */
    public ProbeNotFoundException() {
    }

    /**
     * Constructs an instance of <code>ProbeIDNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ProbeNotFoundException(String msg) {
        super(msg);
    }
}
