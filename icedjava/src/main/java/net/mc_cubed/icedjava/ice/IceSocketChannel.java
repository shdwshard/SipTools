/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 *
 * @author charles
 */
public interface IceSocketChannel extends ByteChannel, ScatteringByteChannel, GatheringByteChannel {

    SocketAddress receive(ByteBuffer dst) throws IOException;

    int send(ByteBuffer src, SocketAddress target)throws IOException;
}