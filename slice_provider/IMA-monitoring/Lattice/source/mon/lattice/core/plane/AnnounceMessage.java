/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import mon.lattice.core.EntityType;
import mon.lattice.core.ID;



public class AnnounceMessage extends AbstractAnnounceMessage {
    public AnnounceMessage(ID id, EntityType e) {
        super(id, e, MessageType.ANNOUNCE);
    }
}