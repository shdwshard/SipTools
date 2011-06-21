/*
 * Copyright 2009 Charles Chappell.
 *
 * This file is part of IcedJava.
 *
 * IcedJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * IcedJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with IcedJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.mc_cubed.icedjava.packet.attribute;

/**
 * A generic interface representing a STUN attribute
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface Attribute {

    /**
     * Get the length of the attribute in bytes
     * 
     * @return the length of the attriute
     */
    int getLength();

    /**
     * Get the type of the attribute
     * 
     * @return an AttributeType enum representing the STUN attribute type
     * @see AttributeType
     */
    AttributeType getType();

    /**
     * Get the underlying byte data of this attribute.
     * 
     * @return raw byte data of this attribute
     */
    byte[] getData();

    /**
     * Write this attribute's data into the supplied byte buffer starting at
     * the supplied offset
     * 
     * @param data Target Byte Buffer
     * @param off Offset to write attribute bytes to
     * @return the number of bytes written (always the same as .getLength())
     */
    int write(byte[] data, int off);
}
