/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.io.IOException;
import java.util.Date;
import javax.sdp.Media;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author shadow
 */
public class StreamingTest  {

    public StreamingTest() {
    }


    @Test
    public void testRTPManagerStreaming() throws InterruptedException, IOException, SdpException {
        Media[] medias = new Media[2];
        SdpFactory factory = SdpFactory.getInstance();
        medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia();
        medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia();
        final IcedRTPConnector[] localSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0]),
            new IcedRTPConnector(medias[1])};
        final IcedRTPConnector[] remoteSockets = new IcedRTPConnector[]{
            new IcedRTPConnector(medias[0]),
            new IcedRTPConnector(medias[1])};

        // Create a local peer for a yet unspecified remote peer
        IcePeer localPeer = IceFactory.createIcePeer("localPeer", localSockets);
        // Create a "remote" peer
        IcePeer remotePeer = IceFactory.createIcePeer("localPeer", remoteSockets);

        Assert.assertNotNull(localPeer.createOffer());
        // Establish the SDP connection
        new LoggingSdpExchanger(localPeer,remotePeer);

        // Start the state machines
        localPeer.start();
        remotePeer.start();

        long startTime = new Date().getTime();
        // Wait for the state machines to die, or 30 seconds to pass
        while (new Date().getTime() - startTime < 30000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
            Thread.sleep(100);
        }


        // TODO: Create a stream from a file and test reception of the file
        
    }
}
