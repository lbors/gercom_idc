/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.im.dht;

import java.io.Serializable;

/**
 *
 * @author uceeftu
 */
public interface DHTInteractor {

    /**
     * Lookup info directly from the DHT.
     * @return the value if found, null otherwise
     */
    Object getDHT(String aKey);

    /**
     * Put stuff into DHT.
     */
    boolean putDHT(String aKey, Serializable aValue);

    /**
     * Remove info from the DHT.
     * @return boolean
     */
    boolean remDHT(String aKey);
    
}
