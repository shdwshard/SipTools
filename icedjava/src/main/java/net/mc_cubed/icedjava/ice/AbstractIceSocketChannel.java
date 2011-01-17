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

import java.net.DatagramPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.AbstractChannelSink;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;

/**
 *
 * @author Charles Chappell
 * @since 1.0
 */
public class AbstractIceSocketChannel extends AbstractChannelSink implements IceSocketChannel {

    protected final IceStateMachine peer;
    protected final IceSocket socket;
    protected final short channel;
    ChannelPipeline pipeline;
    
    public AbstractIceSocketChannel(IceStateMachine peer, IceSocket socket, short channel) {
        this.peer = peer;
        this.socket = socket;
        this.channel = channel;
    }



    @Override
    public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent) {
            MessageEvent evt = (MessageEvent)e;
            ChannelBuffer buf = (ChannelBuffer)evt.getMessage();
            peer.sendTo(socket,channel,buf);
        }

        this.pipeline = pipeline;
    }

    @Override
    public void write(DatagramPacket p) {
        write(ChannelBuffers.copiedBuffer(p.getData(), p.getOffset(), p.getLength()));

    }
    public void write(ChannelBuffer buf) {
        pipeline.getChannel().write(buf);
    }

}
