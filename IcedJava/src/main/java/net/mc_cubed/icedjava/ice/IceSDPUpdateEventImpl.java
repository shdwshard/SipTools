/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.ice.event.IceSDPUpdateEvent;

import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

/**
 *
 * @author shdwshard
 */
class IceSDPUpdateEventImpl implements IceSDPUpdateEvent {

    final SdpFactory factory;
    final IcePeer peer;
    final Connection connection;
    final List<Attribute> iceAttributes;
    final List<MediaDescription> mediaDescriptions;
    final Date modifiedDate;

    public IceSDPUpdateEventImpl(IcePeer peer, Connection connection, List<Attribute> iceAttributes, List<MediaDescription> mediaDescriptions, Date modifiedDate) {
        factory = SdpFactory.getInstance();
        this.peer = peer;
        this.connection = connection;
        this.iceAttributes = iceAttributes;
        this.mediaDescriptions = mediaDescriptions;
        this.modifiedDate = modifiedDate;
    }

    @Override
    public Origin generateOrigin() {
        try {
            return factory.createOrigin("-", peer.hashCode(), modifiedDate.getTime(), connection.getNetworkType(), connection.getAddressType(), connection.getAddress());
        } catch (SdpException ex) {
            Logger.getLogger(IceSDPUpdateEventImpl.class.getName()).log(Level.SEVERE, "Exception generating SDP Origin", ex);
            return null;
        }
    }

    @Override
    public Origin generateOrigin(String username) {
        try {
            return factory.createOrigin(username, peer.hashCode(), modifiedDate.getTime(), connection.getNetworkType(), connection.getAddressType(), connection.getAddress());
        } catch (SdpException ex) {
            Logger.getLogger(IceSDPUpdateEventImpl.class.getName()).log(Level.SEVERE, "Exception generating SDP Origin", ex);
            return null;
        }
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public List<Attribute> getIceAttributes() {
        return this.iceAttributes;
    }

    @Override
    public List<MediaDescription> getMediaDescriptions() {
        return this.mediaDescriptions;
    }

    @Override
    public SessionDescription getSessionDescription() {
        try {
            SessionDescription retval = factory.createSessionDescription();
            retval.setOrigin(generateOrigin());
            retval.setConnection(connection);
            retval.getAttributes(true).addAll(iceAttributes);
            retval.getMediaDescriptions(true).addAll(mediaDescriptions);
            return retval;
        } catch (SdpException ex) {
            Logger.getLogger(IceSDPUpdateEventImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getTextDescription() {
        SessionDescription description = getSessionDescription();
        return description.toString();
    }

    @Override
    public IcePeer getIcePeer() {
        return this.peer;
    }
}
