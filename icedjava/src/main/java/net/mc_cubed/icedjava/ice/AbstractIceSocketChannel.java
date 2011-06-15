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
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import net.mc_cubed.icedjava.ice.event.IceEvent;
import net.mc_cubed.icedjava.ice.event.IceEventListener;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.event.BytesAvailableEvent;
import net.mc_cubed.icedjava.stun.event.StunEvent;
import net.mc_cubed.icedjava.stun.event.StunEventListener;

/**
 *
 * @author Charles Chappell
 * @since 1.0
 */
class AbstractIceSocketChannel implements IceSocketChannel, StunEventListener {

    @Inject
    Event<IceEvent> eventBroadcaster;
    protected final IceStateMachine peer;
    Queue<PeerAddressedByteBuffer> queue = new LinkedBlockingQueue<PeerAddressedByteBuffer>();
    private final static Logger log = Logger.getLogger(AbstractIceSocketChannel.class.getName());
    Set<IceEventListener> listeners = new HashSet<IceEventListener>();
    final IceSocket socket;
    final short component;

    public AbstractIceSocketChannel(IceStateMachine peer, IceSocket socket, short componentId) {
        this.peer = peer;
        this.socket = socket;
        this.component = componentId;
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        return socket.write(bb, component);
    }

    @Override
    public int send(ByteBuffer src, IcePeer target) throws IOException {
        return socket.sendTo(target, component, src);
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
    public int read(ByteBuffer bb) throws IOException {
        receive(bb);
        return bb.remaining();
    }

    private void fireEvent(IceEvent event) {
        if (eventBroadcaster != null) {
            eventBroadcaster.fire(event);
        }

        for (IceEventListener listener : listeners) {
            try {
                listener.iceEvent(event);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Caught an exception during event processing", ex);

            }
        }
    }

    @Override
    public void stunEvent(StunEvent event) {
        if (event instanceof BytesAvailableEvent) {
            BytesAvailableEvent bytesEvent = (BytesAvailableEvent) event;

            ByteBuffer buffer = ByteBuffer.allocate(StunUtil.MAX_PACKET_SIZE);
            SocketAddress address = bytesEvent.getChannel().receive(buffer);

            IcePeer clientPeer = peer.translateSocketAddressToPeer(address, socket, component);
            queue.offer(new PeerAddressedByteBuffer(clientPeer, buffer));

            fireEvent(new BytesAvailableEventImpl(this));
        }
    }

    @Override
    public IcePeer receive(ByteBuffer dst) throws IOException {
        PeerAddressedByteBuffer inBuffer = queue.poll();

        dst.put(inBuffer.getBuffer());
        dst.flip();
        return inBuffer.getPeer();
    }

    @Override
    public boolean isOpen() {
        return peer.getIceStatus() == IceStatus.SUCCESS;
    }

    @Override
    public void close() throws IOException {
        peer.close();
    }

    @Override
    public long read(ByteBuffer[] bbs, int off, int len) throws IOException {
        long readBytes = 0;
        for (int i = off; i < off + len; i++) {
            readBytes += read(bbs[i]);
        }

        return readBytes;
    }

    @Override
    public long read(ByteBuffer[] bbs) throws IOException {
        return read(bbs, 0, bbs.length);
    }

    @Override
    public long write(ByteBuffer[] bbs, int off, int len) throws IOException {
        long writtenLength = 0;
        for (int i = off; i < off + len; i++) {
            writtenLength += write(bbs[i]);
        }

        return writtenLength;

    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        return write(bbs, 0, bbs.length);
    }
}
