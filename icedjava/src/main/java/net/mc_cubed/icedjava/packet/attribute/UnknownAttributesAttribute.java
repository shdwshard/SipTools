/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.packet.attribute;

import java.util.List;

/**
 *
 * @author shadow
 */
public interface UnknownAttributesAttribute extends Attribute {

    /**
     * Get the value of attributes
     *
     * @return the value of attributes
     */
    List<AttributeType> getAttributes();
    
}
