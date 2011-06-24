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

/**
 * Gets the MappedAddress attribute XORed with the transactionId. This attribute
 * is preferred to the MappedAddress attribute as some overly aggressive NAT
 * devices will attempt to rewrite the binary contents of packets in an attempt
 * to correct the source/destination IP addresses contained in them.
 * For our purposes, we need this information unaltered, and so XOR the mapped
 * address value with the transaction ID to prevent the NAT device from seeing
 * the real value contained in this attribute.
 * 
 * This attribute is ONLY supported by RFC 5389 clients/servers.
 *
 * @author Charles Chappell
 * @since 0.9
 * @see MappedAddressAttribute
 */
public interface XORMappedAddressAttribute extends Attribute {

    /**
     * Gets the decoded address represented by this attribute.
     * @param transactionId can be MessageHeader.MAGIC_COOKIE if the address
     * represented by this attribute is an IPv4 address, otherwise MUST be the
     * full 128-bit transaction ID of the STUN packet.
     * @return the decoded address represented by this attribute
     */
    InetAddress getAddress(byte[] transactionId);

    /**
     * Returns the port number represented by this attribute. The RFC 5389
     * magic cookie is used to decode the port, so no transaction ID is required
     *
     * @return the decoded port number represented by this attribute
     */
    int getPort();

}
