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

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.OutputDataStream;
import javax.sdp.Media;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.stun.StunUtil;

/**
 *
 * @author Charles Chappell
 */
public class IcedRTPConnectorTest extends TestCase {

    private final InetSocketAddress STUN_SERVER;
    private final Media MEDIA;
    private final InterfaceProfile DEFAULT_IF;
    public IcedRTPConnectorTest(String testName) throws UnknownHostException, IllegalArgumentException, SdpException {
        super(testName);
        STUN_SERVER = StunUtil.getCachedStunServerSocket();
        Vector v = new Vector();
        v.add("26");
        MEDIA = SdpFactory.getInstance().createMedia("video", 0, 2, "RTP/AVP", v);
        DEFAULT_IF = IceUtil.getBestInterfaceCandidate(STUN_SERVER);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of removeTargets method, of class IcedRTPConnector.
     */
    public void testRemoveTargets() throws SocketException, SdpParseException {
        System.out.println("removeTargets");
        String reason = "Because this is a test";
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        instance.removeTargets(reason);
    }

    /**
     * Test of getDataInputStream method, of class IcedRTPConnector.
     */
    public void testGetDataInputStream() throws Exception {
        System.out.println("getDataInputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        PushSourceStream result = instance.getDataInputStream();
        assertNotNull(result);
    }

    /**
     * Test of getDataOutputStream method, of class IcedRTPConnector.
     */
    public void testGetDataOutputStream() throws Exception {
        System.out.println("getDataOutputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        OutputDataStream result = instance.getDataOutputStream();
        assertNotNull(result);
    }

    /**
     * Test of getControlInputStream method, of class IcedRTPConnector.
     */
    public void testGetControlInputStream() throws Exception {
        System.out.println("getControlInputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        PushSourceStream result = instance.getControlInputStream();
        assertNotNull(result);
    }

    /**
     * Test of getControlOutputStream method, of class IcedRTPConnector.
     */
    public void testGetControlOutputStream() throws Exception {
        System.out.println("getControlOutputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        OutputDataStream result = instance.getControlOutputStream();
        assertNotNull(result);
    }

    /**
     * Test of getReceiveBufferSize method, of class IcedRTPConnector.
     */
    public void testGetReceiveBufferSize() throws SocketException, SdpParseException {
        System.out.println("getReceiveBufferSize");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        int expResult = -1;
        int result = instance.getReceiveBufferSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of setSendBufferSize method, of class IcedRTPConnector.
     */
    public void testSetSendBufferSize() throws Exception {
        System.out.println("setSendBufferSize");
        int arg0 = 1234;
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        instance.setSendBufferSize(arg0);
    }

    /**
     * Test of getSendBufferSize method, of class IcedRTPConnector.
     */
    public void testGetSendBufferSize() throws SocketException, SdpParseException {
        System.out.println("getSendBufferSize");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        int expResult = -1;
        int result = instance.getSendBufferSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRTCPBandwidthFraction method, of class IcedRTPConnector.
     */
    public void testGetRTCPBandwidthFraction() throws SocketException, SdpParseException {
        System.out.println("getRTCPBandwidthFraction");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        double expResult = -1;
        double result = instance.getRTCPBandwidthFraction();
        // Does not specify, but leaves this to the RTPManager
        assertEquals(expResult, result);
    }

    /**
     * Test of getRTCPSenderBandwidthFraction method, of class IcedRTPConnector.
     */
    public void testGetRTCPSenderBandwidthFraction() throws SocketException, SdpParseException {
        System.out.println("getRTCPSenderBandwidthFraction");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        double expResult = -1;
        double result = instance.getRTCPSenderBandwidthFraction();

        // Does not specify, but leaves this to the RTPManager
        assertEquals(expResult, result);
    }

}
