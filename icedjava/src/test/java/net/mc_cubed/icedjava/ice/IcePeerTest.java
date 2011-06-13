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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.ice.IceStateMachine.AgentRole;
import net.mc_cubed.icedjava.stun.StunUtil;

/**
 *
 * @author Charles Chappell
 */
public class IcePeerTest extends TestCase {

    private final InetSocketAddress stunServer = StunUtil.getStunServerSocket();

    public IcePeerTest(String testName) throws UnknownHostException {
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

    /**
     * Test of collectCandidates method, of class IceStateMachine.
     */
    public void testCollectCandidates() throws SocketException, SdpException {
        System.out.println("collectCandidates");
        int port = 1234;
        SdpFactory factory = SdpFactory.getInstance();
        IceFactory iceFactory = new IceFactory();
        IceSocket[] iceSockets = new IceSocket[]{
            iceFactory.createIceSocket(factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia()),
            iceFactory.createIceSocket(factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia())};
        IcePeerImpl instance = new IcePeerImpl("localPeer", AgentRole.CONTROLLING, null, null, iceSockets);
        List<LocalCandidate> candidates = new LinkedList<LocalCandidate>();
        for (IceSocket socket : instance.getIceSockets()) {
            candidates.addAll(instance.getLocalCandidates(socket));
        }
        for (LocalCandidate candidate : candidates) {
            System.out.println(candidate);
        }

        instance.close();

        for (IceSocket socket : iceSockets) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(IcePeerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void testMarkupCandidates() throws SdpException, SdpParseException, UnknownHostException, SocketException {
        System.out.println("collectCandidates");
        SdpFactory factory = SdpFactory.getInstance();
        IceFactory iceFactory = new IceFactory();
        IceSocket[] iceSockets = new IceSocket[]{
            iceFactory.createIceSocket(factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia()),
            iceFactory.createIceSocket(factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia())};
        IcePeerImpl instance = new IcePeerImpl("localPeer", AgentRole.CONTROLLING, null, null, iceSockets);
        SessionDescription session = instance.createOffer();
        System.out.println(session);

        instance.close();

    }
}
