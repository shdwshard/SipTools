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
import java.util.concurrent.ExecutionException;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.SoftwareAttribute;

/**
 *
 * @author Charles Chappell
 */
public class ServerTest extends TestCase {

    private static Integer STUN_PORT = 3478;
    private DatagramStunSocket socket;

    public ServerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        socket = StunUtil.getStunSocket(new InetSocketAddress(0),StunListenerType.CLIENT);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        socket.close();
    }

    // List of STUN servers used for testing lifted from 
    //  http://www.voip-info.org/wiki/view/STUN
    public void testKnownServers() throws IOException, InterruptedException, UnknownHostException, ExecutionException {
        for (String stunServer : StunUtil.serverList) {
            runTest(stunServer);
        }

    }

    private void runTest(String server) throws UnknownHostException, IOException, InterruptedException, ExecutionException {
        System.out.println(server);

        for (InetSocketAddress serverSock : StunUtil.getStunServerByName(server)) {

            StunReply s = socket.doTest(serverSock).get();

            if (!s.isSuccess()) {
                System.out.println(s.getErrorCode() + " " + s.getErrorReason());
            }

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
