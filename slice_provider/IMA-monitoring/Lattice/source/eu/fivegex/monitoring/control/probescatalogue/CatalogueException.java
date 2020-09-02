/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.probescatalogue;

/**
 *
 * @author uceeftu
 */
public class CatalogueException extends Exception {

    /**
     * Creates a new instance of <code>CatalogueException</code> without detail
     * message.
     */
    public CatalogueException() {
    }

    /**
     * Constructs an instance of <code>CatalogueException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CatalogueException(String msg) {
        super(msg);
    }
}
