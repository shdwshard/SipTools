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
package net.mc_cubed.icedjava.ice;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.SourceTransferHandler;
import javax.sdp.SdpParseException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;
import javax.sdp.Media;
import net.mc_cubed.icedjava.ice.event.IceBytesAvailableEvent;
import net.mc_cubed.icedjava.ice.event.IceEvent;
import net.mc_cubed.icedjava.ice.event.IceEventListener;

/**
 * IcedRTPConnector implements a RTP Connector implementation that leverages the
 * IcedJava library to deliver RTP/RTCP media streams between potentially NATed
 * peers, abstracting away most of the difficulty that entails.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class IcedRTPConnector extends IceDatagramSocket
        implements RTPConnector, IceSocket {

    private final BidirectionalStreamSocket rtpSocket;
    private final BidirectionalStreamSocket rtcpSocket;

    public IcedRTPConnector(InetSocketAddress stunServer, Media media, InetAddress defaultIP) throws SocketException, SdpParseException {
        super(stunServer, media);

        rtpSocket = new BidirectionalStreamSocket((short) 0);
        if (media.getPortCount() >= 2) {
            rtcpSocket = new BidirectionalStreamSocket((short) 1);
        } else {
            rtcpSocket = rtpSocket;
        }
    }

    @Override
    protected void addPeer(IcePeer peer) {
        super.addPeer(peer);
        peer.getChannels(this).get(0).addEventListener(rtpSocket);
        if (peer.getChannels(this).size() > 1) {
            peer.getChannels(this).get(1).addEventListener(rtcpSocket);
        }
    }

    @Override
    protected void removePeer(IcePeer peer) {
        super.removePeer(peer);
        peer.getChannels(this).get(0).removeEventListener(rtpSocket);
        if (peer.getChannels(this).size() > 1) {
            peer.getChannels(this).get(1).removeEventListener(rtcpSocket);
        }
    }
    
    
    /* 
     * Closes the open streams associated with all remote endpoints that have been added previously by subsequent addTarget() calls.
     */
    public void removeTargets(java.lang.String reason) {
        for (IcePeer peer : rtpSocket.getSocket().getPeers()) {
            rtpSocket.getSocket().removePeer(peer);
        }
        for (IcePeer peer : rtcpSocket.getSocket().getPeers()) {
            rtcpSocket.getSocket().removePeer(peer);
        }

    }

    // RTPConnector interface methods
    @Override
    public PushSourceStream getDataInputStream() throws IOException {
        return rtpSocket;
    }

    @Override
    public OutputDataStream getDataOutputStream() throws IOException {
        return rtpSocket;
    }

    @Override
    public PushSourceStream getControlInputStream() throws IOException {
        return rtcpSocket;
    }

    @Override
    public OutputDataStream getControlOutputStream() throws IOException {
        return rtcpSocket;
    }

    @Override
    public void setReceiveBufferSize(int arg0) throws IOException {
        // Do Nothing
    }

    @Override
    public int getReceiveBufferSize() {
        // Get the receive buffer size set on the RTP data channel. Return -1 if the receive buffer size is not applicable for the implementation.
        return -1;
    }

    @Override
    public void setSendBufferSize(int arg0) throws IOException {
        // Do Nothing
    }

    @Override
    public int getSendBufferSize() {
        // Get the send buffer size set on the RTP data channel. Return -1 if the send buffer size is not applicable for the implementation
        return -1;
    }

    @Override
    public double getRTCPBandwidthFraction() {
        // This value is used to initialize the RTPManager. Check RTPManager for more detauls. Return -1 to use the default values.
        return -1.0f;
    }

    @Override
    public double getRTCPSenderBandwidthFraction() {
        // This value is used to initialize the RTPManager. Check RTPManager for more detauls. Return -1 to use the default values.
        return -1.0f;
    }

    class BidirectionalStreamSocket implements PushSourceStream,
            OutputDataStream, IceEventListener {

        final private Queue<ByteBuffer> packetQueue =
                new LinkedList<ByteBuffer>();
        private IceDatagramSocket socket;
        private SourceTransferHandler sourceHandler;
        private final ContentDescriptor contentDescriptor;
        private final short componentId;
        private final int MAX_PACKET_SIZE = 4096;

        private BidirectionalStreamSocket(short componentId) {
            this(componentId, null);
        }

        private BidirectionalStreamSocket(short componentId,
                ContentDescriptor contentDescriptor) {
            this.socket = IcedRTPConnector.this;
            this.contentDescriptor = contentDescriptor;
            this.componentId = componentId;            
        }

        /*********************************************
         * PushSourceStream interface implementation *
         *********************************************/
        /**
         * Read from the stream without blocking. Returns -1 when the end of the media is reached.
         * @param outBuffer The buffer to read bytes into.
         * @param offset The offset into the buffer at which to begin writing data.
         * @param length The number of bytes to read.
         * @return The number of bytes read or -1 when the end of stream is reached.
         * @throws IOException Thrown if an error occurs while reading
         */
        @Override
        public int read(byte[] outBuffer, int offset, int length) throws IOException {
            // Get the monitor since we're about to do something sensitive to 
            // changes in the queue
            synchronized (packetQueue) {
                // Basic Sanity checks, do these before we remove the packet from
                // the queue.  This is why we need the monitor.
                ByteBuffer inBuffer = packetQueue.peek();
                if (inBuffer != null) {
                    if (length < inBuffer.remaining()) {
                        throw new IOException("Packet size greater than buffer length");
                    }
                    if (outBuffer.length - offset < length) {
                        throw new IOException("Buffer Size Overrun");
                    }

                    // Get the packet we just peeked at
                    inBuffer = packetQueue.poll();
                    // Copy the data
                    try {
                        System.arraycopy(inBuffer.array(), inBuffer.arrayOffset() + inBuffer.position(), outBuffer, offset, inBuffer.remaining());
                    } catch (Exception ex) {
                        // Replace the packet in the queue if an exception occurs
                        ((LinkedList) packetQueue).addFirst(inBuffer);
                        throw new IOException("Exception thrown while copying data into read buffer", ex);
                    }
                    return inBuffer.remaining();
                } else {
                    return 0;
                }
            }

        }

        /**
         * Determine the size of the buffer needed for the data transfer.
         * This method is provided so that a transfer handler can determine how
         * much data, at a minimum, will be available to transfer from the source.
         * Overflow and data loss is likely to occur if this much data isn't read
         * at transfer time.
         * @return The size of the data transfer.
         */
        @Override
        public int getMinimumTransferSize() {
            ByteBuffer packet = packetQueue.peek();
            if (packet != null) {
                return packet.remaining();
            } else {
                return -1;
            }
        }

        /**
         * Register an object to service data transfers to this stream.
         * If a handler is already registered when setTransferHandler is called,
         * the handler is replaced; there can only be one handler at a time.
         * @param transferHandler The handler to transfer data to.
         */
        @Override
        public void setTransferHandler(SourceTransferHandler transferHandler) {
            this.sourceHandler = transferHandler;
        }

        /**
         * Get the current content type for this stream.
         * @return The current ContentDescriptor for this stream.
         */
        @Override
        public ContentDescriptor getContentDescriptor() {
            return contentDescriptor;
        }

        /**
         * Get the size, in bytes, of the content on this stream. LENGTH_UNKNOWN is returned if the length is not known.
         * @return The content length in bytes.
         */
        @Override
        public long getContentLength() {
            return LENGTH_UNKNOWN;
        }

        /**
         * Find out if the end of the stream has been reached.
         * @return
         */
        @Override
        public boolean endOfStream() {
            return socket.isClosed();
        }

        /**
         * Obtain the collection of objects that control the object that implements this interface.
        
         * If no controls are supported, a zero length array is returned.
         * @return the collection of object controls
         */
        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        /**
         * Obtain the object that implements the specified Class or Interface The full class or interface name must be used.
         *
         * If the control is not supported then null is returned.
         * @param controlType
         * @return the object that implements the control, or null.
         */
        @Override
        public Object getControl(String controlType) {
            return null;
        }

        /*********************************************
         * OutputDataStream interface implementation *
         *********************************************/
        /**
         * Write data to the underlying network. Data is copied from the buffer
         * starting af offset. Number of bytes copied is length
         * @param buffer The buffer from which data is to be sent out on the network.
         * @param offset The offset at which data from buffer is copied over
         * @param length The number of bytes of data copied over to the network.
         * @return
         */
        @Override
        @SuppressWarnings("CallToThreadDumpStack")
        public int write(byte[] buffer, int offset, int length) {
            ByteBuffer bb = ByteBuffer.wrap(buffer, offset, length);
            for (IcePeer peer : socket.getPeers()) {
                try {
                    peer.getChannels(socket).get(componentId).write(bb);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return -1;
                }
            }
            return length;
        }

        /**
         * Gets the underlying socket of this stream
         * @return the underlying ICE socket
         */
        protected IceDatagramSocket getSocket() {
            return socket;

        }

        @Override
        public void iceEvent(IceEvent event) {
            if (event instanceof IceBytesAvailableEvent) {
                try {
                    IceBytesAvailableEvent bytesEvent = (IceBytesAvailableEvent) event;
                    ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
                    bytesEvent.getSocketChannel().read(buffer);
                    // Get object's monitor since we're changing it.
                    synchronized (packetQueue) {
                        // Add the packet to the queue
                        packetQueue.add(buffer);
                        // Notify all listeners in case someone is waiting for data
                        packetQueue.notifyAll();
                    }
                    // If there's a source handler set, notify them too
                    // Do this outside synchronized block to avoid potential deadlocks
                    if (sourceHandler != null) {
                        sourceHandler.transferData(this);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(IcedRTPConnector.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
