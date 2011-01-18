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
package net.mc_cubed.icedjava.packet.attribute;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.ice.IceUtil;
import net.mc_cubed.icedjava.ice.InterfaceProfile;
import net.mc_cubed.icedjava.packet.header.MessageHeader;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.util.StringUtils;

/**
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class XORMappedAddressAttributeTest extends TestCase {

    Logger log = Logger.getLogger(XORMappedAddressAttributeTest.class.getName());

    public XORMappedAddressAttributeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testXOR() {
        InetSocketAddress stunServer = StunUtil.getStunServerSocket();

        List<InterfaceProfile> profiles = IceUtil.getInterfaceCandidates(stunServer);


        Random rand = new Random();
        byte[] buffer = new byte[32];

        for (InterfaceProfile profile : profiles) {
            InetAddress address = profile.getAddress();
            for (int i = 0; i <= 255; i++) {
                int portNum = rand.nextInt(65536);
                byte[] txId = MessageHeader.generateTransactionId();
                Attribute attr = AttributeFactory.createXORMappedAddressAttribute(address, portNum, txId);
                attr.write(buffer, 0);
                log.log(Level.FINE, "xor: {0} {1}",
                        new Object[]{StringUtils.getHexString(txId), StringUtils.getHexString(buffer)});

                XORMappedAddressAttribute attr2 = (XORMappedAddressAttribute)AttributeFactory.processOneAttribute(buffer,0,0);
                Assert.assertEquals("Address mismatch " + address + " != " + attr2.getAddress(txId),address, attr2.getAddress(txId));
                Assert.assertEquals("Port number mismatch " + Integer.toHexString(portNum) + " != " + Integer.toHexString(attr2.getPort()),portNum, attr2.getPort());
            }
        }
    }
}
