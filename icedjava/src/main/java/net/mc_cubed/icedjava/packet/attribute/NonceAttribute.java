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
 * A NonceAttribute is used during authentication processes to discourage replay
 * attacks by adding a random or semi-random information to the authentication
 * process. No meaning can or should be assigned to the nonce value.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface NonceAttribute extends Attribute {
    /**
     * Get the value of the Nonce represented by this attribute
     * 
     * @return The Nonce value.
     */
    public String getValue();
}
