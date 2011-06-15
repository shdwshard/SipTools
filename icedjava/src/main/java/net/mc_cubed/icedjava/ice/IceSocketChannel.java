/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import net.mc_cubed.icedjava.ice.event.IceEventListener;

/**
 *
 * @author charles
 */
public interface IceSocketChannel extends ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    void addEventListener(IceEventListener listener);

    void removeEventListener(IceEventListener listener);

    IcePeer receive(ByteBuffer dst) throws IOException;

    int send(ByteBuffer src, IcePeer target) throws IOException;
}