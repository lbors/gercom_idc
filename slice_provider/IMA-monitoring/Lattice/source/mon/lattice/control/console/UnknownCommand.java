/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.console;

import mon.lattice.control.console.AbstractRestCommand;
import java.io.IOException;
import java.io.PrintStream;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * The command to execute if the incoming command is unknown.
 */
public class UnknownCommand extends AbstractRestCommand {
    /**
     * Construct a UnknownCommand
     */
    public UnknownCommand() {
        super("__UNKNOWN__");
    }
    
    
    /**
     * Evaluate the Command.
     */
    @Override
	public boolean evaluate(Request request, Response response) {
        try {
            PrintStream out = response.getPrintStream();

            response.setCode(302);

            JSONObject jsobj = new JSONObject();
            jsobj.put("error", "UnknownCommand");

            out.println(jsobj.toString());
            response.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        } catch (JSONException jex) {
            System.out.println(jex.getMessage());
        }
        return false;
    }
}
