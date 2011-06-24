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
import net.mc_cubed.icedjava.util.StringUtils;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Charles Chappell
 */
class GenericAttribute implements Attribute {

    static final Class[] CONSTRUCTOR_ARGS = new Class[]{AttributeType.class, int.class, new byte[0].getClass()};
    static final String MD5_ALGORITHM = "MD5";

    protected byte[] data;

    public static Attribute process(byte[] packetBytes, int start, int offset) {
        int attrVal = NumericUtils.toShort(packetBytes, offset);
        AttributeType type = AttributeType.getAttributeType(attrVal);
        int length = NumericUtils.toShort(packetBytes, offset + 2);


        byte[] data;
        if (length > 0) {
            data = new byte[length];
            System.arraycopy(packetBytes, offset + 4, data, 0, length);
        } else {
            data = null;
        }

        Class attributeClass = type.getImplementationClass();
        // Special case
        try {
            Constructor c = attributeClass.getDeclaredConstructor(CONSTRUCTOR_ARGS);

            Attribute newAttr = (Attribute) c.newInstance(type, length, data);
            if (newAttr instanceof FingerprintAttribute) {
                FingerprintAttribute hashAttr = (FingerprintAttribute) newAttr;
                if (hashAttr.verifyHash(packetBytes, start, offset)) {
                    Logger.getAnonymousLogger().log(Level.FINER,
                            "Found {0} attribute and verified it",
                            type);
                } else {
                    Logger.getAnonymousLogger().log(Level.FINER,
                            "Found {0} attribute and verification failed",
                            type);
                }

            }
            if (newAttr instanceof NullAttribute) {
                NullAttribute nullattr = (NullAttribute) newAttr;
                nullattr.setRawAttributeType(attrVal);
            }

            return newAttr;
        } catch (Exception ex) {
            Logger.getLogger(GenericAttribute.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(GenericAttribute.class.getName()).log(
                    Level.SEVERE, "Error depacketizing Attribute: {0}",
                    StringUtils.getHexString(packetBytes, offset, length + 4));
        }

        return null;
    }
    protected int length;
    protected AttributeType type;

    @Override
    public final int getLength() {
        return length;
    }

    public GenericAttribute(AttributeType type, int length, byte[] value) {
        this.type = type;
        this.length = length;
        this.data = value;
    }

    protected GenericAttribute() {
    }

    @Override
    public final byte[] getData() {
        return data;
    }

    @Override
    public AttributeType getType() {
        return type;
    }

    @Override
    final public int write(byte[] data, int off) {
        // If this is a hash attribute, compute the hash now
        if (FingerprintAttribute.class.isInstance(this)) {
            FingerprintAttribute hashAttr = (FingerprintAttribute) this;
            hashAttr.computeHash(data, 0, off);
        }

        long typeVal = type.getTypeVal();

        // Construct the Attribute Header
        NumericUtils.toNetworkBytes((short) typeVal, data, off);
        NumericUtils.toNetworkBytes((short) length, data, off + 2);

        if (length > 0) {
            if (this.data == null) {
                throw new RuntimeException("Data length cannot be non zero if " +
                        "no data is present packetizing: " + this);
            } else {
                // Copy the data
                System.arraycopy(this.data, 0, data, off + 4, length);
            }
        }

        return length + 4;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[type=" + type + ":length=" + length + ":data=" + StringUtils.getHexString(data) + "]";
    }
}
