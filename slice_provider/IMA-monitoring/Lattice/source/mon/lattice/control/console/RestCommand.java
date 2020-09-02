/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import mon.lattice.control.console.Command;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * A Command object processes a command handled by the ManagementConsole
 * of a ComponentController.
 */
public interface RestCommand extends Command {
    /**
     * Evaluate the Command.
     * Returns false if there is a problem responding down the channel
     */
    public boolean evaluate(Request request, Response response);
}
