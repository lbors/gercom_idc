/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.core;

import java.util.EnumSet;
import java.util.HashMap;

/**
 *
 * @author uceeftu
 */

public enum EntityType {
        DATASOURCE(1),
        DATACONSUMER(2),
        CONTROLLERAGENT(3);
        
        private static final HashMap<Integer, EntityType> lookup = new HashMap<>();
        private Integer entityValue;
        
        public static final EntityType lookup(Integer mt) {
            return lookup.get(mt);
        }
        
        static {
	for(EntityType t : EnumSet.allOf(EntityType.class)) { 
            lookup.put(t.getValue(), t);
            }
        }
        
        private EntityType(Integer value) {
            entityValue = value;
        }
        
        public Integer getValue() {
            return entityValue;   
        }
        
    }