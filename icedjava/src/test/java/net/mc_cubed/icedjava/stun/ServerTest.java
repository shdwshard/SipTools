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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.SoftwareAttribute;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Charles Chappell
 */
@RunWith(Parameterized.class)
public class ServerTest extends TestCase {

    static private DatagramStunSocket socket;
    final private String server;

    public ServerTest(String server) {
        this.server = server;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        socket = StunUtil.getStunSocket(new InetSocketAddress(0),StunListenerType.CLIENT);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        socket.close();
    }
   
    @Parameters
    public static Collection<Object[]> serverList() {
        List<Object[]> retval = new ArrayList<Object[]>(StunUtil.serverList.length);
        for (String server : StunUtil.serverList) {
            retval.add(new Object[] {server});            
        }
        
        return retval;
    }

    @Test
    public void serverTest() throws UnknownHostException, IOException, InterruptedException, ExecutionException {

        for (InetSocketAddress serverSock : StunUtil.getStunServerByName(server)) {

            StunReply s = socket.doTest(serverSock).get();

            if (s != null && !s.isSuccess()) {
                System.out.println(s.getErrorCode() + " " + s.getErrorReason());
            }

            Assert.assertNotNull("Reply was null testing server: " + serverSock,s);
            Assert.assertTrue(server + " failed! ", s.isSuccess());

            SoftwareAttribute software = (SoftwareAttribute) s.getAttribute(AttributeType.SOFTWARE);
/*
            if (software != null) {
                System.out.println("Software is: " + software.getValue());
            } else {
                System.out.println("No software attribute present.");
            }

            if (s.isValidFingerprint() != null) {
                Assert.assertTrue("Invalid fingerprint received from " + server, s.isValidFingerprint());
            }

            System.out.println("Mapped address is " + s.getMappedAddress());
*/
            System.out.println("" + serverSock.toString() + " replied: " +
                    s.getMappedAddress() +
                    (software != null ? " (software=" + software.getValue() + ")" : "") +
                    (s.isValidFingerprint() != null ? " Valid Fingerprint=" + s.isValidFingerprint() : ""));
        }
    }
}
