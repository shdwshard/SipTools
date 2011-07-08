/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;

/**
 *
 * @author shdwshard
 */
class IcedRTPConnectorComponent implements RTPConnector {

    private final BidirectionalStreamSocket rtpSocket;
    private final BidirectionalStreamSocket rtcpSocket;
    
    /**
     * Maximum of 64 packets in the buffer at any one time.
     */
    private final int QUEUE_MAX_SIZE = 64;

    public IcedRTPConnectorComponent(IceSocket socket) {
        rtpSocket = new BidirectionalStreamSocket(socket, (short) 0);
        rtcpSocket = new BidirectionalStreamSocket(socket, (short) 1);
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

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void pushBuffer(ByteBuffer buffer, short channelNum) {
        if (channelNum == 0) {
            rtpSocket.pushBuffer(buffer);
        } else {
            rtcpSocket.pushBuffer(buffer);
        }
    }

    class BidirectionalStreamSocket implements PushSourceStream,
            OutputDataStream {

        final private Queue<ByteBuffer> packetQueue =
                new LinkedList<ByteBuffer>();
        private IceSocket socket;
        private SourceTransferHandler sourceHandler;
        private final ContentDescriptor contentDescriptor;
        private final short componentId;

        private BidirectionalStreamSocket(IceSocket socket, short componentId) {
            this(socket, componentId, null);
        }

        private BidirectionalStreamSocket(IceSocket socket, short componentId,
                ContentDescriptor contentDescriptor) {
            this.socket = socket;
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
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "No data available reading from {0}", this);
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
            if (this.sourceHandler != null) {
                Logger.getLogger(getClass().getName()).severe("Transfer Handler is being replaced!");
            }
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
        public synchronized int write(byte[] buffer, int offset, int length) {
            ByteBuffer bb = ByteBuffer.wrap(buffer, offset, length);
            for (IcePeer peer : socket.getPeers()) {
                try {
                    peer.getChannels(socket).get(componentId).write(bb);
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Exception writing to a socket channel", ex);
                }
            }
            return length;
        }

        /**
         * Gets the underlying socket of this stream
         * @return the underlying ICE socket
         */
        protected IceSocket getSocket() {
            return socket;

        }

        public void pushBuffer(ByteBuffer inputBuffer) {
            ByteBuffer buffer = ByteBuffer.allocate(inputBuffer.remaining());
            inputBuffer.mark();
            buffer.put(inputBuffer);
            inputBuffer.reset();
            buffer.flip();
            if (buffer.remaining() == 0) {
                Logger.getLogger(getClass().getName()).log(
                        Level.SEVERE,
                        "Got a null buffer in IcedRTPConnectorComponent.");
            } else {
                // Get object's monitor since we're changing it.
                synchronized (packetQueue) {
                    // Add the packet to the queue
                    packetQueue.add(buffer);
                    
                    // Do length checking on the buffer
                    while (packetQueue.size() > QUEUE_MAX_SIZE) {
                        packetQueue.poll();
                    } 
                    // Notify all listeners in case someone is waiting for data
                    packetQueue.notifyAll();
                }
                // If there's a source handler set, notify them too
                // Do this outside synchronized block to avoid potential deadlocks
                if (sourceHandler != null) {
                    sourceHandler.transferData(this);
                }
            }

        }
    }
}
