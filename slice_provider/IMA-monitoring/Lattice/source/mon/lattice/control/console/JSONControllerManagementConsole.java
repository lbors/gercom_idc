/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import mon.lattice.control.ControlInterface;
import eu.fivegex.monitoring.control.mapping.MappingsRestHandler;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public final class JSONControllerManagementConsole extends RestConsole{
    public JSONControllerManagementConsole(ControlInterface<JSONObject> controller, int port) {

        setAssociated(controller);
        initialise(port);
    }

    @Override
    public void registerCommands() {
        // /probe/uuid/?<args>
        defineRequestHandler("/probe/.*", new ProbeRestHandler());
        
        // /datasource/uuid/probe/?<args> and /datasource/name/probe/?<args>
        defineRequestHandler("/datasource/.*", new DataSourceRestHandler());
        
        // /dataconsumer/uuid/?<args> 
        defineRequestHandler("/dataconsumer/.*", new DataConsumerRestHandler());
        
        
        defineRequestHandler("/controlleragent/.*", new ControllerAgentRestHandler());
        
        // /dataconsumer/uuid/?<args> 
        defineRequestHandler("/reporter/.*", new ReporterRestHandler());
        
        // /mappings/serviceid>
        defineRequestHandler("/mappings/.*", new MappingsRestHandler());
        
        register(new UnknownCommand());
       }
    
}
