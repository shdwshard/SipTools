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

import java.net.InetAddress;
import java.security.SecureRandom;
import net.mc_cubed.icedjava.util.NumericUtils;
import java.util.LinkedList;
import java.util.List;
import net.mc_cubed.icedjava.stun.StunAuthenticator;

/**
 *
 * @author Charles Chappell
 */
public class AttributeFactory {

    public static List<Attribute> processIntoList(byte[] packetBytes, int start, int off, int len) {
        List<Attribute> attrList = new LinkedList<Attribute>();
        while (len > 0) {
            Attribute attr = GenericAttribute.process(packetBytes, start, off);
            if (attr != null) {
                // Add to the attribute list
                attrList.add(attr);
                // Move to the next attribute
                off += NumericUtils.makeMultipleOf(4 + attr.getLength(), 4);
                len -= NumericUtils.makeMultipleOf(4 + attr.getLength(), 4);
            } else {
                throw new RuntimeException("Encountered an error processing the attribute list");
            }
        }

        return attrList;
    }

    @Deprecated
    public static List<Attribute> processIntoList(byte[] packetBytes, int start, int off, int len, StunAuthenticator auth) {
        return processIntoList(packetBytes, start, off, len);
    }

    public static MappedAddressAttribute createMappedAddressAttribute(InetAddress address, int port) {
        return new MappedAddressAttributeImpl(address, port);
    }

    public static XORMappedAddressAttribute createXORMappedAddressAttribute(InetAddress address, int port,byte[] transactionId) {
        return new XORMappedAddressAttributeImpl(address, port, transactionId);
    }

    public static UsernameAttribute createUsernameAttribute(String username) {
        return new StringAttribute(AttributeType.USERNAME,username);
    }

    public static RealmAttribute createRealmAttribute(String realm) {
        return new StringAttribute(AttributeType.REALM,realm);
    }

    public static IntegrityAttribute createIntegrityAttribute(String username, String realm, String password) {
        return new IntegrityAttributeImpl(username,realm,password);
    }

    public static FingerprintAttribute createFingerprintAttribute() {
        return new FingerprintAttributeImpl();
    }

    public static SoftwareAttribute createSoftwareAttribute(String softwareIdentifier) {
        return new StringAttribute(AttributeType.SOFTWARE,softwareIdentifier);
    }

    public static NonceAttribute createNonceAttribute(String nonce) {
        if (nonce == null || nonce.length() == 0) {
            return createNonceAttribute();
        } else {
            return new StringAttribute(AttributeType.NONCE,nonce);
        }
    }

    public static NonceAttribute createNonceAttribute() {
        String nonce = "";
        SecureRandom sr = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            Long nextLong = sr.nextLong();
            nonce += Long.toHexString(nextLong);
        }
        return new StringAttribute(AttributeType.NONCE,nonce);
    }

    public static PriorityAttribute createPriorityAttribute(int priority) {
        return new IntegerAttribute(AttributeType.PRIORITY,priority);
    }

    public static Attribute createIceControllingAttribute(long tieBreaker) {
        return new LongAttribute(AttributeType.ICE_CONTROLLING,tieBreaker);
    }

    public static IceControlledAttribute createIceControlledAttribute(long tieBreaker) {
        return new LongAttribute(AttributeType.ICE_CONTROLLED,tieBreaker);
    }

    public static Attribute processOneAttribute(byte[] data, int offset, int attributeOffset) {
        return GenericAttribute.process(data, offset, attributeOffset);
    }


}
