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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import javax.media.protocol.PushSourceStream;
import javax.media.rtp.OutputDataStream;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import static org.junit.Assert.*;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Charles Chappell
 */
public class IcedRTPConnectorTest {

    private final Media MEDIA;

    public IcedRTPConnectorTest() throws UnknownHostException, IllegalArgumentException, SdpException {
        Vector v = new Vector();
        v.add("26");
        MEDIA = SdpFactory.getInstance().createMedia("video", 0, 2, "RTP/AVP", v);
    }

    /**
     * Test of removeTargets method, of class IcedRTPConnector.
     */
    @Test
    public void testRemoveTargets() throws SocketException, SdpParseException {
        System.out.println("removeTargets");
        String reason = "Because this is a test";
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        instance.removeTargets(reason);
    }

    /**
     * Test of getDataInputStream method, of class IcedRTPConnector.
     */
    @Test
    public void testGetDataInputStream() throws Exception {
        System.out.println("getDataInputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        PushSourceStream result = instance.getDataInputStream();
        assertNotNull(result);
    }

    /**
     * Test of getDataOutputStream method, of class IcedRTPConnector.
     */
    @Test
    public void testGetDataOutputStream() throws Exception {
        System.out.println("getDataOutputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        OutputDataStream result = instance.getDataOutputStream();
        assertNotNull(result);
    }

    /**
     * Test of getControlInputStream method, of class IcedRTPConnector.
     */
    @Test
    public void testGetControlInputStream() throws Exception {
        System.out.println("getControlInputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        PushSourceStream result = instance.getControlInputStream();
        assertNotNull(result);
    }

    /**
     * Test of getControlOutputStream method, of class IcedRTPConnector.
     */
    @Test
    public void testGetControlOutputStream() throws Exception {
        System.out.println("getControlOutputStream");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        OutputDataStream result = instance.getControlOutputStream();
        assertNotNull(result);
    }

    /**
     * Test of getReceiveBufferSize method, of class IcedRTPConnector.
     */
    @Test
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
    @Test
    public void testSetSendBufferSize() throws Exception {
        System.out.println("setSendBufferSize");
        int arg0 = 1234;
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        instance.setSendBufferSize(arg0);
    }

    /**
     * Test of getSendBufferSize method, of class IcedRTPConnector.
     */
    @Test
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
    @Test
    public void testGetRTCPBandwidthFraction() throws SocketException, SdpParseException {
        System.out.println("getRTCPBandwidthFraction");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        double expResult = -1;
        double result = instance.getRTCPBandwidthFraction();
        // Does not specify, but leaves this to the RTPManager
        assertEquals(expResult, result, 0);
    }

    /**
     * Test of getRTCPSenderBandwidthFraction method, of class IcedRTPConnector.
     */
    @Test
    public void testGetRTCPSenderBandwidthFraction() throws SocketException, SdpParseException {
        System.out.println("getRTCPSenderBandwidthFraction");
        IcedRTPConnector instance = new IcedRTPConnector(MEDIA);
        double expResult = -1;
        double result = instance.getRTCPSenderBandwidthFraction();

        // Does not specify, but leaves this to the RTPManager
        assertEquals(expResult, result, 0);
    }

    @Test
    public void testConnectionLocally() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector");
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};

        // Create a local peer for a yet unspecified remote peer
        IcePeer localPeer = IceFactory.createIcePeer("localPeer", localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeer remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);


        Assert.assertNotNull(session);

        // Establish the SDP connection
        new LoggingSdpExchanger(localPeer, remotePeer);

        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Start the state machines
        localPeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);
        remotePeer.start();

        Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(500);
        }

        Assert.assertEquals("ICE processing failed to finish in under 60 seconds", IceStatus.SUCCESS, localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to finish in under 60 seconds", IceStatus.SUCCESS, remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());
        PushSourceStream[] pss = new PushSourceStream[]{
            remoteSockets[0].getDataInputStream(),
            remoteSockets[0].getControlInputStream(),
            remoteSockets[1].getDataInputStream(),
            remoteSockets[1].getControlInputStream(),
            localSockets[0].getDataInputStream(),
            localSockets[0].getControlInputStream(),
            localSockets[1].getDataInputStream(),
            localSockets[1].getControlInputStream()
        };



        OutputDataStream[] ods = new OutputDataStream[]{
            localSockets[0].getDataOutputStream(),
            localSockets[0].getControlOutputStream(),
            localSockets[1].getDataOutputStream(),
            localSockets[1].getControlOutputStream(),
            remoteSockets[0].getDataOutputStream(),
            remoteSockets[0].getControlOutputStream(),
            remoteSockets[1].getDataOutputStream(),
            remoteSockets[1].getControlOutputStream()
        };

        for (int i = 0; i < ods.length; i++) {
            ods[i].write("Testing".concat("" + i).getBytes(), 0, 8);
        }

        // Wait for the data to arrive
        Thread.sleep(500);

        byte[] data = new byte[8];
        for (int i = 0; i < ods.length; i++) {
            // Test for the presense of the data

            Arrays.fill(data, (byte) 0);
            pss[i].read(data, 0, 8);
            Assert.assertEquals("Expecting to see Testing" + i + " Over the line, but didn't see that!", "Testing" + i, new String(data, 0, 8));
        }

        for (IcedRTPConnector conn : localSockets) {
            conn.close();
        }
        for (IcedRTPConnector conn : remoteSockets) {
            conn.close();
        }

    }

    @Test
    public void testConflictConnectionLocally() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector Conflict");
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};

        // Create a local peer for a yet unspecified remote peer
        IcePeer localPeer = IceFactory.createIcePeer("localPeer", localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeer remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);

        Assert.assertNotNull(session);

        // Establish the SDP connection
        new LoggingSdpExchanger(localPeer, remotePeer);

        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Prevent either peer from thinking it's controlled
        ((IcePeerImpl) localPeer).iceStatus = IceStatus.IN_PROGRESS;
        ((IcePeerImpl) remotePeer).iceStatus = IceStatus.IN_PROGRESS;

        // Start the state machines
        localPeer.start();
        remotePeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);


        Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(100);
        }

        Assert.assertEquals("ICE processing failed to complete successfully in under 60 seconds", IceStatus.SUCCESS, localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to complete successfully in under 60 seconds", IceStatus.SUCCESS, remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());

        // Test for ICE Role Conflict Resolution
        Assert.assertNotSame("Ice MUST resolve a role conflict!", localPeer.isLocalControlled(), remotePeer.isLocalControlled());

        PushSourceStream[] pss = new PushSourceStream[]{
            remoteSockets[0].getDataInputStream(),
            remoteSockets[0].getControlInputStream(),
            remoteSockets[1].getDataInputStream(),
            remoteSockets[1].getControlInputStream(),
            localSockets[0].getDataInputStream(),
            localSockets[0].getControlInputStream(),
            localSockets[1].getDataInputStream(),
            localSockets[1].getControlInputStream()
        };



        OutputDataStream[] ods = new OutputDataStream[]{
            localSockets[0].getDataOutputStream(),
            localSockets[0].getControlOutputStream(),
            localSockets[1].getDataOutputStream(),
            localSockets[1].getControlOutputStream(),
            remoteSockets[0].getDataOutputStream(),
            remoteSockets[0].getControlOutputStream(),
            remoteSockets[1].getDataOutputStream(),
            remoteSockets[1].getControlOutputStream()
        };

        for (int i = 0; i < ods.length; i++) {
            ods[i].write("Testing".concat("" + i).getBytes(), 0, 8);
        }

        // Wait for the data to arrive
        Thread.sleep(500);

        byte[] data = new byte[8];
        for (int i = 0; i < ods.length; i++) {
            // Test for the presense of the data

            Arrays.fill(data, (byte) 0);
            pss[i].read(data, 0, 8);
            Assert.assertEquals("Expecting to see Testing" + i + " Over the line, but didn't see that!", "Testing" + i, new String(data, 0, 8));
        }

        for (IcedRTPConnector conn : localSockets) {
            conn.close();
        }

        for (IcedRTPConnector conn : remoteSockets) {
            conn.close();
        }

    }

    @Test
    public void testAggressiveConnectionLocally() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector");
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};

        // Create a local peer for a yet unspecified remote peer
        IcePeer localPeer = IceFactory.createIcePeer("localPeer", true, localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeer remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);


        Assert.assertNotNull(session);

        // Establish the SDP connection
        new LoggingSdpExchanger(localPeer, remotePeer);

        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Start the state machines
        localPeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);
        remotePeer.start();

        Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(500);
        }

        Assert.assertEquals("ICE processing failed to finish in under 60 seconds", IceStatus.SUCCESS, localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to finish in under 60 seconds", IceStatus.SUCCESS, remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());

        PushSourceStream[] pss = new PushSourceStream[]{
            remoteSockets[0].getDataInputStream(),
            remoteSockets[0].getControlInputStream(),
            remoteSockets[1].getDataInputStream(),
            remoteSockets[1].getControlInputStream(),
            localSockets[0].getDataInputStream(),
            localSockets[0].getControlInputStream(),
            localSockets[1].getDataInputStream(),
            localSockets[1].getControlInputStream()
        };



        OutputDataStream[] ods = new OutputDataStream[]{
            localSockets[0].getDataOutputStream(),
            localSockets[0].getControlOutputStream(),
            localSockets[1].getDataOutputStream(),
            localSockets[1].getControlOutputStream(),
            remoteSockets[0].getDataOutputStream(),
            remoteSockets[0].getControlOutputStream(),
            remoteSockets[1].getDataOutputStream(),
            remoteSockets[1].getControlOutputStream()
        };

        for (int i = 0; i < ods.length; i++) {
            ods[i].write("Testing".concat("" + i).getBytes(), 0, 8);
        }

        // Wait for the data to arrive
        Thread.sleep(500);

        byte[] data = new byte[8];
        for (int i = 0; i < ods.length; i++) {
            // Test for the presense of the data

            Arrays.fill(data, (byte)0);
            pss[i].read(data, 0, 8);
            Assert.assertEquals("Expecting to see Testing" + i + " Over the line, but didn't see that!", "Testing" + i, new String(data, 0, 8));
        }

        for (IcedRTPConnector conn : localSockets) {
            conn.close();
        }
        for (IcedRTPConnector conn : remoteSockets) {
            conn.close();
        }

    }

    @Test
    public void testAggressiveConflictConnectionLocally() throws SocketException, SdpException, InterruptedException, IOException {
        System.out.println("Test RTP Connector Conflict");
        MediaDescription[] medias = new MediaDescription[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0].getMedia()),
            new IcedRTPConnector(medias[1].getMedia())};

        // Create a local peer for a yet unspecified remote peer
        IcePeer localPeer = IceFactory.createIcePeer("localPeer", true, localSockets);

        // Create an SDP offer based on this local peer
        SessionDescription session = localPeer.createOffer();
        // Create a "remote" peer from this SDP info
        IcePeer remotePeer = IceFactory.createIcePeer("remotePeer", true, remoteSockets);

        Assert.assertNotNull(session);

        // Establish the SDP connection
        new LoggingSdpExchanger(localPeer, remotePeer);

        Assert.assertEquals(IceStatus.NOT_STARTED, localPeer.getStatus());
        Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

        // Prevent either peer from thinking it's controlled
        ((IcePeerImpl) localPeer).iceStatus = IceStatus.IN_PROGRESS;
        ((IcePeerImpl) remotePeer).iceStatus = IceStatus.IN_PROGRESS;

        // Start the state machines
        localPeer.start();
        remotePeer.start();
        // Wait for the threads to run a tiny bit
        Thread.sleep(100);


        Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
        Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 60 seconds to pass
        while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(100);
        }

        Assert.assertEquals("ICE processing failed to complete successfully in under 60 seconds", IceStatus.SUCCESS, localPeer.getStatus());
        Assert.assertEquals("ICE processing failed to complete successfully in under 60 seconds", IceStatus.SUCCESS, remotePeer.getStatus());

        // Get the nominated connection
        Assert.assertNotNull(localPeer.getNominated());
        Assert.assertNotNull(remotePeer.getNominated());
        Assert.assertEquals(2, localPeer.getNominated().size());
        Assert.assertEquals(2, remotePeer.getNominated().size());

        // Test for ICE Role Conflict Resolution
        Assert.assertNotSame("Ice MUST resolve a role conflict!", localPeer.isLocalControlled(), remotePeer.isLocalControlled());

        PushSourceStream[] pss = new PushSourceStream[]{
            remoteSockets[0].getDataInputStream(),
            remoteSockets[0].getControlInputStream(),
            remoteSockets[1].getDataInputStream(),
            remoteSockets[1].getControlInputStream(),
            localSockets[0].getDataInputStream(),
            localSockets[0].getControlInputStream(),
            localSockets[1].getDataInputStream(),
            localSockets[1].getControlInputStream()
        };



        OutputDataStream[] ods = new OutputDataStream[]{
            localSockets[0].getDataOutputStream(),
            localSockets[0].getControlOutputStream(),
            localSockets[1].getDataOutputStream(),
            localSockets[1].getControlOutputStream(),
            remoteSockets[0].getDataOutputStream(),
            remoteSockets[0].getControlOutputStream(),
            remoteSockets[1].getDataOutputStream(),
            remoteSockets[1].getControlOutputStream()
        };

        for (int i = 0; i < ods.length; i++) {
            ods[i].write("Testing".concat("" + i).getBytes(), 0, 8);
        }

        // Wait for the data to arrive
        Thread.sleep(500);

        byte[] data = new byte[8];
        for (int i = 0; i < ods.length; i++) {
            // Test for the presense of the data

            Arrays.fill(data, (byte)0);
            pss[i].read(data, 0, 8);
            Assert.assertEquals("Expecting to see Testing" + i + " Over the line, but didn't see that!", "Testing" + i, new String(data, 0, 8));
        }

        for (IcedRTPConnector conn : localSockets) {
            conn.close();
        }

        for (IcedRTPConnector conn : remoteSockets) {
            conn.close();
        }

    }
}
