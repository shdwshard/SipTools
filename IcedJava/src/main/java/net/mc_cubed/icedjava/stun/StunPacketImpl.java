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
package net.mc_cubed.icedjava.stun;

import net.mc_cubed.icedjava.packet.attribute.AttributeFactory;
import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageHeader;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import net.mc_cubed.icedjava.util.NumericUtils;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import net.mc_cubed.icedjava.packet.StunPacket;

/**
 * Provides a java POJO representation of a STUN packet.
 *
 * @author Charles Chappell
 * @since 0.9
 */
class StunPacketImpl implements StunPacket {

    MessageHeader header;
    List<Attribute> attributes;
    BigInteger id;

    // Helper Constructor
    public StunPacketImpl(MessageClass mClass, MessageMethod method) {
        this(mClass, method, null);
    }

    // Create a new stun packet from scratch
    public StunPacketImpl(MessageClass mClass, MessageMethod method, byte[] transactionId) {
        header = new MessageHeader(mClass, method, transactionId);
        attributes = new LinkedList<Attribute>();
    }

    // Create a new stun packet from bytes
    public StunPacketImpl(byte[] packetBytes, int off, int len) {
        header = new MessageHeader(packetBytes, off, len);
        attributes = AttributeFactory.processIntoList(packetBytes, off, off + 20, len - 20);
    }

    @Override
    public byte[] getBytes() {
        // Do a length check of the attributes
        int length = 20; // Length of the STUN header

        // Add the length of each attribute and its header
        for (Attribute a : attributes) {
            length += NumericUtils.makeMultipleOf(4 + a.getLength(), 4);
        }

        // Allocate a buffer big enough to hold the stun packet
        byte[] data = new byte[length];

        // Write the header, including the STUN packet length (minus the header)
        if (header.write(data, 0, length - 20) != 20) {
            throw new RuntimeException("Encountered an error writing byte stream");
        }

        int off = 20; // The length of the header
        for (Attribute a : attributes) {
            off += NumericUtils.makeMultipleOf(a.write(data, off), 4);
        }

        return data;
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[header=" + header + ":attributes=" + attributes + "]";
    }

    @Override
    public MessageClass getMessageClass() {
        return header.getMessageClass();
    }

    @Override
    public MessageMethod getMethod() {
        return header.getMessageMethod();
    }

    @Override
    public BigInteger getId() {
        if (id == null) {
            id = BigInteger.ZERO;
            byte[] tid = header.getTransactionId();
            for (int i = 0; i < tid.length; i++) {
                id = id.shiftLeft(8).add(BigInteger.valueOf(0x00ff * tid[i]));
            }
        }

        return id;
    }

    @Override
    public byte[] getTransactionId() {
        return header.getTransactionId();
    }

    @Override
    public boolean isRfc5389() {
        return header.isRfc5389header();
    }
}
