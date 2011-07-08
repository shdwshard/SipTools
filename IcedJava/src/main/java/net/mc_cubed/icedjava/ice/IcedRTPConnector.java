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
import javax.sdp.SdpParseException;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Set;
import javax.media.rtp.RTPConnector;
import javax.sdp.Media;
import net.mc_cubed.icedjava.ice.event.IceBytesAvailableEvent;
import net.mc_cubed.icedjava.ice.event.IceEvent;
import net.mc_cubed.icedjava.ice.event.IceEventListener;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.util.WeakHashSet;

/**
 * IcedRTPConnector implements a RTP Connector implementation that leverages the
 * IcedJava library to deliver RTP/RTCP media streams between potentially NATed
 * peers, abstracting away most of the difficulty that entails.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class IcedRTPConnector extends IceDatagramSocket
        implements IceSocket, IceEventListener {

    public static final int MAX_RTP_PACKET_SIZE = 4096;
    private final Set<IcedRTPConnectorComponent> bidirectionalConnectors = new WeakHashSet<IcedRTPConnectorComponent>();

    public RTPConnector getReceivingConnector() {
        // TODO: Implement a receiver (RTP data + RTCP Sender reports incoming)
        return getBidirectionalConnector();
    }

    public RTPConnector getSendingConnector() {
        // TODO: Implement a sender (RTCP Receiver reports incoming)
        return getBidirectionalConnector();
    }

    public RTPConnector getBidirectionalConnector() {
        IcedRTPConnectorComponent connector = new IcedRTPConnectorComponent(this);
        bidirectionalConnectors.add(connector);

        return connector;
    }

    @Override
    public void iceEvent(IceEvent event) {
        if (event instanceof IceBytesAvailableEvent) {
            IceBytesAvailableEvent bytesEvent = (IceBytesAvailableEvent) event;
            ByteBuffer buffer = ByteBuffer.allocate(StunUtil.MAX_PACKET_SIZE);
            try {
                bytesEvent.getSocketChannel().read(buffer);
                short channelNum;
                // If only one component, do RTCP differentiation, otherwise use the channel number
                if (this.getComponents() == 1) {
                    channelNum = (isRTCP(buffer)) ? (short) 1 : (short) 0;
                } else {
                    channelNum = (short) event.getIcePeer().getChannels(this).indexOf(bytesEvent.getSocketChannel());
                }
                for (IcedRTPConnectorComponent connector : bidirectionalConnectors) {
                    connector.pushBuffer(buffer, channelNum);
                }
            } catch (IOException ex) {
                Logger.getLogger(IcedRTPConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean isRTCP(ByteBuffer buffer) {
        // Mark where the buffer is now
        buffer.mark();
        
        try {
            // The type field, which can be used to differentiate RTP from RTCP
            switch (buffer.get(1)) {
                case (byte) 0xc0: // FIR, full INTRA-frame request.
                case (byte) 0xc1: // NACK, negative acknowledgement
                case (byte) 0xc2: // SMPTETC, SMPTE time-code mapping.
                case (byte) 0xc3: // IJ, extended inter-arrival jitter report.

                case (byte) 0xc8: // SR, sender report.
                case (byte) 0xc9: // RR, receiver report.
                case (byte) 0xca: // SDES, source description.
                case (byte) 0xcb: // BYE, goodbye.
                case (byte) 0xcc: // APP, application defined.

                case (byte) 0xcd: // RTPFB, Generic RTP Feedback.
                case (byte) 0xce: // PSFB, Payload-specific.
                case (byte) 0xcf: // XR, RTCP extension.
                case (byte) 0xd0: // AVB, AVB RTCP packet.
                case (byte) 0xd1: // RSI, Receiver Summary Information.

                    /**
                     * Matched an RTCP type we were looking for, so this is most
                     *  likely an RTCP packet, not an RTP packet.
                     */ 
                    
                    return true;
                default:
                    return false;
            }
        } finally {
            // Make sure we reset to avoid upsetting the buffer position
            buffer.reset();
        }
    }

    public IcedRTPConnector() throws SocketException, SdpParseException {
        super((short) 2);
    }

    public IcedRTPConnector(Media media) throws SocketException, SdpParseException {
        super(media);

    }

    @Override
    protected void addPeer(IcePeer peer) {
        super.addPeer(peer);
        peer.getChannels(this).get(0).addEventListener(this);
        if (peer.getChannels(this).size() > 1) {
            peer.getChannels(this).get(1).addEventListener(this);
        }
    }

    @Override
    protected void removePeer(IcePeer peer) {
        super.removePeer(peer);
        peer.getChannels(this).get(0).removeEventListener(this);
        if (peer.getChannels(this).size() > 1) {
            peer.getChannels(this).get(1).removeEventListener(this);
        }
    }
}
