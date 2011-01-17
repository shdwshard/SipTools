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
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineException;

/**
 * JBoss Netty style Socket Channel for sending media to an IceDatagramSocket
 *
 * @author Charles Chappell
 * @since 1.0
 * @see IceDatagramSocket
 */
class IceDatagramSocketChannel implements IceSocketChannel {

    protected final IceSocket iceSocket;

    /**
     * Get the value of iceSocket
     *
     * @return the value of iceSocket
     */
    public IceSocket getIceSocket() {
        return iceSocket;
    }
    protected final short component;

    /**
     * Get the value of component
     *
     * @return the value of component
     */
    public int getComponent() {
        return component;
    }

    IceDatagramSocketChannel(IceSocket socket, short channel) {
        this.iceSocket = socket;
        this.component = channel;
    }

    public long write(ByteBuffer[] bbs, int offset, int length) throws IOException {
        long bytesWritten = 0;
        for (int bnum = offset; bnum < length; bnum++) {
            bytesWritten += write(bbs[bnum]);
        }

        return bytesWritten;
    }

    public long write(ByteBuffer[] bbs) throws IOException {
        return write(bbs, 0, bbs.length);
    }

    public int write(ByteBuffer bb) throws IOException {
        return iceSocket.send(bb, this.component);
    }

    public boolean isOpen() {
        return iceSocket.isOpen();
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("Call close on the owning Socket.");
    }

    public long read(ByteBuffer[] buffers, int offset, int length) throws IOException {
        int bytesRead = 0;
        for (int bufnum = offset; bufnum < length; bufnum++) {
            int retval = read(buffers[bufnum]);
            if (retval <= 0) {
                break;
            }
            bytesRead += retval;
        }
        return bytesRead;
    }

    public long read(ByteBuffer[] bbs) throws IOException {
        return read(bbs,0,bbs.length);
    }

    public int read(ByteBuffer bb) throws IOException {
        return iceSocket.receive(bb, component);
    }

    @Override
    public void write(DatagramPacket p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exceptionCaught(ChannelPipeline pipeline, ChannelEvent e, ChannelPipelineException cause) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
