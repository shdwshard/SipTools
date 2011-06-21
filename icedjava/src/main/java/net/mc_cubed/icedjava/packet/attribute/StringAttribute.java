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
 * This class represents a generic String based Attribute type.
 *
 * @author Charles Chappell
 * @since 0.9
 * @see UsernameAttribute
 * @see RealmAttribute
 * @see SoftwareAttribute
 * @see NonceAttribute
 */
class StringAttribute extends GenericAttribute implements UsernameAttribute,
        RealmAttribute, SoftwareAttribute, NonceAttribute {

    final String value;

    protected StringAttribute(AttributeType type,String value) {
        this.type = type;
        length = value.length();
        data = new byte[length];
        this.value = value;
        System.arraycopy(value.getBytes(), 0, data, 0, length);
    }

    protected StringAttribute(AttributeType type, int length, byte[] value) {
        super(type, length, value);
        this.value = new String(data, 0, length);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[type=" + type + ":value=" + value + "]";
    }
}
