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
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.mc_cubed.icedjava.ice.event.IceEventListener;
import net.mc_cubed.icedjava.ice.event.IceEvent;
import net.mc_cubed.icedjava.stun.event.StunEvent;
import net.mc_cubed.icedjava.stun.event.StunEventListener;

/**
 * JBoss Netty style Socket Channel for sending media to an IceDatagramSocket
 *
 * @author Charles Chappell
 * @since 1.0
 * @see IceDatagramSocket
 */
class IceDatagramSocketChannel implements IceSocketChannel, StunEventListener {

    protected HashSet<IceEventListener> listeners = new HashSet<IceEventListener>();
    protected final static Logger log = Logger.getLogger(IceDatagramSocketChannel.class.getName());
    protected final IceSocket iceSocket;
    protected Queue<ByteBuffer> queue = new ConcurrentLinkedQueue<ByteBuffer>();
    @Inject
    Event<IceEvent> eventBroadcaster;
    protected final IceStateMachine peer;

    public IcePeer getPeer() {
        return peer;
    }

    /**
     * Get the value of iceSocket
     *
     * @return the value of iceSocket
     */
    public IceSocket getIceSocket() {
        return iceSocket;
    }
    protected final short component;

    /**
     * Get the value of component
     *
     * @return the value of component
     */
    public int getComponent() {
        return component;
    }

    IceDatagramSocketChannel(IceStateMachine peer, IceSocket socket, short channel) {
        this.peer = peer;
        this.iceSocket = socket;
        this.component = channel;
    }

    @Override
    public long write(ByteBuffer[] bbs, int offset, int length) throws IOException {
        long bytesWritten = 0;
        for (int bnum = offset; bnum < length; bnum++) {
            bytesWritten += write(bbs[bnum]);
        }

        return bytesWritten;
    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        return write(bbs, 0, bbs.length);
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        int position = bb.position();
        try {
            CandidatePair pair = peer.nominated.get(iceSocket).get(component);
            pair.localCandidate.socket.send(bb, pair.remoteCandidate.socketAddress);
        } catch (NullPointerException ex) {
            log.log(Level.FINEST, "Socket not fully setup before write submitted.  Data is being lost", ex);
        }
        return bb.position() - position;
    }

    @Override
    public boolean isOpen() {
        return iceSocket.isOpen();
    }

    @Override
    public void close() throws IOException {
        iceSocket.close();
    }

    @Override
    public long read(ByteBuffer[] buffers, int offset, int length) throws IOException {
        int bytesRead = 0;
        for (int bufnum = offset; bufnum < length; bufnum++) {
            int retval = read(buffers[bufnum]);
            if (retval <= 0) {
                break;
            }
            bytesRead += retval;
        }
        return bytesRead;
    }

    @Override
    public long read(ByteBuffer[] bbs) throws IOException {
        return read(bbs, 0, bbs.length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ByteBuffer src = queue.poll();
        dst.put(src);
        dst.flip();
        return dst.remaining();
    }

    @Override
    public void addEventListener(IceEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(IceEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void stunEvent(StunEvent event) {
        if (event instanceof net.mc_cubed.icedjava.stun.event.DemultiplexedBytesAvailableEvent) {
            net.mc_cubed.icedjava.stun.event.DemultiplexedBytesAvailableEvent bytesEvent = (net.mc_cubed.icedjava.stun.event.DemultiplexedBytesAvailableEvent) event;
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            SocketAddress address = bytesEvent.getChannel().receive(buffer);
            queue.add(buffer);
            fireEvent(new BytesAvailableEventImpl(this));
        }
    }

    private void fireEvent(IceEvent iceEvent) {
        if (eventBroadcaster != null) {
            eventBroadcaster.fire(iceEvent);
        }

        for (IceEventListener listener : listeners) {
            try {
                listener.iceEvent(iceEvent);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Caught exception during event listener call", ex);
            }
        }
    }
}
