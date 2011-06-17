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
 *
 */
package net.mc_cubed.icedjava.ice;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SessionDescription;

/**
 * The public interface representing a Peer in ICE. This is the most actively
 * used interface in the entire system.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface IcePeer extends SDPListener {

    SessionDescription createOffer() throws SdpException;

    String getLocalPassword();

    String getLocalUFrag();

    /**
     * Gets the global ICE attributes for an ice session, including the
     * Ice-Supports, UFrag and Password attributes
     * @return Attributes to include in SDP offer.
     */
    List<Attribute> getGlobalAttributes() throws SdpException;

    /**
     * Get media descriptions to include in an SDP offer to this peer.
     * @return MediaDescriptions to include in an SDP offer to peer.
     */
    List<MediaDescription> getMediaDescriptions() throws SdpException;

    /**
     * Get the nominated candidates for each socket connected to this peer
     * @return A map of nominated candidates for each socket
     */
    Map<IceSocket, List<CandidatePair>> getNominated();

    /**
     * Get the value of peerId
     *
     * @return the value of peerId
     */
    String getPeerId();

    /**
     * Get the remote password for this peer
     * @return the currently known remote password for this peer
     */
    String getRemotePassword();

    /**
     * Get the remote user fragment for this peer
     * @return the currently known remote ufrag for this peer
     */
    String getRemoteUFrag();

    /**
     * Returns the overall status of ICE processing for this peer
     * @return
     */
    IceStatus getStatus();

    /**
     * Gets the tie breaker used in case of an ICE Role collision.
     * @return the tieBreaker
     */
    long getTieBreaker();

    /**
     * Sets or updates the remote password in use by this peer
     * @param remotePassword new remote password for peer
     */
    void setRemotePassword(String remotePassword);

    /**
     * Sets or updates the remote uFrag in use by this peer
     * @param remoteUFrag new remote uFrag for peer
     */
    void setRemoteUFrag(String remoteUFrag);

    /**
     * Sets an SDP Listener for this peer, which can be used to send updated
     * offers as part of the ICE process
     * @param sdpListener
     */
    public void setSdpListener(SDPListener sdpListener);

    /**
     * Start ICE processing.
     */
    public void start();

    /**
     * Is this peer locally in control of ICE processing (The active peer)
     * @return True if local peer is active, false if remote peer is active.
     */
    public boolean isLocalControlled();

    /**
     * Change the localControlled flag.
     * <br/>
     * In general you should NOT update this manually, but let the ICE process
     * sort out which is the controlling peer.
     * @param localControl
     */
    public void setLocalControlled(boolean localControl);

    /**
     * Are we in an SDP timeout state, waiting for a reply from the remote peer?
     * @return true if waiting, false otherwise
     */
    public boolean isSdpTimeout();

    /**
     * Update the SDP Timeout flag.
     * <br/>
     * In general, you should NOT update this manually, but leave it to the ICE
     * process.
     * @param timeout
     */
    public void setSdpTimeout(boolean timeout);

    /**
     * Reset ICE processing, and set the localControl flag
     * @param localControl
     */
    public void doReset(boolean localControl);

    /**
     * Close this peer connection and disconnect from sockets
     */
    public void close();

    /**
     * Get a list of channels that can be used to send data to this peer
     * @return
     */
    public List<IceSocketChannel> getChannels(IceSocket socket);

    /**
     * Does this peer have a matching remote address for the given socket address?
     * @param address Socket address to test
     * @param socket Socket to match against, or null to test all
     * @param componentId component id to match against or null if none
     * @return true if the given remote address matches this peer
     */
    public boolean hasRemoteAddress(SocketAddress address, IceSocket socket, Short componentId);
}
