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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import net.mc_cubed.icedjava.packet.StunPacket;

/**
 * An exception indicating that the STUN packet attempting to be sent is too big
 * and is exceedingly likely to be silently killed along the route.
 * 
 * @author Charles Chappell
 * @since 1.0
 */
public class OversizeStunPacketException extends IOException {

    String message;
    
    public OversizeStunPacketException(SocketAddress remoteSocket, StunPacket packet) {
        this.message = MessageFormat.format(ResourceBundle.getBundle("net.mc_cubed.icedjava.Messages").getString("OversizeStunPacketMessage"),remoteSocket,packet);
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
