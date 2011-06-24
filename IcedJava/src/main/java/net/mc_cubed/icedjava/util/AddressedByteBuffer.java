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
package net.mc_cubed.icedjava.util;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Stores a socketAddress/ByteBuffer pair for use in queues and other applications
 * where it's useful to have this data stored together
 * 
 * @author Charles Chappell
 * @since 1.0
 */
public class AddressedByteBuffer {
    
    protected final SocketAddress address;
    protected final ByteBuffer buffer;
    
    /**
     * Constructor which creates the association object
     * 
     * @param socketAddress Socket Address to store
     * @param byteBuffer Byte Buffer to store
     */
    public AddressedByteBuffer(SocketAddress socketAddress, ByteBuffer byteBuffer) {
        this.address = socketAddress;
        this.buffer = byteBuffer;
    }

    
    /**
     * Returns the socket address stored in this object
     * 
     * @return 
     */
    public SocketAddress getAddress() {
        return address;
    }

    /**
     * Returns the Byte Buffer stored in this object
     * 
     * @return 
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    
    
}
