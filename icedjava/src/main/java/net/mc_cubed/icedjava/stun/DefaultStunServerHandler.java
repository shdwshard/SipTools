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
import net.mc_cubed.icedjava.packet.attribute.SoftwareAttribute;
import net.mc_cubed.icedjava.packet.attribute.UnknownAttributesAttribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.AttributeFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 *
 * @author Charles Chappell
 */
@ChannelPipelineCoverage(ChannelPipelineCoverage.ONE)
public class DefaultStunServerHandler extends SimpleChannelHandler {

    static SoftwareAttribute mySoftwareAttribute = AttributeFactory.createSoftwareAttribute(
            "IcedJava 1.0 - Copyright MC Cubed, Inc. of Saitama, Japan, released under LGPL v3.0");

    public DefaultStunServerHandler() {
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof StunPacket) {
            if (processPacket((StunPacket)e.getMessage(), e.getRemoteAddress(),ctx)) {
                return;
            } else {
                super.messageReceived(ctx,e);                
            }
        } else {
            super.messageReceived(ctx, e);
        }
    }



    // The actual workhorse method
    // Follows RFC 5389 Section 7.3.1
    protected boolean processPacket(StunPacket packet, SocketAddress senderAddress,ChannelHandlerContext ctx) {
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

        // Check for client type messages (we don't handle these)
        if ((packet.getMessageClass() != MessageClass.REQUEST
                && packet.getMessageClass() != MessageClass.INDICATION)) {
            // Not acting as a client
            return false;
        }

        // We only support the binding method currently
        if (packet.getMethod() != MessageMethod.BINDING) {
            // Not acting as a client
            StunPacketImpl reply = new StunPacketImpl(MessageClass.ERROR, MessageMethod.BINDING, packet.getTransactionId());
            reply.getAttributes().add(new ErrorCodeAttribute(400, "Unknown method invoked"));
            reply.getAttributes().add(mySoftwareAttribute);
            reply.getAttributes().add(AttributeFactory.createFingerprintAttribute());

            ChannelFuture cf = Channels.future(ctx.getChannel());
            Channels.write(ctx, cf, reply, senderAddress);
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
        }

        if (reply != null) {
            if (unknownAttributes.size() > 0) {
                reply.getAttributes().add(new UnknownAttributesAttribute(unknownAttributes));
            }
            reply.getAttributes().add(mySoftwareAttribute);
            reply.getAttributes().add(AttributeFactory.createFingerprintAttribute());
            
            ChannelFuture cf = Channels.future(ctx.getChannel());
            Channels.write(ctx, cf, reply, senderAddress);
        }
        return true;
    }

    private StunPacketImpl processIndication(StunPacket packet, SocketAddress remoteSocket) {
        throw new UnsupportedOperationException("Not yet implemented");
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
