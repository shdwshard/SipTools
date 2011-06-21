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

import net.mc_cubed.icedjava.util.NumericUtils;

/**
 * A value holder class for STUN attributes which contain a single long (64-bit)
 * value
 *
 * @author Charles Chappell
 * @since 0.9
 * @see IceControlledAttribute
 * @see IceControllingAttribute
 */
class LongAttribute extends GenericAttribute implements IceControlledAttribute, 
        IceControllingAttribute {

    private final long number;

    public LongAttribute(AttributeType type, long number) {
        this.type = type;
        this.number = number;
        this.length = 8;
        this.data = NumericUtils.toNetworkBytes(number);
    }

    public LongAttribute(AttributeType type, int length, byte[] value) {
        super(type, length, value);
        if (length != NumericUtils.LONGSIZE || data.length != NumericUtils.LONGSIZE) {
            throw new IllegalArgumentException("Longs are " +
                    NumericUtils.LONGSIZE + " bytes long and must be in a field" +
                    " that size.  Attribute " + type + " is NOT long.");
        }
        number = NumericUtils.toLong(data);
    }

    /**
     * @return the number
     */
    public long getNumber() {
        return number;
    }
}
