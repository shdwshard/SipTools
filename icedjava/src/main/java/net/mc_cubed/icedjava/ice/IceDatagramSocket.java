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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.sdp.Media;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.Attribute;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.TransportType;
import net.mc_cubed.icedjava.util.ExpiringCache;
import org.glassfish.grizzly.filterchain.BaseFilter;

/**
 * The name is somewhat misleading since this class doesn't actually implement
 * any sockets, but instead is a representation of a single SDP M line, or a
 * single type of media, even if spread across multiple connections or ports.
 * (for example RTP/RTCP for a single media type)<br/>
 * <br/>
 * This class represents a datagram, or UDP type media type
 *
 * @author Charles Chappell
 * @see IceStreamSocket
 * @see IceSocket
 */
public class IceDatagramSocket extends BaseFilter implements IceSocket {

    private final InetSocketAddress stunServer;
    private Map<String, IcePeer> _peers = new HashMap<String, IcePeer>();
    private final static Logger log =
            Logger.getLogger(net.mc_cubed.icedjava.ice.IceDatagramSocket.class.getName());
    private Short components;
    protected Media media;
    public static final String PROP_MEDIA = "media";
    ExpiringCache<SocketAddress, IcePeer> socketCache = new ExpiringCache<SocketAddress, IcePeer>();

    /**
     * Get the value of media
     *
     * @return the value of media
     */
    @Override
    public Media getMedia() {
        return media;
    }

    /**
     * Set the value of media
     *
     * @param media new value of media
     */
    @Override
    public final void setMedia(Media media) {
        Media oldMedia = this.media;
        this.media = media;

        if (media != null) {
            this.components = null;
        }
        propertyChangeSupport.firePropertyChange(PROP_MEDIA, oldMedia, media);
    }
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Creates a single flow IceDatagramSocket
     * 
     * @param stunServer
     * @throws SocketException
     * @throws IOException
     * @throws InterruptedException
     */
    protected IceDatagramSocket(InetSocketAddress stunServer)
            throws SocketException, IOException, InterruptedException {
        this(stunServer, (short) 1);
    }

    protected IceDatagramSocket(InetSocketAddress stunServer, short components)
            throws SocketException {
        this.stunServer = stunServer;
        this.components = components;
        setMedia(null);
    }

    protected IceDatagramSocket(InetSocketAddress stunServer,
            Media media) throws SocketException, SdpParseException {
        this.stunServer = stunServer;
        setMedia(media);
    }

    protected IceDatagramSocket(Media media)
            throws SocketException, SdpParseException {
        this.stunServer = StunUtil.getCachedStunServerSocket();
        setMedia(media);
    }

    @Override
    final public Collection<IcePeer> getPeers() {
        return getPeerMap().values();
    }

    public Map<String, IcePeer> getPeerMap() {
        return _peers;
    }

    protected void addPeer(IcePeer peer) {
        getPeerMap().put(peer.getLocalUFrag(), peer);
    }

    protected void removePeer(IcePeer peer) {
        getPeerMap().remove(peer.getLocalUFrag());
    }

    final public IcePeer getPeer(String uFrag) {
        return getPeerMap().get(uFrag);
    }

    @Override
    public boolean isClosed() {
        return !isOpen();
    }

    public InetSocketAddress getStunServer() {
        return stunServer;
    }
    protected IceSocketChannel[] socketChannels;
    public static final String PROP_SOCKETCHANNELS = "socketChannels";


    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getComponents() {
        if (components == null) {
            try {
                return (short) media.getPortCount();
            } catch (SdpParseException ex) {
                // This really should never happen, but in case it does, we need
                // to trap it.
                Logger.getLogger(IceDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        } else {
            return components;
        }

    }

    /**
     * Close all the actual sockets attached to this ICE channel
     */
    @Override
    public void close() {
        for (IcePeer peer : getPeers()) {
            peer.close();
        }
    }

    /**
     * Copy the media description
     * @param media the media description
     * @return the copied media description
     * @throws SdpParseException
     */
    public static MediaDescription copyMedia(MediaDescription media)
            throws SdpParseException {
        SdpFactory factory = SdpFactory.getInstance();
        Vector<String> formats = media.getMedia().getMediaFormats(true);
        MediaDescription copiedMedia = factory.createMediaDescription(media.getMedia().getMediaType(),
                media.getMedia().getMediaPort(),
                1, // This is always only one for a single DatagramDemultiplexerSocket
                media.getMedia().getProtocol(),
                formats.toArray(new String[]{}));
        for (Attribute attribute : (Vector<Attribute>) media.getAttributes(true)) {
            copiedMedia.getAttributes(true).add(factory.createAttribute(
                    attribute.getName(),
                    attribute.getValue()));
        }
        return copiedMedia;

    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + hashCode() + "]";
    }
    
    protected IcePeer translateSocketAddressToPeer(SocketAddress address, Short componentId) {
        if (socketCache.containsKey(address)) {
            return socketCache.get(address);
        } else {
            IcePeer peer = null;
            for (IcePeer checkPeer : getPeerMap().values()) {
                if (checkPeer.hasRemoteAddress(address,this,componentId)) {
                    peer = checkPeer;
                    break;
                }
            }
            if (peer != null) {
                socketCache.admit(address, peer);
            }
            return peer;
        }
    }

    @Override
    public TransportType getTransport() {
        return TransportType.UDP;
    }
}
