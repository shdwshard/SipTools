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

/**
 *
 * @author Charles Chappell
 */
public class MappedAddressAttribute extends GenericAttribute {

    private InetAddress address;
    private int port;

    public MappedAddressAttribute(AttributeType type, int length, byte[] data) {
        super(type, length, data);

        if (data[0] != 0x00) {
            throw new IllegalArgumentException("Mapped Address Attribute must have a Zeroed first byte");
        }


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

        // Interpret 'addr' into an InetAddress
        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }

        // Extract the port number
        port = 0x0000ffff & NumericUtils.toShort(data, 2);

    }

    public MappedAddressAttribute(InetAddress address, int port) {
        // Initialize members
        this.type = AttributeType.MAPPED_ADDRESS;
        this.address = address;
        this.port = port;

        // Split processing based on IPv4 or 6 address
        if (address instanceof Inet4Address) {
            data = new byte[8];
            Inet4Address i4addr = (Inet4Address) address;
            byte[] rawAddr = i4addr.getAddress();

            // Reserved Byte + IPv4 Address Flag
            NumericUtils.toNetworkBytes((short) 0x0001, data, 0);
            NumericUtils.toNetworkBytes((short) port, data, 2);
            System.arraycopy(rawAddr, 0, data, 4, 4); // Copy the IP address
            length = 8;
        } else if (address instanceof Inet6Address) {
            data = new byte[20];
            Inet6Address i6addr = (Inet6Address) address;
            byte[] rawAddr = i6addr.getAddress();

            // Reserved Byte + IPv6 Address Flag
            NumericUtils.toNetworkBytes((short) 0x0002, data, 0);
            NumericUtils.toNetworkBytes((short) port, data, 2);
            System.arraycopy(rawAddr, 0, data, 4, 16); // Copy the IP address
            length = 20;
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[type=" + type + ":address=" + address + ":port=" + port + "]";

    }
}
