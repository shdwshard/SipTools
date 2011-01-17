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
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

/**
 * MultiSDPListener is an interface for objects listening for SDP announcements
 * from multiple peers
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface MultiSDPListener {

    @Deprecated
    public void sendSession(SessionDescription session, IcePeer fromPeer);

    public void updateMedia(Connection conn,Vector iceAttributes, Vector iceMedias, IcePeer fromPeer)
            throws SdpParseException;

    public void updateMedia(Connection conn,List<Attribute> iceAttributes, List<MediaDescription> iceMedias, IcePeer fromPeer)
            throws SdpParseException;


}
