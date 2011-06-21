/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import net.mc_cubed.icedjava.ice.event.IceEventListener;

/**
 * IceSocketChannels can be used to send bytes to an IcePeer along an established
 * or establishing IceSocket. Implements NIO interfaces for easy usage.
 *
 * @author charles
 * @see ScatteringByteChannel
 * @see GatheringByteChannel
 * @see ByteChannel
 * @see IceEventListener
 */
public interface IceSocketChannel extends ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    /**
     * IceEventListeners will receive asynchronous notification of IceEvents on
     * an IceSocketChannel.
     * 
     * @param listener 
     */
    void addEventListener(IceEventListener listener);

    void removeEventListener(IceEventListener listener);
}