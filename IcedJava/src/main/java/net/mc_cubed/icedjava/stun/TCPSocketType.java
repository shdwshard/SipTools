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
package net.mc_cubed.icedjava.stun;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapping for TCP Socket Types in ICE
 *
 * @author Charles Chappell
 * @since 1.0
 */
public enum TCPSocketType {

    ACTIVE("act"),
    SO("so"),
    PASSIVE("pass");
    
    final String networkString;
    
    final static Map<String,TCPSocketType> socketTypeMap = new HashMap<String,TCPSocketType>();
    
    static {
        for (TCPSocketType types : EnumSet.allOf(TCPSocketType.class)) {
            socketTypeMap.put(types.getNetworkString(), types);
        }
    }
    
    TCPSocketType(String networkString) {
        this.networkString = networkString;
    }

    public String getNetworkString() {
        return networkString;
    }
    
    static public TCPSocketType fromNetworkString(String networkString) {
        return socketTypeMap.get(networkString);
    }
}
