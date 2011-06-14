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
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Charles Chappell
 */ 
public class DemultiplexerSocketTest extends TestCase {

    @Test
    public void testSocket() throws Exception, Throwable {
        System.out.println("socket");
        DatagramDemultiplexerSocket instance1 = StunUtil.getDemultiplexerSocket(0);
        DatagramDemultiplexerSocket instance2 = StunUtil.getDemultiplexerSocket(0);

        System.out.println("Testing on ports: " + instance1.getLocalPort() + " " + instance2.getLocalPort());
        
        StunReply i1reply = instance1.doTest(InetAddress.getByName("127.0.0.1"), instance2.getLocalPort()).get();
        StunReply i2reply = instance2.doTest(InetAddress.getByName("127.0.0.1"), instance1.getLocalPort()).get();

        instance1.close();
        instance2.close();

        Assert.assertTrue("Got wrong reply: " + i1reply + " " + i1reply.getPacket(), i1reply.isSuccess());
        Assert.assertTrue("Got wrong reply: " + i2reply + " " + i2reply.getPacket(), i2reply.isSuccess());
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getAddress().getHostAddress(), "127.0.0.1");
        Assert.assertEquals("Got wrong reply: " + i1reply,i1reply.getMappedAddress().getPort(), instance1.getLocalPort());
        Assert.assertEquals("Got wrong reply: " + i2reply,i2reply.getMappedAddress().getPort(), instance2.getLocalPort());

    }

}
