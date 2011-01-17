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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import net.mc_cubed.icedjava.packet.StunPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;

/**
 * A generic demultiplexer socket interface implemented by both Datagram and
 * Stream based STUN sockets
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface DemultiplexerSocket {
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
    /**
     * Performs a stun BINDING request to the specified server.
     *
     * @param stunServer STUN server to test
     * @return a future which can be used to obtain the result of this STUN test
     * @throws IOException
     * @throws InterruptedException
     */
    public Future<StunReply> doTest(InetSocketAddress stunServer) throws IOException, InterruptedException;
    /**
     * Performs a stun BINDING request to the specified server.
     *
     * @param stunServer STUN server to test
     * @param packet packet to use for this test
     * @return a future which can be used to obtain the result of this STUN test
     * @throws IOException
     * @throws InterruptedException
     */
    public Future<StunReply> doTest(InetSocketAddress stunServer,StunPacket packet) throws IOException, InterruptedException;

    /**
     * Close this socket
     */
    public void close();

    /**
     * Send data to the default destination of this socket, or the connected
     * peer in the case of a stream socket
     *
     * @param buf buffer containing the data to send
     * @return A future which can be used to obtain the status of the operation
     */
    public ChannelFuture send(ChannelBuffer buf);

    /**
     * Send a DatagramPacket using this socket
     *
     * @param packet specifies the packet data and destination address
     * @return A future which can be used to obtain the status of the operation
     */
    public ChannelFuture send(DatagramPacket packet);

    /**
     * Send a datagram packet using this socket
     *
     * @param buf buffer containing the data to send
     * @param destination specifies the target address of the packet
     * @return A future which can be used to obtain the status of the operation
     */
    public ChannelFuture sendTo(ChannelBuffer buf, InetSocketAddress destination);
    
}
