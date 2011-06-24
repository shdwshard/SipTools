package net.mc_cubed.msrp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.sdp.MediaDescription;

/**
 * Copyright 2010 Charles Chappell.
 *
 * This file is part of MsrpJava.
 *
 * MsrpJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * MsrpJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MsrpJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Charles Chappell <shdwshard@me.com>
 * @version 2010.1112
 * @since 1.0
 */
public interface MsrpPeer {

    /**
     * Add an EventListener class to listen for peer related events such as
     * message and report events.
     * <p>
     * Alternatively, this events can be observed via WELD annotations
     *
     * @param listener Adds a class implementing an {@link MsrpEventListner}
     * interface to the event listener tree of this MsrpPeer.
     *
     * @see MsrpEventListener
     */
    void addEventListener(MsrpEventListener listener);

    /**
     * Remove an event listener
     *
     * @param listener Removes a {@link MsrpEventListener} from the event
     * listener tree
     */
    void removeEventListener(MsrpEventListener listener);

    /**
     * Queries whether the local peer is the active participant in this MSRP
     * connection.
     * 
     * @return true if the local peer will connect to this remote peer, false
     * otherwise.
     */
    boolean isActive();

    /**
     * Sets whether this is the active MSRP peer.  Active peers establish the
     * connection, while a passive peer waits for incoming connections.
     * This only has meaning for a Local peer, and is ignored for remote peers.
     * <p>
     * Generally speaking, in a Client-Server scenario, the server is passive
     * and the client is active.
     * <p>
     * Per {@linkplain   RFC 4975 Section 5.4:
     * <p>
     * When a new MSRP session is created, the initiating endpoint MUST act
     * as the "active" endpoint, meaning that it is responsible for opening the
     * transport connection to the answerer, if a new connection is required.
     * However, this requirement MAY be weakened if a standardized mechanisms
     * for negotiating the connection direction become available and are
     * implemented by both parties to the connection.
     */
    void setActive(boolean active);

    /**
     * Send a simple text message, with whatever encryption or encapsulation has
     * already been set on this peer.
     * 
     * @param message
     * @return
     */
    boolean sendMessage(URI toPath, String message) throws IOException ;

    /**
     * Send a simple text message, with whatever encryption or encapsulation has
     * already been set on this peer.
     *
     * @param message
     * @return
     */
    boolean sendMessage(MsrpMessage message) throws IOException ;
    
    /**
     * Get the MSRP URIs associated with this peer
     *
     * @return an array of MSRP {@link URI}s associated with this peer
     */
    URI[] getMsrpURIs();

    /**
     * Get the MediaDescription associated with this peer
     *
     * @return the {@link MediaDescription} associated with this peer
     */
    MediaDescription getMediaDescription();

    /**
     * Queries whether this is a local or remote peer
     *
     * @return true if peer is local, false if peer is remote.
     */
    boolean isLocalPeer();

    /**
     * Gets the transactionId associated with this peer
     *
     * @return
     */
    String getSessionId();

    /**
     * Gets a list of addresses this socket is bound to.
     *
     * This may be a list of local interfaces, but may also be a list of
     * public facing addresses discovered through STUN or ICE.
     */
    InetSocketAddress[] getAddresses();

    /**
     * Get a known remote peer list for this peer.
     */
    MsrpPeer[] getPeers();

    /**
     * Get a peer identified by a session id.
     * @param sessionId
     * @return
     */
    MsrpPeer getPeer(String sessionId);

    /**
     * Shutdown this socket
     */
    void shutdown();

}
