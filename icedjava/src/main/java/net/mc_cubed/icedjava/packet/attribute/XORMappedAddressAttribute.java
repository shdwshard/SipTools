/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.packet.attribute;

import java.net.InetAddress;

/**
 *
 * @author charles
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
