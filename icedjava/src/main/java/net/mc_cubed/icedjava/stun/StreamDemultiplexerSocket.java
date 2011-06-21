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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.stun.DatagramStunSocket.StunReplyFuture;
import net.mc_cubed.icedjava.stun.event.DemultiplexedBytesAvailableEvent;
import net.mc_cubed.icedjava.stun.event.StunEvent;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import net.mc_cubed.icedjava.util.AddressedByteBuffer;
import net.mc_cubed.icedjava.util.ExpiringCache;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
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
class StreamDemultiplexerSocket extends BaseFilter implements DemultiplexerSocket, StunSocketChannel {

    protected static Logger log = Logger.getLogger(DatagramStunSocket.class.getName());
    static ExpiringCache<BigInteger, StunReplyFuture> requestCache = new ExpiringCache<BigInteger, StunReplyFuture>();
    protected volatile WeakReference<FilterChain> filterChain;
    protected volatile WeakReference<Connection<SocketAddress>> connection;
    boolean nonBlocking = false;
    final protected Queue<AddressedByteBuffer> bufferQueue = new ConcurrentLinkedQueue<AddressedByteBuffer>();
    final protected HashSet<StunEventListener> listeners = new HashSet<StunEventListener>();
    @Inject
    Event<StunEvent> eventBroadcaster;
    private ServerStreamStunSocketBridge serverSocketBridge;
    private StreamStunSocketBridge socketBridge;

    protected StreamDemultiplexerSocket(StunEventListener stunEventListener) {
        if (stunEventListener != null) {
            listeners.add(stunEventListener);
        }
    }

    @Override
    public NextAction handleRead(FilterChainContext e) throws IOException {
        if (e.getMessage() instanceof StunPacket) {
            return super.handleRead(e);
        } else if (e.getMessage() instanceof ByteBuffer) {
            log.log(Level.FINER, "Got a data packet of length {0} from peer {1}", new Object[]{((ByteBuffer) e.getMessage()).remaining(), e.getAddress()});
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
        return TransportType.TCP;
    }

    @Override
    public DatagramSocket getDatagramSocket() throws SocketException {
        throw new UnsupportedOperationException("Stream Socket does not support this operation");
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

        dst.put(packet.getBuffer());
        dst.flip();
        return packet.getAddress();
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
    public ServerSocket getServerSocket() throws IOException{
        if (serverSocketBridge == null) {
            serverSocketBridge = new ServerStreamStunSocketBridge();
        }

        return serverSocketBridge;
    }

    @Override
    public Socket getSocket() throws IOException{
        if (socketBridge == null) {
            socketBridge = new StreamStunSocketBridge();
        }

        return socketBridge;
    }

    @Override
    public InetAddress getLocalAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMaxRetries(int retries) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void storeAndNotify(StunPacket packet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<StunReply> doTest(InetSocketAddress stunServer) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<StunReply> doTest(InetSocketAddress stunServer, StunPacket packet) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setServerConnection(Connection connection) {
        this.connection = new WeakReference<Connection<SocketAddress>>(connection);
    }

    /**
     * A dummy ServerSocket implementation used to allow OIO dependant code to
     * leverage ICE without major code rewriting.
     */
    class StreamStunSocketBridge extends Socket {

        public StreamStunSocketBridge() throws IOException {
            super(new StreamStunSocketBridgeImpl());
        }
    }

    class StreamStunSocketBridgeImpl extends SocketImpl {

        @Override
        protected void create(boolean bln) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void connect(String string, int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void connect(InetAddress ia, int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void connect(SocketAddress sa, int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void bind(InetAddress ia, int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void listen(int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void accept(SocketImpl si) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected int available() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void close() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void sendUrgentData(int i) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOption(int i, Object o) throws SocketException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getOption(int i) throws SocketException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    class ServerStreamStunSocketBridge extends ServerSocket {

        public ServerStreamStunSocketBridge() throws IOException {
            // TODO: Figure out how to make this work in a reasonable way
            throw new UnsupportedOperationException("Not supported yet.");            
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
