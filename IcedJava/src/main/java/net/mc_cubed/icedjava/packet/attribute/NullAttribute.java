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
 * NullAttribute is a special case for attributes which are unintelligible to
 * IcedJava. The raw attribute type and bytes are available, but otherwise
 * IcedJava makes no attempt to parse this attribute's information.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class NullAttribute extends GenericAttribute {

    private int rawAttributeType;
    
    public NullAttribute(AttributeType type) {
        this.type = type;
        this.length = 0;
        this.data = null;
    }

    public NullAttribute(AttributeType type, int length, byte[] value) {
        super(type, length, value);
    }

    /**
     * @return the rawAttributeType
     */
    public int getRawAttributeType() {
        return rawAttributeType;
    }

    /**
     * @param rawAttributeType the rawAttributeType to set
     */
    public void setRawAttributeType(int rawAttributeType) {
        this.rawAttributeType = rawAttributeType;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[type=" + type + "]";
    }



}
