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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.mc_cubed.icedjava.packet.header.MessageHeader;

/**
 *
 *
 * @author Charles Chappell
 * @since 0.9
 */
class XORMappedAddressAttributeImpl extends GenericAttribute implements XORMappedAddressAttribute {

    private byte[] xAddress;
    private int xPort;

    public XORMappedAddressAttributeImpl(AttributeType type, int length, byte[] data) {
        super(type, length, data);

        // Determine the address family and copy the address data to 'xAddress'
        switch (data[1]) {
            case 0x01:
                xAddress = new byte[4];
                System.arraycopy(data, 4, xAddress, 0, 4);
                break;
            case 0x02:
                xAddress = new byte[16];
                System.arraycopy(data, 4, xAddress, 0, 16);
                break;
            default:
                throw new IllegalArgumentException("Invalid Address Family: " + data[1]);
        }

        // Convert data bytes to the xPort value
        xPort = NumericUtils.toShort(data, 2);

    }

    protected XORMappedAddressAttributeImpl(InetAddress address, int port, byte[] txId) {
        if (txId == null || (address instanceof Inet6Address && txId.length != 16) ||
                (address instanceof Inet4Address && txId.length < 4)) {
            throw new java.lang.IllegalArgumentException("TransactionID must be a 128-bit number");
        }
        for (int i = 0; i < MessageHeader.MAGIC_COOKIE.length; i++) {
            if (MessageHeader.MAGIC_COOKIE[i] != txId[i]) {
                throw new java.lang.IllegalArgumentException("TransactionID must have an RFC 5389 compliant Magic Cookie");
            }
        }

        // Initialize members
        this.type = AttributeType.XOR_MAPPED_ADDRESS;

        /**
         * RFC 5389 15.2: XOR-MAPPED-ADDRESS
         *
         * X-Port is computed by taking the mapped port in host byte order,
         * XOR'ing it with the most significant 16 bits of the magic cookie,
         * and then converting the result to network byte order.
         */
        int xorVal = NumericUtils.toShort(txId);
        xPort = port ^ xorVal;

        /**
         * RFC 5389 15.2: XOR-MAPPED-ADDRESS
         *
         * If the IP address family is IPv4, X-Address is computed by taking
         * the mapped IP address in host byte order, XOR'ing it with the
         * magic cookie, and converting the result to network byte order.
         * If the IP address family is IPv6, the X-Address is computed by
         * taking the mapped IP address in host byte order, XOR'ing it with
         * the concatenation of the magic cookie and the 96-bit transaction
         * ID, and converting the result to network byte order.
         */
        xAddress = address.getAddress();

        for (int i = 0; i < xAddress.length; i++) {
            xAddress[i] = (byte) (xAddress[i] ^ txId[i]);
        }

        // Split processing based on IPv4 or 6 address
        if (address instanceof Inet4Address) {
            data = new byte[8];

            // Reserved Byte + IPv4 Address Flag
            data[0] = 0x00;
            data[1] = 0x01;

            // xored port number
            NumericUtils.toNetworkBytes((short) xPort, data, 2);
            // Copy the xored IP bytes in
            System.arraycopy(xAddress, 0, data, 4, 4); // Copy the IP address

            length = 8;
        } else if (address instanceof Inet6Address) {
            data = new byte[20];

            // Reserved Byte + IPv6 Address Flag
            data[0] = 0x00;
            data[1] = 0x02;

            // xored port number
            NumericUtils.toNetworkBytes((short) xPort, data, 2);
            // Copy the xored IP bytes in
            System.arraycopy(xAddress, 0, data, 4, 16); // Copy the IP address

            length = 20;
        }
    }

    /**
     * Gets the decoded address represented by this attribute.
     * @param transactionId can be MessageHeader.MAGIC_COOKIE if the address
     * represented by this attribute is an IPv4 address, otherwise MUST be the
     * full 128-bit transaction ID of the STUN packet.
     * @return the decoded address represented by this attribute
     */
    @Override
    public InetAddress getAddress(byte[] transactionId) {
        if (transactionId == null || xAddress.length > transactionId.length) {
            throw new java.lang.IllegalArgumentException("TransactionId is a required parameter.");
        }

        byte[] addr = new byte[xAddress.length];
        // Do the xor decoding
        for (int i = 0; i < xAddress.length; i++) {
            addr[i] = (byte) (xAddress[i] ^ transactionId[i]);
        }

        // Interpret 'addr' into an InetAddress
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the port number represented by this attribute. The RFC 5389
     * magic cookie is used to decode the port, so no transaction ID is required
     *
     * @return the decoded port number represented by this attribute
     */
    @Override
    public int getPort() {
        /**
         * RFC 5389 15.2: XOR-MAPPED-ADDRESS
         *
         * X-Port is computed by taking the mapped port in host byte order,
         * XOR'ing it with the most significant 16 bits of the magic cookie,
         * and then converting the result to network byte order.
         */
        return xPort ^ NumericUtils.toShort(MessageHeader.MAGIC_COOKIE);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[xAddress=" + xAddress + ":xPort=" + xPort + "]";

    }
}
