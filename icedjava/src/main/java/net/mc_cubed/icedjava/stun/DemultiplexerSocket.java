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
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import net.mc_cubed.icedjava.stun.event.StunEventListener;

/**
 * A generic demultiplexer socket interface implemented by both Datagram and
 * Stream based STUN sockets
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface DemultiplexerSocket extends StunSocketChannel,StunPacketSender {

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
    
    /**
     * Get a java OIO DatagramSocket representing the non-stun data side of this
     * DemultiplexerSocket
     * 
     * @return A DatagramSocket for non-stun data.
     * @throws SocketException
     */
    public DatagramSocket getDatagramSocket() throws SocketException;

    public ServerSocket getServerSocket() throws IOException;

    public Socket getSocket() throws IOException;

}
