/*
 * Copyright 2010 Charles Chappell.
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.packet.StunPacket;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

/**
 * Decodes a ChannelBuffer into a StunPacket object if it contains one
 *
 * @author Charles Chappell
 * @since 1.0
 */
class StunPacketProtocolFilter extends BaseFilter {

    final StunFactory factory = StunFactory.getInstance();
    final StunAuthenticator authenticator;
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        ByteBuffer buffer = ctx.getMessage();
        StunPacket packet = null;
        try {
            if (authenticator != null) {
                packet = factory.processByteBuffer(buffer, authenticator);
            } else {
                packet = factory.processByteBuffer(buffer);
            }
        } catch (Exception ex) {
            // If the factory throws an exception, this probably wasn't a stun packet
            //log.log(Level.INFO,"Caught an exception processing packet buffer",ex);
        }

        if (packet != null) {
            log.log(Level.FINEST, "Decoded stun packet {0}", packet);
            ctx.setMessage(packet);            
        }
        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        Object msg = ctx.getMessage();
        if (msg instanceof StunPacket) {
            log.log(Level.FINEST,"Encoding stun packet {0}",msg);
            StunPacket stunPacket = (StunPacket)msg;
            byte[] packetBytes = stunPacket.getBytes();
            ctx.setMessage(ByteBuffer.wrap(packetBytes));
        }
        return ctx.getInvokeAction();
    }

    
    public StunPacketProtocolFilter() {
        this(null);
    }

    public StunPacketProtocolFilter(StunAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
}
