/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core.plane;

import mon.lattice.core.EntityType;
import mon.lattice.core.ID;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractAnnounceMessage implements Serializable {
    
    protected ID entityID;
    protected MessageType messageType; // subclass will set this either to Announce or Deannounce
    protected EntityType entity;

    public AbstractAnnounceMessage(ID id, EntityType entity, MessageType messageType) {
        this.entityID = id;
        this.entity = entity;
        this.messageType = messageType;
    }

    public ID getEntityID() {
        return entityID;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public EntityType getEntity() {
        return entity;
    }
    
    public static String toString(AbstractAnnounceMessage o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
    
    public static AbstractAnnounceMessage fromString(String s) throws IOException ,
                                                       ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return (AbstractAnnounceMessage) o;
   }
    
    
    
}
