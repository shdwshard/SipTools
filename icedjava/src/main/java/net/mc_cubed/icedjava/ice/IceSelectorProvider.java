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
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

/**
 * NIO bridge that may or may not make it into the final 1.0 release
 *
 * @author Charles Chappell
 * @since 1.0
 */
public class IceSelectorProvider extends SelectorProvider {

    // TODO: Implement this method
    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO: Implement this method
    @Override
    public Pipe openPipe() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO: Implement this method
    @Override
    public AbstractSelector openSelector() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO: Implement this method
    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO: Implement this method
    @Override
    public SocketChannel openSocketChannel() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
