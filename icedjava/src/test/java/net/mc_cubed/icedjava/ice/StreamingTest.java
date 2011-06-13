/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import javax.sdp.Media;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.ice.IceStateMachine.AgentRole;
import net.mc_cubed.icedjava.stun.StunUtil;

/**
 *
 * @author shadow
 */
public class StreamingTest extends TestCase {

    public StreamingTest(String testName) {
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

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    // This test is a placeholder for now, but will do something eventually.
    public void testRTPManagerStreaming() throws InterruptedException, IOException, SdpException {
        InetSocketAddress STUN_SERVER = new InetSocketAddress(StunUtil.getStunServer(), StunUtil.STUN_PORT);
        InterfaceProfile defaultIf = IceUtil.getBestInterfaceCandidate(STUN_SERVER);
        Media[] medias = new Media[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia();
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia();
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{new IcedRTPConnector(STUN_SERVER, medias[0], defaultIf.getPublicIP()),
            new IcedRTPConnector(STUN_SERVER, medias[1], defaultIf.getPublicIP())};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{new IcedRTPConnector(STUN_SERVER, medias[0], defaultIf.getPublicIP()),
            new IcedRTPConnector(STUN_SERVER, medias[1], defaultIf.getPublicIP())};

        // Create a local peer for a yet unspecified remote peer
        IcePeerImpl localPeer = new IcePeerImpl("localPeer", AgentRole.CONTROLLING, localSockets);
        // Set local only mode
        localPeer.setLocalOnly(true);
        // Create a "remote" peer
        IcePeerImpl remotePeer = new IcePeerImpl("remotePeer", AgentRole.CONTROLLED, remoteSockets);
        // Set local only mode
        remotePeer.setLocalOnly(true);

        Assert.assertNotNull(localPeer.createOffer());
        // Establish the SDP connection
        localPeer.setSdpListener(remotePeer);
        remotePeer.setSdpListener(localPeer);

        // Start the state machines
        localPeer.start();
        remotePeer.start();

        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 30 seconds to pass
        while (new Date().getTime() - startTime < 30000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(100);
        }

        
    }
}
