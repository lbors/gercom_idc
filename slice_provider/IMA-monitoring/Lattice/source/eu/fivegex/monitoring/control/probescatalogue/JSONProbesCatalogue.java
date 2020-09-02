/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.probescatalogue;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public interface JSONProbesCatalogue {
    JSONObject getProbesCatalogue() throws JSONException; 
    
    void initCatalogue();
}
