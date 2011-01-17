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

package net.mc_cubed.icedjava.ice;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.stun.DatagramStunSocket;
import net.mc_cubed.icedjava.stun.StunListener;

/**
 * This class adapts a DatagramStunSocket (single source datagram listener) to an
 * IceDatagramSocket which is a multi-source datagram listener
 *
 * @author Charles Chappell
 * @since 0.9
 * @deprecated StunListeners are being replaced with Channel Handlers
 */
public class SocketSourceAdapter implements StunListener {

    final private MultiStunListener target;
    final private DatagramStunSocket source;

    public SocketSourceAdapter(DatagramStunSocket source, MultiStunListener target) {
        this.source = source;
        this.target = target;
    }

    @Override
    @Deprecated
    public boolean processPacket(DatagramPacket p) {
        return target.processPacket(p, source);
    }

    @Override
    public boolean processPacket(StunPacket packet, SocketAddress senderAddress) {
        return target.processPacket(packet, senderAddress, source);
    }

}
