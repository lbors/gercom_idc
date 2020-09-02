/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.appl.probes.openstack;

import java.io.UnsupportedEncodingException;
import us.monoid.web.Content;

/**
 *
 * @author uceeftu
 */


public class JSONFormContent extends Content {
	protected String rawQuery;

	public JSONFormContent(String query) {
		super("application/json", getBytes(query)); // strictly speaking US ASCII should be used
	}
	
	private static byte[] getBytes(String query) {
		try {
			return query.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new byte[0]; // should never happen
	}


	@Override
	public String toString() {
		return rawQuery;
	}
}
