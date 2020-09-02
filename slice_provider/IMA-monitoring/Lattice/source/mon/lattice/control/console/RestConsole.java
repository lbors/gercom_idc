/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import java.util.HashMap;
import org.slf4j.LoggerFactory;


/**
 * A ManagementConsole listens for REST requests
 * in the Controller
 */
public abstract class RestConsole extends AbstractRestConsole {

    // HashMap of command name -> Command
    HashMap<String, Command> commandMap;
    
    /**
     * The no arg Constructor.
     */
    public RestConsole() {
        // setup the Commands
        commandMap = new HashMap<String, Command>();
    }

    /**
     * Construct a ManagementConsole, given a specific port.
     */
    @Override
	public void initialise (int port) {
            super.initialise(port);

            // setup default /command handler
            defineRequestHandler("/command/", new CommandAsRestHandler());

    }

    /**
     * Start the ManagementConsole.
     */
    @Override
	public boolean start() {
        // check the UnknownCommand exists
        Command unknown = commandMap.get("__UNKNOWN__");
        
        
        if (unknown == null) {
            LoggerFactory.getLogger(RestConsole.class).error("Unknown Command is null");
            return false;
        }


        return super.start();
        }
        
        
    /**
     * Register a new command with the ManagementConsole.
     */
    public void register(Command command) {
        String commandName = command.getName();

        command.setManagementConsole(this);

        commandMap.put(commandName, command);
    }

    /**
     * Find a command in the ManagementConsole.
     * @param commandName The name of the command
     */
    public Command find(String commandName) {
        return commandMap.get(commandName);
    }

    /**
     * Find a handler in the ManagementConsole.
     * @param pattern The pattern for the handler
     */
    public Command findHandler(String pattern) {
        return commandMap.get(pattern);
    }

}