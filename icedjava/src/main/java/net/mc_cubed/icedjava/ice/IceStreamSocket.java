/*
 * Copyright 2010 Charles Chappell.
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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.sdp.Media;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.TCPSocketType;
import net.mc_cubed.icedjava.stun.TransportType;
import net.mc_cubed.icedjava.util.ExpiringCache;

/**
 * An initial implementation of ICE-TCP<br/><br/>
 * <strong>WARNING: ICE-TCP is a WORK IN PROGRESS</strong> and carries the
 * following notice: <i>Internet-Drafts are draft documents valid for a maximum of
 * six months and may be updated, replaced, or obsoleted by other documents at
 * any time.  It is inappropriate to use Internet-Drafts as reference
 * material or to cite them other than as "work in progress."</i>
 * 
 * @author Charles Chappell
 * @since 1.0
 */
class IceStreamSocket implements IceSocket {
    
    private final InetSocketAddress stunServer;
    private Map<String, IcePeer> _peers = new HashMap<String, IcePeer>();
    private final static Logger log =
            Logger.getLogger(IceStreamSocket.class.getName());
    private Short components;
    protected Media media;
    public static final String PROP_MEDIA = "media";
    ExpiringCache<SocketAddress, IcePeer> socketCache = new ExpiringCache<SocketAddress, IcePeer>();
    TCPSocketType tcpSocketType;
       
    protected IceStreamSocket(Media media) {
        this(StunUtil.getCachedStunServerSocket(),media);
        
    }
    protected IceStreamSocket(InetSocketAddress stunServer)
            throws SocketException, IOException, InterruptedException {
        this(stunServer, (short) 1);
    }

    protected IceStreamSocket(InetSocketAddress stunServer, short components)
            throws SocketException {
        this.components = components;
        this.stunServer = stunServer;
    }

    protected IceStreamSocket(InetSocketAddress stunServer,Media media) {
        try {
            this.media = media;
            this.components = (short)media.getPortCount();
            this.stunServer = stunServer;
        } catch (SdpParseException ex) {
            throw new java.lang.IllegalArgumentException("Got an exception determining port count of the media",ex);
        }
        
    }

    @Override
    public Media getMedia() throws SdpException {
        return media;
    }

    @Override
    public void setMedia(Media media) {
        this.media = media;
    }

    /**
     * Close all the actual sockets attached to this ICE socket
     */
    @Override
    public void close() {
        for (IcePeer peer : getPeers()) {
            peer.close();
        }
    }

    @Override
    public Collection<IcePeer> getPeers() {
        return this._peers.values();
    }

    @Override
    public boolean isOpen() {
        return (!getPeers().isEmpty());
    }

    @Override
    public short getComponents() {
        return this.components;
    }

    @Override
    public boolean isClosed() {
        return !isOpen();
    }

    @Override
    public TransportType getTransport() {
        return TransportType.TCP;
    }

    @Override
    public TCPSocketType getTcpSocketType() {
        return tcpSocketType;
    }

    
}
