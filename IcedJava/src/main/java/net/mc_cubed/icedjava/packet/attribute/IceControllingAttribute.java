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
 * The ICEControlling Attribute signals to the remote peer that the sending peer
 * is an ICE peer in the controlling role. In the event that both peers believe
 * they are in the controlling role, the included randomly generated number is
 * used as a tie breaker to determine who the real controlling peer is.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface IceControllingAttribute extends Attribute {
    
    /**
     * This number is the tie breaker used during ICE processing to determine
     * who is the controlling party in the event of an ICE conflict
     * 
     * @return The ICE tie breaker
     */
    public long getNumber();
}
