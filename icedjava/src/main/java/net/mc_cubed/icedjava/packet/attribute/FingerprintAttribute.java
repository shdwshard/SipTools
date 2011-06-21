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
package net.mc_cubed.icedjava.packet.attribute;

/**
 * Fingerprint Attributes are used for error checking, but also to differentiate
 * STUN packets from non-STUN packets in a flow with a high degree of precision.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface FingerprintAttribute extends Attribute {
    /*
     * Compute the message hash for use in this attribute.  The credentials MUST
     *  be delivered via an alternate mechanism
     * @param data the buffer being used to construct or verify the STUN message
     * @param offset the offset in the buffer where the stun message starts
     * @param length the length of the stun packet UP TO BUT NOT INCLUDING the
     *          attribute the hash is being computed on
     */

    public void computeHash(byte[] data, int offset, int length);
    /*
     * Verify the message hash used in this attribute
     * @param data the buffer being used to construct or verify the STUN message
     * @param offset the offset in the buffer where the stun message starts
     * @param length the length of the stun packet UP TO BUT NOT INCLUDING the
     *          attribute the hash is being computed on
     */

    public boolean verifyHash(byte[] data, int offset, int length);

    /*
     * Check the validity of the hash
     */
    public boolean isValid();
}
