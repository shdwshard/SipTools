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
import java.util.Date;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.OutputDataStream;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.ice.IceStateMachine.AgentRole;
import net.mc_cubed.icedjava.stun.StunUtil;

/**
 *
 * @author Charles Chappell
 */
public class LocalRTPConnectorTest extends TestCase {

    public LocalRTPConnectorTest(String testName) {
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

    public void testRTPConnector() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector");
        InetSocketAddress STUN_SERVER = StunUtil.getCachedStunServerSocket();
        InterfaceProfile defaultIf = IceUtil.getBestInterfaceCandidate(STUN_SERVER);
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[] {new IcedRTPConnector(STUN_SERVER, medias[0].getMedia(),defaultIf.getPublicIP()),
                new IcedRTPConnector(STUN_SERVER, medias[1].getMedia(),defaultIf.getPublicIP())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[] {new IcedRTPConnector(STUN_SERVER, medias[0].getMedia(),defaultIf.getPublicIP()),
                new IcedRTPConnector(STUN_SERVER, medias[1].getMedia(),defaultIf.getPublicIP())};

        // Create a local peer for a yet unspecified remote peer
        IcePeerImpl localPeer = new IcePeerImpl("localPeer",AgentRole.CONTROLLING,localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeerImpl remotePeer = new IcePeerImpl("remotePeer",AgentRole.CONTROLLED,remoteSockets);
        // Set local only mode
        remotePeer.setLocalOnly(true);

        
        Assert.assertNotNull(session);
        
        // Establish the SDP connection
        localPeer.setSdpListener(remotePeer);
        remotePeer.setSdpListener(localPeer);
       
        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Start the state machines
        localPeer.start();
        remotePeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);

        Assert.assertEquals(IceStatus.IN_PROGRESS,localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS,remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(500);
        }
        
        Assert.assertEquals("ICE processing failed to finish in under 60 seconds",IceStatus.SUCCESS,localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to finish in under 60 seconds",IceStatus.SUCCESS,remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());

        final byte[] data = new byte[30];
        final byte[] data2 = new byte[30];
        final byte[] data3 = new byte[30];
        final byte[] data4 = new byte[30];

        PushSourceStream pss = remoteSockets[0].getDataInputStream();
        PushSourceStream pss2 = remoteSockets[0].getControlInputStream();
        PushSourceStream pss3 = remoteSockets[1].getDataInputStream();
        PushSourceStream pss4 = remoteSockets[1].getControlInputStream();

        OutputDataStream ods = localSockets[0].getDataOutputStream();
        OutputDataStream ods2 = localSockets[0].getControlOutputStream();
        OutputDataStream ods3 = localSockets[1].getDataOutputStream();
        OutputDataStream ods4 = localSockets[1].getControlOutputStream();

        ods.write("Testing1".getBytes(),0,8);
        ods2.write("Testing2".getBytes(),0,8);
        ods3.write("Testing3".getBytes(),0,8);
        ods4.write("Testing4".getBytes(),0,8);

        // Wait for the data to arrive
        Thread.sleep(100);

        pss.read(data, 0, 30);
        pss2.read(data2, 0, 30);
        pss3.read(data3, 0, 30);
        pss4.read(data4, 0, 30);

        // Test for the presense of the data
        Assert.assertEquals("Testing1",new String(data, 0, 8));
        Assert.assertEquals("Testing3",new String(data3, 0, 8));
        Assert.assertEquals("Testing2",new String(data2, 0, 8));
        Assert.assertEquals("Testing4",new String(data4, 0, 8));

        System.out.println(localPeer.createOffer());
        System.out.println(remotePeer.createOffer());

    }

    public void testRTPConnectorConflict() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector Conflict");
        InetSocketAddress STUN_SERVER = StunUtil.getCachedStunServerSocket();
        InterfaceProfile defaultIf = IceUtil.getBestInterfaceCandidate(STUN_SERVER);
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[] {new IcedRTPConnector(STUN_SERVER, medias[0].getMedia(),defaultIf.getPublicIP()),
                new IcedRTPConnector(STUN_SERVER, medias[1].getMedia(),defaultIf.getPublicIP())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[] {new IcedRTPConnector(STUN_SERVER, medias[0].getMedia(),defaultIf.getPublicIP()),
                new IcedRTPConnector(STUN_SERVER, medias[1].getMedia(),defaultIf.getPublicIP())};

        // Create a local peer for a yet unspecified remote peer
        IcePeerImpl localPeer = new IcePeerImpl("localPeer",AgentRole.CONTROLLING,localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeerImpl remotePeer = new IcePeerImpl("remotePeer",AgentRole.CONTROLLING,remoteSockets);
        remotePeer.setLocalOnly(true);

        Assert.assertNotNull(session);

        // Establish the SDP connection
        localPeer.setSdpListener(remotePeer);
        remotePeer.setSdpListener(localPeer);

        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Start the state machines
        localPeer.start();
        remotePeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);

        Assert.assertEquals(IceStatus.IN_PROGRESS,localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS,remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(100);
        }

        Assert.assertEquals("ICE processing failed to finish in under 60 seconds",IceStatus.SUCCESS,localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to finish in under 60 seconds",IceStatus.SUCCESS,remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());

        // Test for ICE Role Conflict Resolution
        Assert.assertNotSame("Ice MUST resolve a role conflict!",localPeer.isLocalControlled(), remotePeer.isLocalControlled());

        final byte[] data = new byte[30];

        PushSourceStream pss = remoteSockets[0].getDataInputStream();

        OutputDataStream ods = localSockets[0].getDataOutputStream();

        ods.write("Testing".getBytes(),0,7);

        // Wait for the data to arrive
        Thread.sleep(100);

        pss.read(data, 0, 30);

        // Test for the presense of the data
        Assert.assertEquals("Testing",new String(data, 0, 7));

        System.out.println(localPeer.createOffer());
        System.out.println(remotePeer.createOffer());

    }
}
