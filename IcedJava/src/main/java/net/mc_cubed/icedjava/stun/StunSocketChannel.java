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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * Represents an StunSocketChannel which is used to send and receive data from
 * a particular StunDemultiplexerSocket
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface StunSocketChannel extends ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    SocketAddress receive(ByteBuffer dst);

    int send(ByteBuffer src, SocketAddress target) throws IOException;
    /**
     * Returns the transport type of the socket
     * @return TCP if stream based, UDP if datagram based
     */
    public TransportType getTransportType();
    /**
     * Get the local address of this socket
     * @return the local address of the socket
     */
    public InetAddress getLocalAddress();
    /**
     * Get the local port of this socket
     * @return the local port of the socket
     */
    public int getLocalPort();
    /**
     * Get the local socket address
     * @return the local socket address
     */
    public InetSocketAddress getLocalSocketAddress();
    
}
