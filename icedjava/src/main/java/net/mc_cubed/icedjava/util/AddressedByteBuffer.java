/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.util;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author charles
 */
public class AddressedByteBuffer {
    protected final SocketAddress address;
    protected final ByteBuffer buffer;
    public AddressedByteBuffer(SocketAddress socketAddress, ByteBuffer bb) {
        this.address = socketAddress;
        this.buffer = bb;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    
    
}
