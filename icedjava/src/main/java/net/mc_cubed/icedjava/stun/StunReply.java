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
package net.mc_cubed.icedjava.stun;

import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Provides a generic representation of a STUN reply.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface StunReply {

    /**
     * Does this reply object represent a successfully executed STUN request?
     *
     * @return true if success, false if an error
     */
    boolean isSuccess();

    /**
     * Is the fingerprint attribute of this STUN reply valid?
     *
     * @return true if the fingerprint is valid, false otherwise
     */
    Boolean isValidFingerprint();

    /**
     * Returns the STUN error code associated with this request
     *
     * @return 0 if successful, error code otherwise
     */
    int getErrorCode();

    /**
     * Get the error reason phrase (if provided) associated with an erroring
     * stun request.  Might not always be present
     *
     * @return a specific error reason if provided by the server, null otherwise
     */
    String getErrorReason();

    /**
     * The mapped address given by the STUN server in response to the request.
     * Will automatically decode XOR encoded attributes as well.
     *
     * @return the Socket Address the server saw when we sent the STUN request
     */
    InetSocketAddress getMappedAddress();

    /**
     * Get a specific attribute from the STUN reply packet
     *
     * @param attrType the attribute type to get
     *
     * @return the attribute or null if not present
     */
    Attribute getAttribute(AttributeType attrType);

    /**
     * Get a collection of attributes.  Some attributes may be duplicated, and
     * so might not be otherwise accessible through the getAttribute method.
     *
     * @return a collection of all the attributes received
     */
    Collection<Attribute> getAttributes();

    /**
     * Get the Stun Packet which was decoded into this StunReply object.
     *
     * @return The original StunPacket
     */
    public StunPacket getPacket();
}
