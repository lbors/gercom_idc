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
public class ControllerAgentNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>DSNotFoundException</code> without detail
     * message.
     */
    public ControllerAgentNotFoundException() {
    }

    /**
     * Constructs an instance of <code>DSNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ControllerAgentNotFoundException(String msg) {
        super(msg);
    }
}
