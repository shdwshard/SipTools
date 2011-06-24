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
 * IntegerAttribute is a base class for integer holding Attribute types
 *
 * @author Charles Chappell
 * @see PriorityAttribute
 */
class IntegerAttribute extends GenericAttribute implements PriorityAttribute {

    int number;

    public IntegerAttribute(AttributeType type, int number) {
        this.type = type;
        this.number = number;
        this.length = 4;
        this.data = NumericUtils.toNetworkBytes(number);
    }

    public IntegerAttribute(AttributeType type, int length, byte[] value) {
        super(type, length, value);
        NumericUtils.toInt(data);
    }

    @Override
    public int getNumber() {
        return number;
    }
}
