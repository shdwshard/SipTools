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
 */
package net.mc_cubed.icedjava.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.TransportType;

/**
 * An experimental class to interface with JAIN-SIP to enable SIP sockets to be
 * ICE sockets<br/>
 * <br/>
 * WARNING: This is HIGHLY EXPERIMENTAL and INCOMPLETE!<br/>
 * TODO: Finish implementing the underlying classes needed to make this work
 *
 * @author Charles Chappell
 * @since 1.0
 */
class SipNetworkLayer implements gov.nist.core.net.NetworkLayer {

    StunUtil stunUtil = new StunUtil();

    @Override
    public DatagramSocket createDatagramSocket() throws SocketException {
        try {
            return StunUtil.getDemultiplexerSocket(null, TransportType.UDP, null).getDatagramSocket();
        } catch (IOException ex) {
            throw (SocketException) new SocketException().initCause(ex);
        }
    }

    @Override
    public DatagramSocket createDatagramSocket(int port, InetAddress bindAddress) throws SocketException {
        try {
            return StunUtil.getDemultiplexerSocket(new InetSocketAddress(bindAddress, port),TransportType.UDP, null).getDatagramSocket();
        } catch (IOException ex) {
            throw (SocketException) new SocketException().initCause(ex);
        }
    }

    @Override
    public ServerSocket createServerSocket(int i, int port, InetAddress bindAddress) throws IOException {
        try {
            return StunUtil.getDemultiplexerSocket(new InetSocketAddress(bindAddress, port),TransportType.TCP, null).getServerSocket();
        } catch (IOException ex) {
            throw (SocketException) new SocketException().initCause(ex);
        }
    }

    @Override
    public SSLServerSocket createSSLServerSocket(int i, int i1, InetAddress bindAddress) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Socket createSocket(InetAddress bindAddress, int port) throws IOException {
        try {
            return StunUtil.getDemultiplexerSocket(new InetSocketAddress(bindAddress, port),TransportType.TCP, null).getSocket();
        } catch (IOException ex) {
            throw (SocketException) new SocketException().initCause(ex);
        }
    }

    @Override
    public Socket createSocket(InetAddress bindAddress, int port, InetAddress ia1) throws IOException {
        try {
            return StunUtil.getDemultiplexerSocket(new InetSocketAddress(bindAddress, port),TransportType.TCP, null).getSocket();
        } catch (IOException ex) {
            throw (SocketException) new SocketException().initCause(ex);
        }
    }

    @Override
    public SSLSocket createSSLSocket(InetAddress bindAddress, int port) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLSocket createSSLSocket(InetAddress bindAddress, int port, InetAddress ia1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
