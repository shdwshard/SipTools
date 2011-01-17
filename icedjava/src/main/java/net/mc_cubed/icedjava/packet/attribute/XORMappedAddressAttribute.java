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
import java.util.Random;

/**
 *
 * @author Charles Chappell
 */
public class XORMappedAddressAttribute extends GenericAttribute {

    private InetAddress address;
    private int port;
    private byte xor;
    private static Random random = new Random();

    public XORMappedAddressAttribute(AttributeType type, int length, byte[] data) {
        super(type, length, data);

        /*
         * I shouldn't be so strict apparently.
         * 
        if (data[0] == 0x00) {
            throw new IllegalArgumentException("XOR Mapped Address Attribute must have a non-zero first byte");
        }*/

        // Grab the xor value
        xor = data[0];

        // Determine the address family and copy the data to 'addr'
        byte[] addr;

        switch (data[1]) {
            case 0x01:
                addr = new byte[4];
                System.arraycopy(data, 4, addr, 0, 4);
                break;
            case 0x02:
                addr = new byte[16];
                System.arraycopy(data, 4, addr, 0, 16);
                break;
            default:
                throw new IllegalArgumentException("Invalid Address Family: " + data[1]);
        }

        // Do the xor decoding
        for (int i = 0; i < addr.length; i++) {
            addr[i] = (byte) (addr[i] ^ xor);
        }

        // Interpret 'addr' into an InetAddress
        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }

        // Extract the port number
        int xorPort = 0x0000ffff & NumericUtils.toShort(data, 2);
        // Decode the xor
        int xorVal = ((0x00ff & xor) << 8) | (0x00ff & xor);

        port = xorPort ^ xorVal;

    }

    public XORMappedAddressAttribute(InetAddress address, int port, byte xor) {
        // Initialize members
        this.type = AttributeType.XOR_MAPPED_ADDRESS;
        this.address = address;
        this.port = port;
        this.xor = xor;

        // Split processing based on IPv4 or 6 address
        if (address instanceof Inet4Address) {
            data = new byte[8];
            Inet4Address i4addr = (Inet4Address) address;
            byte[] rawAddr = i4addr.getAddress();

            // Do the xor encoding of the IP
            for (int i = 0; i < rawAddr.length; i++) {
                rawAddr[i] = (byte) (rawAddr[i] ^ xor);
            }

            int xorVal = ((0x00ff & xor) << 8) | (0x00ff & xor);
            // xor the port bytes too
            int xorPort = port ^ xorVal;

            // Reserved Byte + IPv4 Address Flag
            data[0] = xor;
            data[1] = 0x01;
            // xored port number
            NumericUtils.toNetworkBytes((short) xorPort, data, 2);
            // Copy the xored IP bytes in
            System.arraycopy(rawAddr, 0, data, 4, 4); // Copy the IP address

            length = 8;
        } else if (address instanceof Inet6Address) {
            data = new byte[20];
            Inet6Address i6addr = (Inet6Address) address;
            byte[] rawAddr = i6addr.getAddress();

            // Do the xor encoding
            for (int i = 0; i < rawAddr.length; i++) {
                rawAddr[i] = (byte) (rawAddr[i] ^ xor);
            }

            int xorVal = ((0x00ff & xor) << 8) | (0x00ff & xor);

            // xor the port bytes too
            int xorPort = port ^ xorVal;

            // Reserved Byte + IPv6 Address Flag
            data[0] = xor;
            data[1] = 0x02;
            // xored port number
            NumericUtils.toNetworkBytes((short) xorPort, data, 2);
            // Copy the xored IP bytes in
            System.arraycopy(rawAddr, 0, data, 4, 16); // Copy the IP address

            length = 20;
        }
    }

    public XORMappedAddressAttribute(InetAddress address, int port) {
        this(address,port,(byte)random.nextInt(255));
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public byte getXor() {
        return xor;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[address=" + address + ":port=" + port + ":xor=" + xor + "]";

    }
}
