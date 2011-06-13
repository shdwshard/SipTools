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
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.stun.event.BytesAvailableEvent;
import net.mc_cubed.icedjava.stun.event.StunEvent;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import net.mc_cubed.icedjava.util.AddressedByteBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;

/**
 * A datagram socket that can be used for STUN testing, or sending and receiving
 * data, or both at the same time. The socket implementation takes care of
 * separating the two types of data.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class DatagramDemultiplexerSocket extends DatagramStunSocket implements DemultiplexerSocket, StunSocketChannel {

    boolean nonBlocking = false;
    private DatagramStunSocketBridge socket = null;
    final protected Queue<AddressedByteBuffer> bufferQueue = new LinkedBlockingQueue<AddressedByteBuffer>();
    final protected HashSet<StunEventListener> listeners = new HashSet<StunEventListener>();
    
    @Inject
    Event<StunEvent> eventBroadcaster;
    
    protected DatagramDemultiplexerSocket(StunEventListener stunEventListener) {
        if (stunEventListener != null) {
            listeners.add(stunEventListener);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof StunPacket) {
            super.messageReceived(ctx, e);
        } else if (e.getMessage() instanceof ChannelBuffer) {
            ChannelBuffer cb = (ChannelBuffer) e.getMessage();
            bufferQueue.add(new AddressedByteBuffer(e.getRemoteAddress(), cb.toByteBuffer()));
            broadcastReceivedMessage();
        } else {
            log.log(Level.WARNING, "Got a packet of unknown type {0} from peer {1}", new Object[]{e.getMessage().getClass().getName(), e.getRemoteAddress()});
        }
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.UDP;
    }

    public DatagramSocket getDatagramSocket() throws SocketException {
        if (socket == null) {
            socket = new DatagramStunSocketBridge(this);
        }
        return socket;
    }

    @Override
    public int read(ByteBuffer bb) throws IOException {
        AddressedByteBuffer packet = bufferQueue.poll();
        
        bb.put(packet.getBuffer());
        return bb.remaining();
    }

    @Override
    public boolean isOpen() {
        return localContext.getChannel().isOpen();
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        ChannelBuffer cb = ChannelBuffers.copiedBuffer(bb);
        ChannelFuture future = Channels.future(localContext.getChannel());
        Channels.write(localContext, future, cb);
        if (!nonBlocking) {
            try {
                future.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(DatagramDemultiplexerSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (future.isSuccess()) {
                return bb.remaining();
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public long read(ByteBuffer[] bbs, int off, int len) throws IOException {
        long bytesRead = 0;
        for (int i = off; i < off + len; i++) {
            bytesRead += read(bbs[i]);
        }
        return bytesRead;
    }

    @Override
    public long read(ByteBuffer[] bbs) throws IOException {
        return read(bbs,0,bbs.length);
    }

    @Override
    public long write(ByteBuffer[] bbs, int off, int len) throws IOException {
        long bytesWritten = 0;
        for (int i = off; i < off + len; i++) {
            bytesWritten += write(bbs[i]);
        }
        return bytesWritten;
        
    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        return write(bbs,0,bbs.length);        
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) {
        AddressedByteBuffer packet = bufferQueue.poll();
        
        dst.put(packet.getBuffer());
        return packet.getAddress();
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) {
        int remainingBytes = src.remaining();
        ChannelFuture cf = Channels.future(localContext.getChannel());
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(src);
        Channels.write(localContext, cf, buffer, target);
        if (!nonBlocking) {
            try {
                cf.await();
                if (cf.isSuccess()) {
                    return remainingBytes;
                } else {
                    return 0;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(DatagramDemultiplexerSocket.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        } else {
            return remainingBytes;
        }
        
    }

    @Override
    public void registerStunEventListener(StunEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deregisterStunEventListener(StunEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void broadcastReceivedMessage() {
        StunEvent event = new BytesAvailableEventImpl(this);

        // Use CDI if availble
        if (eventBroadcaster != null) {
            eventBroadcaster.fire(event);
        }
        
        // Fire listeners next
        for (StunEventListener listener : listeners) {
            listener.stunEvent(event);
        }       
    }

    /**
     * A dummy DatagramSocket implementation used to allow OIO dependant code to
     * leverage ICE without major code rewriting.
     */
    class DatagramStunSocketBridge extends DatagramSocket {

        protected final DatagramDemultiplexerSocket outer;

        public DatagramStunSocketBridge(DatagramDemultiplexerSocket outer) throws SocketException {
            super();
            super.close();
            this.outer = outer;

        }
        
        
        @Override
        public synchronized void bind(SocketAddress sa) throws SocketException {
            if (localContext.getChannel() != null) {
                localContext.getChannel().bind(sa);
            }
        }

        @Override
        public void close() {
            localContext.getChannel().close().awaitUninterruptibly();
        }

        @Override
        public void connect(InetAddress address, int port) {
            localContext.getChannel().connect(new InetSocketAddress(address, port));
        }

        @Override
        public void connect(SocketAddress sa) throws SocketException {
            localContext.getChannel().connect(sa);
        }

        @Override
        public void disconnect() {
            localContext.getChannel().disconnect().awaitUninterruptibly();
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
            return ((InetSocketAddress) localContext.getChannel().getRemoteAddress()).getAddress();
        }

        @Override
        public InetAddress getLocalAddress() {
            return ((InetSocketAddress) localContext.getChannel().getLocalAddress()).getAddress();
        }

        @Override
        public int getLocalPort() {
            return ((InetSocketAddress) localContext.getChannel().getLocalAddress()).getPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return localContext.getChannel().getLocalAddress();
        }

        @Override
        public int getPort() {
            return ((InetSocketAddress) localContext.getChannel().getRemoteAddress()).getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return localContext.getChannel().getRemoteAddress();
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
            return localContext.getChannel().isBound();
        }

        @Override
        public boolean isClosed() {
            if (localContext.getChannel() == null) {
                return true;
            } else {
                return !localContext.getChannel().isOpen();
            }
        }

        @Override
        public boolean isConnected() {
            return localContext.getChannel().isConnected();
        }

        @Override
        public void receive(DatagramPacket dp) {
            ByteBuffer buffer = ByteBuffer.allocate(102400);
            SocketAddress address = outer.receive(buffer);
            if (address != null) {
                dp.setData(
                        buffer.array(),
                        buffer.arrayOffset(),
                        buffer.remaining());
                dp.setSocketAddress(address);
            }
        }

        @Override
        public void send(DatagramPacket dp) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(dp.getLength());
            bb.put(dp.getData(), dp.getOffset(), dp.getLength());
            outer.send(bb,dp.getSocketAddress());
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
    }

    private static class BytesAvailableEventImpl implements BytesAvailableEvent {
        private static final long serialVersionUID = 5561852445673815517L;

        private final StunSocketChannel thisChannel;

        public BytesAvailableEventImpl(StunSocketChannel thisChannel) {
            this.thisChannel = thisChannel;
        }

        @Override
        public StunSocketChannel getChannel() {
            return thisChannel;
        }
    }
}
