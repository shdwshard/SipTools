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
package net.mc_cubed.icedjava.stun;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;

/**
 * A datagram socket that can be used for STUN testing, or sending and receiving
 * data, or both at the same time. The socket implementation takes care of
 * separating the two types of data.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class DatagramDemultiplexerSocket extends DatagramStunSocket implements DemultiplexerSocket {

    protected DatagramListener listener;
    private final DatagramStunSocketBridge socket;

    final protected Queue<DatagramPacket> packetQueue = new LinkedBlockingQueue<DatagramPacket>();

    protected DatagramDemultiplexerSocket(DatagramListener listener) throws SocketException {
        super(StunListenerType.BOTH);
        this.listener = listener;
        this.socket = new DatagramStunSocketBridge();
    }

    protected DatagramDemultiplexerSocket(DatagramListener listener, StunListener stunListener) throws SocketException {
        super(stunListener);
        this.listener = listener;
        this.socket = new DatagramStunSocketBridge();
    }


    @Override
    protected void notStunPacket(DatagramPacket p) {
        if (listener == null) {
            packetQueue.add(p);
        } else {
            listener.deliverDatagram(p);
        }
    }

    public void setListener(DatagramListener listener) {
        this.listener = listener;

        // Dequeue any packets in the queue
        for (DatagramPacket p : packetQueue) {
            listener.deliverDatagram(p);
        }
    }

    public DatagramListener getListener() {
        return listener;
    }



    @Override
    public TransportType getTransportType() {
        return TransportType.UDP;
    }

    public void setStunListener(StunListener stunListener) {
        this.stunListener = stunListener;
    }

    public void receive(DatagramPacket dp) throws IOException {
        DatagramPacket p = packetQueue.poll();

        // Copy data
        dp.setSocketAddress(p.getSocketAddress());
        dp.setAddress(p.getAddress());
        dp.setPort(p.getPort());
        dp.setData(p.getData());
        dp.setLength(p.getLength());
    }

    public ChannelFuture send(ChannelBuffer buf) {
        return channel.write(buf);
    }

    public ChannelFuture send(DatagramPacket packet) {
        ChannelBuffer buf = ChannelBuffers.wrappedBuffer(packet.getData(), packet.getOffset(), packet.getLength());
        return channel.write(buf, packet.getSocketAddress());
    }

    public ChannelFuture sendTo(ChannelBuffer buf, InetSocketAddress destination) {
        return channel.write(buf, destination);

    }

    public DatagramSocket getDatagramSocket() {
        return socket;
    }

    /**
     * A dummy DatagramSocket implementation used to allow OIO dependant code to
     * leverage ICE without major code rewriting.
     */
    class DatagramStunSocketBridge extends DatagramSocket {

        @Override
        public synchronized void bind(SocketAddress sa) throws SocketException {
            if (channel != null) {
                channel.bind(sa);
            }
        }

        @Override
        public void close() {
            channel.close().awaitUninterruptibly();
        }

        @Override
        public void connect(InetAddress address, int port) {
            channel.connect(new InetSocketAddress(address,port));
        }

        @Override
        public void connect(SocketAddress sa) throws SocketException {
            channel.connect(sa);
        }

        @Override
        public void disconnect() {
            channel.disconnect().awaitUninterruptibly();
        }

        @Override
        public synchronized boolean getBroadcast() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public DatagramChannel getChannel() {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public InetAddress getInetAddress() {
            return ((InetSocketAddress)channel.getRemoteAddress()).getAddress();
        }

        @Override
        public InetAddress getLocalAddress() {
            return ((InetSocketAddress)channel.getLocalAddress()).getAddress();
        }

        @Override
        public int getLocalPort() {
            return ((InetSocketAddress)channel.getLocalAddress()).getPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return channel.getLocalAddress();
        }

        @Override
        public int getPort() {
            return ((InetSocketAddress)channel.getRemoteAddress()).getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return channel.getRemoteAddress();
        }

        @Override
        public synchronized boolean getReuseAddress() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized int getSoTimeout() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized int getTrafficClass() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isBound() {
            return channel.isBound();
        }

        @Override
        public boolean isClosed() {
            if (channel == null) {
                return true;
            } else {
                return !channel.isOpen();
            }
        }

        @Override
        public boolean isConnected() {
            return channel.isConnected();
        }

        @Override
        public void receive(DatagramPacket dp) {
            DatagramPacket packet = packetQueue.poll();
            if (packet != null) {
                dp.setData(packet.getData(),packet.getOffset(),packet.getLength());
                dp.setSocketAddress(packet.getSocketAddress());
            }
        }

        @Override
        public void send(DatagramPacket dp) throws IOException {
            ChannelFuture cf = channel.write(dp, dp.getSocketAddress());
            cf.awaitUninterruptibly();
            if (!cf.isSuccess()) {
                throw (IOException)cf.getCause();
            }
        }

        @Override
        public synchronized void setBroadcast(boolean bln) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized void setReceiveBufferSize(int i) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized void setReuseAddress(boolean bln) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized void setSendBufferSize(int i) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized void setSoTimeout(int i) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public synchronized void setTrafficClass(int i) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        public DatagramStunSocketBridge() throws SocketException {
            super();
            super.close();
        }

    }
}
