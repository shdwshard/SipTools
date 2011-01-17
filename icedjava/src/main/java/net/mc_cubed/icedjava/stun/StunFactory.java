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

import net.mc_cubed.icedjava.packet.StunPacket;
import java.net.DatagramPacket;
import javax.inject.Named;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author Charles Chappell
 */
@Named
public class StunFactory {

    private static StunFactory instance;

    public static StunFactory getInstance() {
        if (instance == null) {
            instance = new StunFactory();
        }

        return instance;
    }

    public StunPacket processDatagram(DatagramPacket packet) {
        return new StunPacketImpl(packet);
    }

    public StunPacket processDatagram(DatagramPacket packet, StunAuthenticator auth) {
        return new StunPacketImpl(packet, auth);
    }

    public StunPacket processChannelBuffer(ChannelBuffer buffer) {
        StunPacket retval = new StunPacketImpl(buffer.toByteBuffer().array(),buffer.readerIndex(),buffer.readableBytes(),null);
        buffer.skipBytes(buffer.readableBytes());
        return retval;
    }

    public StunPacket processChannelBuffer(ChannelBuffer buffer, StunAuthenticator auth) {
        StunPacket retval = new StunPacketImpl(buffer.toByteBuffer().array(),buffer.readerIndex(),buffer.readableBytes(),auth);
        buffer.skipBytes(buffer.readableBytes());
        return retval;
    }
}
