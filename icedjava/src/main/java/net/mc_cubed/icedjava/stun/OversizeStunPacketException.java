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
import java.net.SocketAddress;
import net.mc_cubed.icedjava.packet.StunPacket;

/**
 *
 * @author Charles Chappell
 * @since 1.0
 */
class OversizeStunPacketException extends IOException {

    public OversizeStunPacketException(SocketAddress remoteSocket, StunPacket packet) {
        // TODO: Implement this in a sane manner.
    }

    @Override
    public String getLocalizedMessage() {
        // TODO: Implement this in a sane manner.
        return super.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        // TODO: Implement this in a sane manner.
        return super.getMessage();
    }


}
