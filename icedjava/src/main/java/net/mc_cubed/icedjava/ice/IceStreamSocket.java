/*
 * Copyright 2010 Charles Chappell.
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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import javax.sdp.Media;
import javax.sdp.SdpException;

/**
 * An initial implementation of ICE-TCP<br/><br/>
 * <strong>WARNING: ICE-TCP is a WORK IN PROGRESS</strong>
 *
 * @author Charles Chappell
 * @since 1.0
 */
class IceStreamSocket implements IceSocket {

    protected IceStreamSocket(Media media) {
    }

    @Override
    public Media getMedia() throws SdpException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMedia(Media media) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<IcePeer> getPeers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int receive(DatagramPacket p, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int send(DatagramPacket data, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public short getComponents() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deliverDatagram(DatagramPacket p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int send(ByteBuffer data, SocketAddress target, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int write(ByteBuffer data, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int read(ByteBuffer data, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int read(ByteBuffer bb) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long read(ByteBuffer[] bbs, int i, int i1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long read(ByteBuffer[] bbs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long write(ByteBuffer[] bbs, int i, int i1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SocketAddress receive(ByteBuffer data, short componentId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
