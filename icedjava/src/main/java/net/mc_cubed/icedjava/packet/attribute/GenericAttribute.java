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

import net.mc_cubed.icedjava.stun.StunAuthenticator;
import net.mc_cubed.icedjava.util.NumericUtils;
import net.mc_cubed.icedjava.util.StringUtils;
import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Charles Chappell
 */
public class GenericAttribute implements Attribute {

    static final Class[] CONSTRUCTOR_ARGS = new Class[]{AttributeType.class, int.class, new byte[0].getClass()};
    static final String MD5_ALGORITHM = "MD5";

    public static byte[] computeMD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            return md.digest(string.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GenericAttribute.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    protected byte[] data;

    public static Attribute process(byte[] packetBytes, int start, int offset, StunAuthenticator auth) {
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
            Constructor c = attributeClass.getConstructor(CONSTRUCTOR_ARGS);
            Attribute newAttr = (Attribute) c.newInstance(type, length, data);
            if (newAttr instanceof HashAttribute) {
                byte[] credentials = null;
                if (auth != null) {
                    String username = auth.getUsername();
                    String realm = auth.getRealm();
                    String password = auth.getPassword();
                    credentials = computeMD5(username + ":" + realm + ":" + password);
                }
                HashAttribute hashAttr = (HashAttribute) newAttr;
                if (hashAttr.verifyHash(credentials, packetBytes, start, offset)) {
                    Logger.getAnonymousLogger().finer("Found " + type + " attribute and verified it");
                } else {
                    Logger.getAnonymousLogger().finer("Found " + type + " attribute and verification failed");
                }

            }
            if (newAttr instanceof NullAttribute) {
                NullAttribute nullattr = (NullAttribute) newAttr;
                nullattr.setRawAttributeType(attrVal);
            }
            if (type == AttributeType.USERNAME) {
                if (auth != null) {
                    StringAttribute sattr = (StringAttribute)newAttr;
                    auth.setUsername(sattr.getValue());
                }
            }
            if (type == AttributeType.REALM) {
                if (auth != null) {
                    StringAttribute sattr = (StringAttribute)newAttr;
                    auth.setRealm(sattr.getValue());
                }
            }

            return newAttr;
        } catch (Exception ex) {
            Logger.getLogger(GenericAttribute.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(GenericAttribute.class.getName()).severe(
                    "Error depacketizing Attribute: " +
                    StringUtils.getHexString(packetBytes, offset, length + 4));
        }

        return null;
    }
    protected int length;
    protected AttributeType type;

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

    public final byte[] getData() {
        return data;
    }

    public AttributeType getType() {
        return type;
    }

    final public int write(byte[] data, int off) {
        // If this is a hash attribute, compute the hash now
        if (HashAttribute.class.isInstance(this)) {
            HashAttribute hashAttr = (HashAttribute) this;
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
