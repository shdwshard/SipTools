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

import java.util.List;
import java.util.Vector;
import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

/**
 * An interface for objects listening for SDP updates from an ICE state machine
 * or IcePeer.  All IceStateMachines (including IcePeers) also implement the
 * SDPListener interface to listen for SDP updates.
 *
 * @author Charles Chappell
 * @since 0.9
 * @see IcePeer
 */
public interface SDPListener {
    /**
     * Classes implementing the SDPListener interface can be sent an SDP update
     * to use for ICE or other processing using this method
     * 
     * @param session Updated SessionDescription to use for processing
     * @throws SdpException
     * @deprecated Use updateMedia() instead
     * @see .updateMedia
     */
    @Deprecated
    public void sendSession(SessionDescription session)
            throws SdpException;

    /**
     * Classes implementing the SDPListener interface can be sent an SDP update
     * to use for ICE or other processing using this method.  Not all parts of
     * the SDP descriptor necessarily originate from an IcePeer, or are destined
     * for an IcePeer, hence this method allows only the relevant information to
     * be sent, and the receiving class to use only the relevant information
     * 
     * @param conn The C line of the SDP description
     * @param iceAttributes global A lines used during ICE negociation
     * @param iceMedias M lines used during ICE processing
     * @throws SdpParseException 
     */
    public void updateMedia(Connection conn,Vector iceAttributes, Vector iceMedias)
            throws SdpParseException;

    /**
     * Classes implementing the SDPListener interface can be sent an SDP update
     * to use for ICE or other processing using this method.  Not all parts of
     * the SDP descriptor necessarily originate from an IcePeer, or are destined
     * for an IcePeer, hence this method allows only the relevant information to
     * be sent, and the receiving class to use only the relevant information
     * 
     * @param conn The C line of the SDP description
     * @param iceAttributes global A lines used during ICE negociation
     * @param iceMedias M lines used during ICE processing
     * @throws SdpParseException 
     */
    public void updateMedia(Connection conn,List<Attribute> iceAttributes, List<MediaDescription> iceMedias)
            throws SdpParseException;

}
