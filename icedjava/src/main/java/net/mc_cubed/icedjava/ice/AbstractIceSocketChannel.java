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

/**
 *
 * @author Charles Chappell
 * @since 1.0
 */
abstract class AbstractIceSocketChannel implements IceSocketChannel {

    protected final IceStateMachine peer;

    public AbstractIceSocketChannel(IceStateMachine peer) {
        this.peer = peer;
    }

    public void write(DatagramPacket p) throws IOException {        
        write(ByteBuffer.wrap(p.getData(), p.getOffset(), p.getLength()));

    }

    @Override
    public abstract int read(ByteBuffer bb) throws IOException;

    @Override
    public boolean isOpen() {
        return peer.getIceStatus() == IceStatus.SUCCESS;
    }

    @Override
    public void close() throws IOException {
        peer.close();
    }

    @Override
    public abstract int write(ByteBuffer bb) throws IOException;

    @Override
    public long read(ByteBuffer[] bbs, int off, int len) throws IOException {
        long readBytes = 0;
        for (int i = off; i < off + len; i++) {
            readBytes += read(bbs[i]);
        }
        
        return readBytes;
    }

    @Override
    public long read(ByteBuffer[] bbs) throws IOException {
        long readBytes = 0;
        for (ByteBuffer bb : bbs) {
            readBytes += read(bb);
        }
        
        return readBytes;
    }

    @Override
    public long write(ByteBuffer[] bbs, int off, int len) throws IOException {
        long writtenLength = 0;
        for (int i = off; i < off + len; i++) {
            writtenLength += write(bbs[i]);
        }

        return writtenLength;

    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        long writtenLength = 0;
        for (ByteBuffer bb : bbs) {
            writtenLength += write(bb);
        }
        return writtenLength;
    }
}
