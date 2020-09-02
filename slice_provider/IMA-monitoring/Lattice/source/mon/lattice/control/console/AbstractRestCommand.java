package mon.lattice.control.console;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import cc.clayman.console.ManagementConsole;
import mon.lattice.control.ControlInterface;
import us.monoid.json.JSONObject;

/**
 * A Command object processes a command handled by the ManagementConsole
 * of a ComponentController.
 */
public abstract class AbstractRestCommand implements RestCommand {
    // The name of the command
    String name;
    
    // The ManagementConsole
    ManagementConsole managementConsole;

    // The Lattice controller
    ControlInterface<JSONObject> controller = null;
    

    /**
     * Construct a RestCommand given a name
     */
    protected AbstractRestCommand(String name) {
        this.name = name;
    }

    /**
     * Evaluate the Command.
     * Returns false if there is a problem responding down the channel
     */
    @Override
	public abstract boolean evaluate(Request request, Response response);

    /**
     * Get the name of command as a string.
     */
    @Override
	public String getName() {
        return name;
    }

    /**
     * Set the name
     */
    protected void setName(String n) {
        name = n;
    }
    
    
    /**
     * Get the ManagementConsole this is a command for.
     */
    @Override
	public ManagementConsole getManagementConsole() {
        return managementConsole;
    }

    /**
     * Set the ManagementConsole this is a command for.
     */
    @Override
	public void setManagementConsole(ManagementConsole mc) {
        managementConsole = mc;
        controller = (ControlInterface<JSONObject>)managementConsole.getAssociated();
    }
    
    
    /**
     * Hash code
     */
    @Override
	public int hashCode() {
        return name.hashCode();
    }

    /**
     * Create the String to print out before a message
     */
    protected String leadin() {
        final String MC = "MC: ";
        ManagementConsole mc = getManagementConsole();
        ControlInterface<JSONObject> controller = (ControlInterface<JSONObject>)mc.getAssociated();

        if (controller == null) {
            return MC;
        } else {
            //return controller.getName() + " " + MC;
            return "Lattice Controller";
        }

    }

}