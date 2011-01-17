/*
 * Copyright 2011 Charles Chappell.
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
 *
 *
 */

package net.mc_cubed.icedjava.stun;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.packet.StunPacket;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * Allows sending StunPacket objects via Netty by encoding them into
 * ChannelBuffers automatically
 *
 * @author Charles Chappell
 * @since 1.0
 */
@ChannelPipelineCoverage(ChannelPipelineCoverage.ALL)
class StunPacketEncoder extends OneToOneEncoder {

    Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof StunPacket) {
            log.log(Level.FINEST,"Encoding stun packet {0}",msg);
            StunPacket stunPacket = (StunPacket)msg;
            byte[] packetBytes = stunPacket.getBytes();
            msg = ChannelBuffers.wrappedBuffer(packetBytes);
        }
        return msg;
    }

}
