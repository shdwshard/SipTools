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

import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import java.net.InetAddress;
import junit.framework.TestCase;

/**
 *
 * @author Charles Chappell
 */
public class StunSocketTest extends TestCase {

    private static String STUN_SERVER = StunUtil.getStunServer();
    private static Integer STUN_PORT = StunUtil.STUN_PORT;

    public StunSocketTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSocket() throws Exception, Throwable {
        System.out.println("socket");
        DatagramStunSocket instance1 = StunUtil.getStunSocket(1234, StunSocketType.BOTH);
        DatagramStunSocket instance2 = StunUtil.getStunSocket(5678, StunSocketType.BOTH);

        StunPacketImpl packet = new StunPacketImpl(MessageClass.REQUEST, MessageMethod.BINDING);

        // Local send tests
        //instance1.send(InetAddress.getByName("127.0.0.1"),5678,packet);
        //instance2.send(InetAddress.getByName("127.0.0.1"),1234,packet);

        //Thread.sleep(200);

        // Internet based test
        //instance1.send(InetAddress.getByName(STUN_SERVER),STUN_PORT,packet);

        StunReply s = instance1.doTest(InetAddress.getByName(STUN_SERVER), STUN_PORT).get();

        System.out.println(s);

        //Thread.sleep(2000);

        instance1.close();
        instance2.close();

    }
}
