/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import cc.clayman.console.ManagementConsole;
import cc.clayman.console.RequestHandler;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.PrintStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.*;

/**
 * A Console listens for REST requests
 * for doing component management.
 */
public abstract class AbstractRestConsole implements Container, ManagementConsole {

    private static Logger LOGGER = LoggerFactory.getLogger(RestConsole.class);
    // the port this element is listening on
    protected int port;

    // HashMap of command name -> Request
    HashMap<String, RequestHandler> handlerMap;

    // The handler of all the actual requests
    ContainerServer server;

    // The connection and socket
    Connection connection;
    SocketAddress address;

    // The associated object
    Object associated;

    /**
     * The no arg Constructor.
     */
    public AbstractRestConsole() {
        associated = null;
    }


    /**
     * Construct a Console, given a specific port.
     */
    public void initialise (int port) {
        this.port = port;

        // setp the handlers
        handlerMap = new HashMap<String, RequestHandler>();

        registerCommands();

    }

    /**
     * Start the ManagementConsole.
     */
    public boolean start() {
        // initialise the socket
        try {
            server = new ContainerServer(this);
            connection = new SocketConnection(server);
            address = new InetSocketAddress(port);

            connection.connect(address);

            LOGGER.info("Listening on port " + port);
            return true;

        } catch (IOException ioe) {
            LOGGER.error("Cannot listen on port " + port);
            return false;
        }

    }


    /**
     * Stop the ManagementConsole.
     */
    public boolean stop() {
        try {
            server.stop();
            connection.close();
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }


    /**
     * This is the main handler method for a Container.
     * It takes the requests and delegates them to
     * define RequestHandler objects, based on a pattern.
     */
    public void handle(Request request, Response response) {
        try {
            
            LOGGER.debug("method: " + request.getMethod());
            LOGGER.debug("path: " + request.getPath());
            LOGGER.debug("query: " + request.getQuery());
            LOGGER.debug("target: " + request.getTarget());
            LOGGER.debug("directory: " + request.getPath().getDirectory());

            String path =  request.getPath().getPath();
            String directory = request.getPath().getDirectory();

            // skip through all patterns 
            // and try and find a RequestHandler for it

            Set<String> patterns = handlerMap.keySet();

            for (String pattern : patterns) {
                
                if (directory.equals(pattern) || path.matches(pattern)) {
                    RequestHandler handler = (RequestHandler)handlerMap.get(pattern);

                    handler.handle(request, response);

                    return;
                }
            }


            // if we fall through
            {
                LOGGER.error("AbstractRestConsole error: " + "no command");

                LOGGER.error("method: " + request.getMethod() + 
                             " path: " + request.getPath() +
                             " query: " + request.getQuery() +
                             " target: " + request.getTarget() +
                             " directory: " + request.getPath().getDirectory());

                //fetch the UnknownCommand
                PrintStream out = response.getPrintStream();
                response.setCode(404);
                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "UnknownResource");
                out.println(jsobj.toString());
                response.close();
            }
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage());
        } catch (JSONException jex) {
            LOGGER.error(jex.getMessage());
        }
    }

    /**
     * Define a handler for a request
     */
    public void defineRequestHandler(String pattern, RequestHandler rh) {
        // set up the RequestHandler
        // point to ManagementConsole
        rh.setManagementConsole(this);
        rh.setPattern(pattern);

        // put in map
        handlerMap.put(pattern, rh);
    }

    /**
     * Get an associated object.
     * Possibly a controller or a container.
     */
    public Object getAssociated() {
        return associated;
    }

    /**
     * Set an associated object.
     * Possibly a controller or a container.
     */
    public void setAssociated(Object obj) {
        associated = obj;
    }
        


    protected String leadin() {
        return "RestConsole: ";
    }

}