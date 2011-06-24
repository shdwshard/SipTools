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
package net.mc_cubed.icedjava.stun;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
 * Abstract base class for Stun Sockets
 *
 * @author Charles Chappell
 * @since 1.0
 */
abstract class AbstractStunSocket extends BaseFilter implements StunSocketChannel {

    protected static Logger log = Logger.getLogger(DatagramStunSocket.class.getName());
    static ExpiringCache<BigInteger, StunReplyFuture> requestCache = new ExpiringCache<BigInteger, StunReplyFuture>();
    protected volatile WeakReference<FilterChain> filterChain;
    protected volatile WeakReference<Connection<SocketAddress>> connection;
    boolean nonBlocking = false;
    final protected Queue<AddressedByteBuffer> bufferQueue = new ConcurrentLinkedQueue<AddressedByteBuffer>();
    final protected HashSet<StunEventListener> listeners = new HashSet<StunEventListener>();
    @Inject
    Event<StunEvent> eventBroadcaster;

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

    protected static class BytesAvailableEventImpl implements DemultiplexedBytesAvailableEvent {

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
