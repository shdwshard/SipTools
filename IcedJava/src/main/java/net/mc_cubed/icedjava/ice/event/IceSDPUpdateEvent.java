/*
 * Copyright 2011 Charles Chappell.
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
package net.mc_cubed.icedjava.ice.event;

import java.util.List;
import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SessionDescription;

/**
 * An SDPUpdateEvent signals that an SDP Update should be sent over the wire to
 * the remote peer.
 *
 * @author charles
 */
public interface IceSDPUpdateEvent extends IceEvent {
    
    /**
     * 
     * @return 
     */
    Origin generateOrigin();
    
    /**
     * 
     * @param username
     * @return 
     */
    Origin generateOrigin(String username);
    /**
     * Get the connection associated with this SDP update
     * 
     * If not being used directly in the session description, this should be
     * added to each MediaDescription line before being sent.
     * 
     * @return 
     */    
    Connection getConnection();
    
    /**
     * Get the Global ICE attributes as a list.  These attributes are essential
     * to the proper functioning of ICE, and should be added as-is to the
     * Session Description.
     * 
     * @return 
     */
    List<Attribute> getIceAttributes();
    
    /**
     * Get the MediaDescriptions, including ICE candidates.
     * 
     * @return 
     */
    List<MediaDescription> getMediaDescriptions();
    
    /**
     * Get a fully formed SessionDescription, suitable for sending as-is over
     * the wire.
     * 
     * @return 
     */
    SessionDescription getSessionDescription();
    
    /**
     * Uses the output of getSessionDescription as a base, and outputs a
     * text only Session Description, suitable for simpler uses of ICE.
     * 
     * @return 
     */
    String getTextDescription();
}
