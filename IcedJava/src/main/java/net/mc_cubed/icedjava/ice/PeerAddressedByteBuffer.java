/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.nio.ByteBuffer;

/**
 *
 * @author charles
 */
public class PeerAddressedByteBuffer {

    final IcePeer peer;
    final ByteBuffer buffer;

    public PeerAddressedByteBuffer(IcePeer peer, ByteBuffer buffer) {
        this.peer = peer;
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public IcePeer getPeer() {
        return peer;
    }
    
    
}
