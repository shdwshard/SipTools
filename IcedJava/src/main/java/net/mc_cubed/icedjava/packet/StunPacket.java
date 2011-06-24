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
package net.mc_cubed.icedjava.packet;

import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import java.math.BigInteger;
import java.util.List;

/**
 * Provides a POJO representation of a STUN Packet.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface StunPacket {

    /**
     * Gets a list of attributes contained in this packet.
     *
     * @return a list of STUN attributes
     */
    List<Attribute> getAttributes();

    /**
     * Creates a binary representation of the STUN packet suitable for network
     * transmission
     *
     * @return a binary representation of this STUN packet
     */
    byte[] getBytes();

    /**
     * Get the Transaction ID number of this STUN packet
     *
     * @return a BigInteger representation of the Transaction ID number of this
     * STUN packet
     */
    BigInteger getId();

    /**
     * Fetches the message class of this STUN packet.
     *
     * @return the message class of this STUN packet
     */
    MessageClass getMessageClass();

    /**
     * Fetches the method name of this STUN packet
     *
     * @return the method name of this STUN packet
     */
    MessageMethod getMethod();

    /**
     * Fetch the binary Transaction ID (same as getId()) of this STUN packet
     *
     * @return the binary representation of this packet's transaction ID number
     */
    public byte[] getTransactionId();

    /**
     * Determine whether this packet uses an RFC 5389 compliant Transaction ID
     *
     * @return true if packet uses an RFC 5389 Transaction ID, false otherwise
     */
    public boolean isRfc5389();
}
