/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author shadow
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

        for (InterfaceProfile profile : profiles) {
            InetAddress address = profile.getAddress();
            for (int i = 0; i <= 255; i++) {
                int portNum = rand.nextInt(65536);
                byte[] txId = MessageHeader.generateTransactionId();
                Attribute attr = new XORMappedAddressAttribute(address, portNum, txId);
                log.log(Level.FINE, "xor: {0} {1}",
                        new Object[]{StringUtils.getHexString(txId), StringUtils.getHexString(attr.getData())});

                XORMappedAddressAttribute attr2 = new XORMappedAddressAttribute(AttributeType.XOR_MAPPED_ADDRESS, attr.getLength(), attr.getData());
                Assert.assertEquals(address, attr2.getAddress(txId));
                Assert.assertEquals("Port number mismatch " + Integer.toHexString(portNum) + " != " + Integer.toHexString(attr2.getPort()),portNum, attr2.getPort());
            }
        }
    }
}
