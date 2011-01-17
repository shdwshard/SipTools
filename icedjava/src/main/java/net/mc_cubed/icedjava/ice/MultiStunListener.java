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

/**
 *
 * @author Charles Chappell
 * @since 0.9
 * @deprecated StunListeners are being replaced by ChannelHandlers
 */
public interface MultiStunListener {
    @Deprecated
    boolean processPacket(DatagramPacket packet, DatagramStunSocket source);

    boolean processPacket(StunPacket packet, SocketAddress senderAddress, DatagramStunSocket source);
}
