/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import cc.clayman.console.ManagementConsole;

/**
 * A Command object processes a command handled by the ManagementConsole
 * of a ComponentController.
 */
public interface Command {
    /**
     * Get the name of command as a string.
     */
    public String getName();

    /**
     * Get the ManagementConsole this is a command for.
     */
    public ManagementConsole getManagementConsole();

    /**
     * Set the ManagementConsole this is a command for.
     */
    public void setManagementConsole(ManagementConsole mc);

}