/*
 * Copyright 2009 Charles Chappell.
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

import java.net.DatagramPacket;
import net.mc_cubed.icedjava.stun.DatagramListener;

/**
 * Bridges between a DatagramListener, which is only intended for a single source,
 * and a MultiDatagramListener, which merges packets from several sources.
 *
 * @author Charles Chappell
 * @since 0.9
 * @deprecated DatagramListeners are being replaced by SocketChannels
 * @see IceSocketChannel
 */
class MultiDatagramListenerAdapter implements DatagramListener {

    protected final MultiDatagramListener multiListener;
    protected final IceSocket source;

    public MultiDatagramListenerAdapter(MultiDatagramListener multiListener,IceSocket source) {
        this.multiListener = multiListener;
        this.source = source;
    }

    @Override
    public void deliverDatagram(DatagramPacket p) {
        multiListener.deliverDatagram(p, source);
    }



}
