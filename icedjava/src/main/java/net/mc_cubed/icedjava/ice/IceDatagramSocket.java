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
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
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
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

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
public class IceDatagramSocket extends SimpleChannelUpstreamHandler implements IceSocket {

    private final InetSocketAddress stunServer;
    private Map<String, IcePeer> _peers = new HashMap<String, IcePeer>();
    private final static Logger log =
            Logger.getLogger(net.mc_cubed.icedjava.ice.IceDatagramSocket.class.getName());
    private Short components;
    protected Media media;
    public static final String PROP_MEDIA = "media";

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

        IceDatagramSocketChannel[] channels = new IceDatagramSocketChannel[getComponents()];
        for (short channel = 0; channel < getComponents(); channel++) {
            channels[channel] = new IceDatagramSocketChannel(this, channel);
        }
        setSocketChannels(channels);
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

    final protected void addPeer(IcePeer peer) {
        getPeerMap().put(peer.getLocalUFrag(), peer);
    }

    final protected void removePeer(IcePeer peer) {
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

    /**
     * Get the value of socketChannels
     *
     * @return the value of socketChannels
     */
    @Override
    public IceSocketChannel[] getSocketChannels() {
        return socketChannels;
    }

    /**
     * Set the value of socketChannels
     *
     * @param socketChannels new value of socketChannels
     */
    public void setSocketChannels(IceSocketChannel[] socketChannels) {
        IceSocketChannel[] oldSocketChannels = this.socketChannels;
        this.socketChannels = socketChannels;
        propertyChangeSupport.firePropertyChange(PROP_SOCKETCHANNELS, oldSocketChannels, socketChannels);
    }

    /**
     * Get the value of socketChannels at specified index
     *
     * @param index
     * @return the value of socketChannels at specified index
     */
    @Override
    public IceSocketChannel getSocketChannel(short index) {
        return this.socketChannels[index];
    }

    /**
     * Set the value of socketChannels at specified index.
     *
     * @param index
     * @param newSocketChannels new value of socketChannels at specified index
     */
    public void setSocketChannels(int index, IceSocketChannel newSocketChannels) {
        IceSocketChannel oldSocketChannels = this.socketChannels[index];
        this.socketChannels[index] = newSocketChannels;
        propertyChangeSupport.fireIndexedPropertyChange(PROP_SOCKETCHANNELS, index, oldSocketChannels, newSocketChannels);
    }

    @Override
    public int receive(DatagramPacket p, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

/*    @Override
    public int send(DatagramPacket p, short componentId) throws IOException {
        int bytesSent = 0;
        for (IcePeer peer : getPeers()) {
            IceSocketChannel sink = peer.getChannels(this).get(componentId);
            ByteBuffer bb = ByteBuffer.allocate(p.getLength());
            bb.put(p.getData(), p.getOffset(), p.getLength());
            sink.write(bb);
        }
        return bytesSent;
    }*/

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

    @Override
    public IcePeer receive(ByteBuffer data, short componentId) throws IOException {
        return getSocketChannel(componentId).receive(data);
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

    /**
     * Sends data not to a specific ip/port, but to a specific peer
     * @param peer the peer to send this data to.
     * @param data the data to send over the channel
     */
    public int sendTo(IcePeer peer, short componentId, byte[] data) throws IOException {
        return sendTo(peer, componentId, data, data.length);
    }

    /**
     * Sends data not to a specific ip/port, but to a specific peer
     * @param peer the peer to send this data to.
     * @param data the data to send over the channel
     * @param length the length of the data in the byte array to use
     */
    public int sendTo(IcePeer peer, short componentId, byte[] data, int length) throws IOException {
        return sendTo(peer, componentId, data, 0, length);
    }

    /**
     * Sends data not to a specific ip/port, but to a specific peer
     * @param peer the peer to send this data to.
     * @param data the data to send over the channel
     * @param offset where to start copying data from
     * @param length the length of the data in the byte array to use
     */
    public int sendTo(IcePeer peer, short componentId, byte[] data, int offset, int length) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        return sendTo(peer, componentId, bb);
    }

    /**
     * Sends data not to a specific ip/port, but to a specific peer
     * @param peer the peer to send this data to.
     * @param buffer the packet to send to the peer
     */
    @Override
    public int sendTo(IcePeer peer, short componentId, ByteBuffer buffer) throws IOException {
        CandidatePair pair = peer.getNominated().get(this).get(componentId);
        if (pair != null) {
            int remainingBytes = buffer.remaining();
            pair.getLocalCandidate().socket.send(buffer, pair.getRemoteCandidate().getSocketAddress());
            return remainingBytes;
        } else {
            throw new IOException("Peer " + peer + " is not connected via " + this);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + hashCode() + "]";
    }

    @Override
    public int write(ByteBuffer data, short componentId) throws IOException {
        for (IcePeer peer : this.getPeerMap().values()) {
            sendTo(peer, componentId, data);
        }
        return 0;
    }

    @Override
    public int read(ByteBuffer data, short componentId) throws IOException {
        return getSocketChannel(componentId).read(data);
    }

    @Override
    public int send(ByteBuffer data, short componentId) throws IOException {
        return write(data,componentId);
    }
}
