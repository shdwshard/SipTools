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
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.sdp.Media;
import javax.sdp.SdpException;
import net.mc_cubed.icedjava.stun.DatagramListener;

/**
 * Represents a generic IceSocket which Peers will be attached to.
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface IceSocket extends DatagramListener,IceSocketChannel {

    //public int getPort();

    /**
     * Gets the Media line this socket and all of its peers will be based
     * on.
     *
     * @return An SDP Media line that generically describes this IceSocket
     * @throws SdpException
     */
    public Media getMedia() throws SdpException;

    /**
     * Set the generic media line this socket will be based on.
     *
     * @param media the generic Media line to base this Ice Socket on.
     */
    public void setMedia(Media media);

    /**
     * Get a list of peers connected to this socket
     * 
     * @return A collection of IcePeers
     */
    public Collection<IcePeer> getPeers();

 //   public void setDatagramListener(DatagramListener listener);

    /**
     * Receive a datagram packet sent to this socket
     * @param p
     * @throws IOException
     */
    public int receive(DatagramPacket p, short componentId) throws IOException;
    public SocketAddress receive(ByteBuffer data, short componentId) throws IOException;

    /**
     * Send a packet on a specific component of this socket
     * 
     * @param data
     * @param componentId
     * @throws IOException
     */
    public int send(DatagramPacket data, short componentId) throws IOException;
    public int send(ByteBuffer data, SocketAddress target, short componentId) throws IOException;

    /**
     * Get the number of components making up this flow
     *
     * @return a whole number of ports/streams in use
     */
    public short getComponents();
    
    /**
     * Is this IceSocket closed? (not connected to any peers)
     * Delegates to isOpen and negates the result
     *
     * @return true if no peers are connected, false otherwise.
     */
    public boolean isClosed();

    public int write(ByteBuffer data, short componentId) throws IOException;
    
    public int read(ByteBuffer data, short componentId) throws IOException;

}
