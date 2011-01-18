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

import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.ErrorCodeAttribute;
import net.mc_cubed.icedjava.packet.attribute.FingerprintAttribute;
import net.mc_cubed.icedjava.packet.attribute.SoftwareAttribute;
import net.mc_cubed.icedjava.packet.attribute.UnknownAttributesAttribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageHeader;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.AttributeFactory;
import net.mc_cubed.icedjava.util.StringUtils;

/**
 *
 * @author Charles Chappell
 */
public class GenericStunListener implements StunListener {

    StunPacketSender socket;
    StunListenerType type;
    static SoftwareAttribute mySoftwareAttribute = new SoftwareAttribute(
            "IcedJava 1.0 Alpha - Copyright MC Cubed, Inc. of Saitama, Japan, released under LGPL v3.0");

    public GenericStunListener(StunPacketSender socket, StunListenerType type) {
        this.socket = socket;
        this.type = type;
    }

    @Override
    public boolean processPacket(DatagramPacket p) {
        // Only process Rfc5389 compliant Stun Packets
        if (MessageHeader.isRfc5389StunPacket(p)) {
            try {
                StunPacketImpl packet = new StunPacketImpl(p);
                processPacket(packet, (InetSocketAddress) p.getSocketAddress());
                return true;
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Datgram that caused this error: {0}", StringUtils.getHexString(p.getData()));
                return false;
            }
        } else {
            return false;
        }
    }

    // The actual workhorse method
    // Follows RFC 5389 Section 7.3.1
    @Override
    public boolean processPacket(StunPacket packet, SocketAddress senderAddress) {
        // Unknown Attribute Check
        List<AttributeType> unknownAttributes = new LinkedList<AttributeType>();
        for (Attribute attr : packet.getAttributes()) {
            switch (attr.getType()) {
                default:
                    unknownAttributes.add(attr.getType());
                    break;
                case MAPPED_ADDRESS:
                case ERROR_CODE:
                case SOFTWARE:
                case ALTERNATE_SERVER:
                case FINGERPRINT:
                case UNKNOWN_ATTRIBUTES:
                    break;
            }
        }

        // Check for a request to a non-server
        if (type == StunListenerType.CLIENT
                && (packet.getMessageClass() == MessageClass.REQUEST
                || packet.getMessageClass() == MessageClass.INDICATION)) {
            // Not acting as a server
            StunPacketImpl reply = new StunPacketImpl(MessageClass.ERROR, MessageMethod.BINDING);
            reply.getAttributes().add(new ErrorCodeAttribute(400, "Not acting as a server"));
            reply.getAttributes().add(mySoftwareAttribute);
            try {
                socket.send(senderAddress, reply);
            } catch (IOException ex) {
                Logger.getLogger(GenericStunListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        // Check for a reply to a non-client
        if (type == StunListenerType.SERVER
                && (packet.getMessageClass() == MessageClass.ERROR
                || packet.getMessageClass() == MessageClass.SUCCESS)) {
            // Not acting as a client
            StunPacketImpl reply = new StunPacketImpl(MessageClass.ERROR, MessageMethod.BINDING, packet.getTransactionId());
            reply.getAttributes().add(new ErrorCodeAttribute(400, "Not acting as a client"));
            reply.getAttributes().add(mySoftwareAttribute);
            reply.getAttributes().add(new FingerprintAttribute());
            try {
                socket.send(senderAddress, reply);
            } catch (IOException ex) {
                Logger.getLogger(GenericStunListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        // We only support the binding method
        if (packet.getMethod() != MessageMethod.BINDING) {
            // Not acting as a client
            StunPacketImpl reply = new StunPacketImpl(MessageClass.ERROR, MessageMethod.BINDING, packet.getTransactionId());
            reply.getAttributes().add(new ErrorCodeAttribute(400, "Unknown method invoked"));
            reply.getAttributes().add(mySoftwareAttribute);
            reply.getAttributes().add(new FingerprintAttribute());
            try {
                socket.send(senderAddress, reply);
            } catch (IOException ex) {
                Logger.getLogger(GenericStunListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        // Process the message
        StunPacket reply = null;
        switch (packet.getMessageClass()) {
            case REQUEST:
                reply = processRequest(packet, senderAddress);
                break;
            case INDICATION:
                reply = processIndication(packet, senderAddress);
                break;
            case SUCCESS:
                processSuccess(packet, senderAddress);
                break;
            case ERROR:
                processError(packet, senderAddress);
                break;
        }

        if (reply != null) {
            if (unknownAttributes.size() > 0) {
                reply.getAttributes().add(new UnknownAttributesAttribute(unknownAttributes));
            }
            reply.getAttributes().add(mySoftwareAttribute);
            reply.getAttributes().add(new FingerprintAttribute());
            try {
                socket.send(senderAddress, reply);
            } catch (IOException ex) {
                Logger.getLogger(GenericStunListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    private StunPacketImpl processIndication(StunPacket packet, SocketAddress remoteSocket) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void processSuccess(StunPacket packet, SocketAddress remoteSocket) {
        socket.storeAndNotify(packet);
    }

    private void processError(StunPacket packet, SocketAddress remoteSocket) {
        socket.storeAndNotify(packet);
    }

    private StunPacketImpl processRequest(StunPacket packet, SocketAddress remoteSocket) {
        InetSocketAddress insocket = (InetSocketAddress) remoteSocket;
        // Don't process any attributes, just reply with the mapped address attribute
        StunPacketImpl reply = new StunPacketImpl(MessageClass.SUCCESS, MessageMethod.BINDING, packet.getTransactionId());
        if (packet.isRfc5389()) {
            reply.getAttributes().add(AttributeFactory.createXORMappedAddressAttribute(insocket.getAddress(), insocket.getPort(),packet.getTransactionId()));
        } else {
            reply.getAttributes().add(AttributeFactory.createMappedAddressAttribute(insocket.getAddress(), insocket.getPort()));
        }
        return reply;
    }
}
