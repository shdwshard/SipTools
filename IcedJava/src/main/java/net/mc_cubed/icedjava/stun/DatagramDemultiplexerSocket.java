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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.stun.event.DemultiplexedBytesAvailableEvent;
import net.mc_cubed.icedjava.stun.event.StunEvent;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import net.mc_cubed.icedjava.util.AddressedByteBuffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

/**
 * A datagram socket that can be used for STUN testing, or sending and receiving
 * data, or both at the same time. The socket implementation takes care of
 * separating the two types of data.
 *
 * @author Charles Chappell
 * @since 0.9
 */
class DatagramDemultiplexerSocket extends DatagramStunSocket implements DemultiplexerSocket, StunSocketChannel {

    boolean nonBlocking = false;
    private DatagramStunSocketBridge socket = null;
    final protected Queue<AddressedByteBuffer> bufferQueue = new ConcurrentLinkedQueue<AddressedByteBuffer>();
    final protected HashSet<StunEventListener> listeners = new HashSet<StunEventListener>();
    @Inject
    Event<StunEvent> eventBroadcaster;

    protected DatagramDemultiplexerSocket(StunEventListener stunEventListener) {
        if (stunEventListener != null) {
            listeners.add(stunEventListener);
        }
    }

    @Override
    public NextAction handleRead(FilterChainContext e) throws IOException {
        if (e.getMessage() instanceof StunPacket) {
            return super.handleRead(e);
        } else if (e.getMessage() instanceof ByteBuffer) {
            log.log(Level.FINER, "Got a data packet of length {0} from peer {1}", new Object[]{((ByteBuffer)e.getMessage()).remaining(), e.getAddress()});
            ByteBuffer cb = (ByteBuffer) e.getMessage();
            bufferQueue.add(new AddressedByteBuffer((SocketAddress) e.getAddress(), cb));
            broadcastReceivedMessage();
            return e.getStopAction();
        } else {
            log.log(Level.WARNING, "Got a packet of unknown type {0} from peer {1}", new Object[]{e.getMessage().getClass().getName(), e.getAddress()});
            return e.getInvokeAction();
        }
    }

    
    @Override
    public TransportType getTransportType() {
        return TransportType.UDP;
    }

    @Override
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
        bb.flip();
        return bb.remaining();
    }

    @Override
    public boolean isOpen() {
        return connection.get() != null ? connection.get().isOpen() : false;
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        int bytes = bb.remaining();
        connection.get().write(bb);
        return bytes;

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
        return read(bbs, 0, bbs.length);
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
        return write(bbs, 0, bbs.length);
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) {
        AddressedByteBuffer packet = bufferQueue.poll();

        if (packet != null) {
            // Put the contents of our buffer into the destination buffer
            dst.put(packet.getBuffer());
            // Flip the buffer for reading
            dst.flip();
            // Return the source address
            return packet.getAddress();
        } else {
            // Flip the buffer so it looks empty if the caller tries to read it.
            dst.flip();
            // Return null to indicate no packet read.
            return null;
        }
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        int remainingBytes = src.remaining();
        connection.get().write(target, src, null);
        return remainingBytes;
    }

    @Override
    public void registerStunEventListener(StunEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setStunEventListener(StunEventListener listener) {
        listeners.clear();
        listeners.add(listener);
    }

    @Override
    public void deregisterStunEventListener(StunEventListener listener) {
        listeners.remove(listener);
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

    @Override
    public ServerSocket getServerSocket() {
        throw new UnsupportedOperationException("Datagram Socket does not support this operation");
    }

    @Override
    public Socket getSocket() {
        throw new UnsupportedOperationException("Datagram Socket does not support this operation");
    }

    @Override
    public TCPSocketType getTcpSocketType() {
        throw new UnsupportedOperationException("Not valid for a datagram socket.");
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
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public void connect(InetAddress address, int port) {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public void connect(SocketAddress sa) throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public void disconnect() {
            try {
                connection.get().close().get();
            } catch (InterruptedException ex) {
                Logger.getLogger(DatagramDemultiplexerSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(DatagramDemultiplexerSocket.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DatagramDemultiplexerSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            return ((InetSocketAddress) connection.get().getPeerAddress()).getAddress();
        }

        @Override
        public InetAddress getLocalAddress() {
            return ((InetSocketAddress) connection.get().getLocalAddress()).getAddress();
        }

        @Override
        public int getLocalPort() {
            return ((InetSocketAddress) connection.get().getLocalAddress()).getPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return (SocketAddress) connection.get().getLocalAddress();
        }

        @Override
        public int getPort() {
            return ((InetSocketAddress) connection.get().getPeerAddress()).getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return (SocketAddress) connection.get().getPeerAddress();
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
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isClosed() {
            throw new UnsupportedOperationException("Not Implemented");
        }

        @Override
        public boolean isConnected() {
            throw new UnsupportedOperationException("Not Implemented");
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
            outer.send(bb, dp.getSocketAddress());
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

    private static class BytesAvailableEventImpl implements DemultiplexedBytesAvailableEvent {

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
