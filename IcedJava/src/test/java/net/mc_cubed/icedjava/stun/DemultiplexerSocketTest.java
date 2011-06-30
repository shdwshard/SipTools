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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author Charles Chappell
 */
public class DemultiplexerSocketTest extends TestCase {

    public DemultiplexerSocketTest(String testName) {
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

    public void testUDPSocket() throws Exception, Throwable {
        System.out.println("socket");
        DemultiplexerSocket instance1 = StunUtil.getDemultiplexerSocket(1234);
        DemultiplexerSocket instance2 = StunUtil.getDemultiplexerSocket(5678);

        StunReply i1reply = instance1.doTest(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5678)).get();
        StunReply i2reply = instance2.doTest(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1234)).get();

        instance1.close();
        instance2.close();

        Assert.assertTrue("Got wrong reply: " + i1reply, i1reply.isSuccess());
        Assert.assertTrue("Got wrong reply: " + i2reply, i2reply.isSuccess());
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getPort(), 1234);
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getPort(), 5678);

    }

    public void ignoreTestTCPSocket() throws Exception, Throwable {
        System.out.println("socket");
        DemultiplexerSocket instance1 = StunUtil.getDemultiplexerSocket(new InetSocketAddress(1234),TransportType.TCP,true,null);
        DemultiplexerSocket instance2 = StunUtil.getDemultiplexerSocket(new InetSocketAddress(5678),TransportType.TCP,false,null);

        StunReply i1reply = instance1.doTest(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5678)).get();
        StunReply i2reply = instance2.doTest(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1234)).get();

        instance1.close();
        instance2.close();

        Assert.assertTrue("Got wrong reply: " + i1reply, i1reply.isSuccess());
        Assert.assertTrue("Got wrong reply: " + i2reply, i2reply.isSuccess());
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getPort(), 1234);
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getPort(), 5678);

    }
}
