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
public class SipNetworkLayer implements gov.nist.core.net.NetworkLayer {
    
    StunUtil stunUtil = new StunUtil();

    @Override
    public DatagramSocket createDatagramSocket() throws SocketException {
        return StunUtil.getDemultiplexerSocket(null,null).getDatagramSocket();
    }

    @Override
    public DatagramSocket createDatagramSocket(int port, InetAddress bindAddress) throws SocketException {
        return StunUtil.getDemultiplexerSocket(new InetSocketAddress(bindAddress,port),null).getDatagramSocket();
    }

    @Override
    public ServerSocket createServerSocket(int i, int i1, InetAddress ia) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLServerSocket createSSLServerSocket(int i, int i1, InetAddress ia) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLSocket createSSLSocket(InetAddress ia, int i) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLSocket createSSLSocket(InetAddress ia, int i, InetAddress ia1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
