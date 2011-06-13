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
import java.util.concurrent.Future;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.stun.event.StunEventListener;

/**
 * A generic demultiplexer socket interface implemented by both Datagram and
 * Stream based STUN sockets
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface DemultiplexerSocket extends StunSocketChannel {
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
     * Register a stun event listener with this demultiplexer socket.
     * 
     * @param listener listener to register
     */
    public void registerStunEventListener(StunEventListener listener);
    
    /**
     * De-register a stun event listener from this demultiplexer socket.
     * 
     * @param listener listener to register
     */
    public void deregisterStunEventListener(StunEventListener listener);

}
