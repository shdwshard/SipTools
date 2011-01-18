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
import net.mc_cubed.icedjava.util.NumericUtils;
import java.util.LinkedList;
import java.util.List;
import net.mc_cubed.icedjava.stun.StunAuthenticator;

/**
 *
 * @author Charles Chappell
 */
public class AttributeFactory {

    public static List<Attribute> processIntoList(byte[] packetBytes, int start, int off, int len, StunAuthenticator auth) {
        List<Attribute> attrList = new LinkedList<Attribute>();
        while (len > 0) {
            Attribute attr = GenericAttribute.process(packetBytes, start, off, auth);
            if (attr != null) {
                // Check for authentication attributes
                if (auth != null) {
                    switch (attr.getType()) {
                        case USERNAME:
                            StringAttribute userAttr = (StringAttribute) attr;
                            auth.setUsername(userAttr.getValue());
                            break;
                        case REALM:
                            StringAttribute realmAttr = (StringAttribute) attr;
                            auth.setRealm(realmAttr.getValue());
                            break;
                    }
                }
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

    public static List<Attribute> processIntoList(byte[] packetBytes, int start, int off, int len) {
        return processIntoList(packetBytes, start, off, len, null);
    }

    public static MappedAddressAttribute createMappedAddressAttribute(InetAddress address, int port) {
        return new MappedAddressAttribute(address, port);
    }

    public static XORMappedAddressAttribute createXORMappedAddressAttribute(InetAddress address, int port,byte[] transactionId) {
        return new XORMappedAddressAttribute(address, port, transactionId);
    }
}
