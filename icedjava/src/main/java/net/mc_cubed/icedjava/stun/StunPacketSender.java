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
package net.mc_cubed.icedjava.stun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import net.mc_cubed.icedjava.packet.StunPacket;

/**
 * Classes implementing StunPacketSender, are able to perform STUN tests, and
 * return results asynchronously using a java Future.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface StunPacketSender {

    /**
     * Set the maximum number of STUN retries.  This can have performance
     * implications as the backout schedule starts at 500ms, and doubles after
     * each attempt. The default is also quite high, and so can take many 
     * seconds to time out.
     * 
     * @param retries maximum number of retries to send STUN packets.
     */
    void setMaxRetries(int retries);

    /**
     * Mostly used internally, and should NOT be called directly.
     * This method is used to record a STUN reply to a sent packet.
     * 
     * @param packet Reply packet
     */
    public void storeAndNotify(StunPacket packet);

    /**
     * Performs a stun BINDING request to the specified server.
     *
     * @param stunServer STUN server to test
     * @return a future which can be used to obtain the result of this STUN test
     * @throws IOException
     * @throws InterruptedException
     */
    public Future<StunReply> doTest(InetSocketAddress stunServer) throws IOException, InterruptedException;

    /**
     * Performs a stun BINDING request to the specified server.
     *
     * @param stunServer STUN server to test
     * @param packet packet to use for this test
     * @return a future which can be used to obtain the result of this STUN test
     * @throws IOException
     * @throws InterruptedException
     */
    public Future<StunReply> doTest(InetSocketAddress stunServer, StunPacket packet) throws IOException, InterruptedException;
}
