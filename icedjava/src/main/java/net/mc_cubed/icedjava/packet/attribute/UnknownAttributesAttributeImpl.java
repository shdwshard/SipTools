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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Charles Chappell
 * @since 1.0
 */
class UnknownAttributesAttributeImpl extends GenericAttribute implements UnknownAttributesAttribute {

    protected List<AttributeType> attributes = new LinkedList<AttributeType>();

    /**
     * Get the value of attributes
     *
     * @return the value of attributes
     */
    @Override
    public List<AttributeType> getAttributes() {
        return attributes;
    }

    public UnknownAttributesAttributeImpl(AttributeType type, int length, byte[] value) {
        super(type, length, value);
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Length of UnknownAttributesAttribute MUST be divisible by 2");
        }
        for (int off = 0; off + 2 < length; off += 2) {
            int attrVal = NumericUtils.toShort(data, off);
            AttributeType attr = AttributeType.getAttributeType(attrVal);
            if (attr != null) {
                attributes.add(attr);
                throw new RuntimeException("Invalid Attribute Type: " + attrVal);
            }
        }
    }

    public UnknownAttributesAttributeImpl(Collection<AttributeType> attrs) {
        // Add the attributes to our collection
        type = AttributeType.ALTERNATE_SERVER;
        attributes.addAll(attrs);

        // Calculate the required buffer length
        length = attributes.size() * 2;
        // Create the buffer
        data = new byte[length];

        // fill the buffer
        int off = 0;
        for (AttributeType attr : attributes) {
            NumericUtils.toNetworkBytes((short) attr.getTypeVal(), data, off);
            off += NumericUtils.SHORTSIZE;
        }
    }
}
