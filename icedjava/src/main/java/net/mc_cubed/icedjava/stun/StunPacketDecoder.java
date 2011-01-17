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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.packet.StunPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * Decodes a ChannelBuffer into a StunPacket object if it contains one
 *
 * @author Charles Chappell
 * @since 1.0
 */
@ChannelPipelineCoverage(ChannelPipelineCoverage.ALL)
class StunPacketDecoder extends FrameDecoder {

    final StunFactory factory = StunFactory.getInstance();
    final StunAuthenticator authenticator;
    Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        StunPacket packet = null;
        try {
            if (authenticator != null) {
                packet = factory.processChannelBuffer(buffer, authenticator);
            } else {
                packet = factory.processChannelBuffer(buffer);
            }
        } catch (Exception ex) {
            // If the factory throws an exception, this probably wasn't a stun packet
        }

        if (packet != null) {
            log.log(Level.FINEST, "Decoded stun packet {0}", packet);
            return packet;
        } else {
            return buffer;
        }
    }

    public StunPacketDecoder() {
        this(null);
    }

    public StunPacketDecoder(StunAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
}
