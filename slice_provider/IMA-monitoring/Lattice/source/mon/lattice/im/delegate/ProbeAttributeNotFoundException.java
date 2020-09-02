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
public class ProbeAttributeNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>ProbeIDNotFoundException</code> without
     * detail message.
     */
    public ProbeAttributeNotFoundException() {
    }

    /**
     * Constructs an instance of <code>ProbeIDNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ProbeAttributeNotFoundException(String msg) {
        super(msg);
    }
}
