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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 * @author Charles Chappell
 */
public class IcePeerTest extends TestCase {

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
        SdpFactory factory = SdpFactory.getInstance();
        IceSocket[] iceSockets = new IceSocket[]{
            IceFactory.createIceSocket(factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia()),
            IceFactory.createIceSocket(factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia())};
        IcePeerImpl instance = (IcePeerImpl) IceFactory.createIcePeer("localPeer", iceSockets);
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
        IceSocket[] iceSockets = new IceSocket[]{
            IceFactory.createIceSocket(factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"}).getMedia()),
            IceFactory.createIceSocket(factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"}).getMedia())};
        IcePeerImpl instance = (IcePeerImpl) IceFactory.createIcePeer("localPeer", iceSockets);
        SessionDescription session = instance.createOffer();
        System.out.println(session);

        instance.close();

    }

    public void testICESocket() throws SocketException, SdpException, InterruptedException, IOException, InvocationTargetException {
        final IceNegociationProgressForm form;
        if (!GraphicsEnvironment.isHeadless()) {
            IceNegociationProgressForm iceForm = null;
            try {
                iceForm = new IceNegociationProgressForm();
                iceForm.setTitle("ICESocket");
            } catch (Throwable t) {
            }

            form = iceForm;
        } else {
            form = null;
        }

        IcePeer localPeer = null;
        IcePeer remotePeer = null;

        try {
            MediaDescription[] medias = new MediaDescription[2];
            SdpFactory factory = SdpFactory.getInstance();
            medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
            medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
            final IceSocket[] localSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};
            final IceSocket[] remoteSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};

            // Create a local peer for a yet unspecified remote peer
            localPeer = IceFactory.createIcePeer("localPeer", localSockets);

            // Create a "remote" peer
            remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);

            // Establish the SDP connection
            new LoggingSdpExchanger(remotePeer, localPeer);
            //localPeer.setSdpListener(remotePeer);
            //remotePeer.setSdpListener(localPeer);

            // Start the local state machine.  This will send an SDP offer then
            //  wait for a reply.
            localPeer.start();

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

            if (form != null) {
                form.getLocalPeer().setModel(new PairStatusTableModel(((IcePeerImpl) localPeer).checkPairs));
                form.getRemotePeer().setModel(new PairStatusTableModel(((IcePeerImpl) remotePeer).checkPairs));

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        form.setVisible(true);
                    }
                });
            }

            // Start the state machines
            remotePeer.start();
            // Wait for the threads to run a tiny bit
            Thread.sleep(100);

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


            long startTime = new Date().getTime();
            // Wait for the state machines to die, or 60 seconds to pass
            while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
                Thread.sleep(500);

                if (form != null) {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            form.getLocalPeer().updateUI();
                            form.getRemotePeer().updateUI();
                        }
                    });
                }
            }

            Assert.assertEquals(IceStatus.SUCCESS, localPeer.getStatus());
            //System.out.println(localPeer.createOffer());
            Assert.assertEquals(IceStatus.SUCCESS, remotePeer.getStatus());
            //System.out.println(remotePeer.createOffer());

            // Get the nominated connection
            Assert.assertNotNull(localPeer.getNominated());
            Assert.assertNotNull(remotePeer.getNominated());
            Assert.assertEquals(2, localPeer.getNominated().size());
            Assert.assertEquals(2, remotePeer.getNominated().size());

            List<IceSocketChannel> outputChannels = new LinkedList<IceSocketChannel>();
            List<IceSocketChannel> inputChannels = new LinkedList<IceSocketChannel>();

            for (IceSocket socket : localSockets) {
                outputChannels.addAll(localPeer.getChannels(socket));
            }
            for (IceSocket socket : remoteSockets) {
                outputChannels.addAll(remotePeer.getChannels(socket));
                inputChannels.addAll(remotePeer.getChannels(socket));
            }
            for (IceSocket socket : localSockets) {
                inputChannels.addAll(localPeer.getChannels(socket));
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Testing {0} data channels...", outputChannels.size());
            for (int i = 0; i < outputChannels.size(); i++) {
                ByteBuffer outputBytes = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                outputChannels.get(i).write(outputBytes);
            }

            // A little delay for the network            
            Thread.sleep(250);

            for (int i = 0; i < inputChannels.size(); i++) {
                ByteBuffer expected = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                ByteBuffer inputBytes = ByteBuffer.allocate(30);
                inputChannels.get(i).read(inputBytes);
                Assert.assertEquals(0, inputBytes.compareTo(expected));
            }
        } finally {
            if (localPeer != null) {
                localPeer.setSdpListener(null);
                localPeer.close();
            }
            if (remotePeer != null) {
                remotePeer.setSdpListener(null);
                remotePeer.close();
            }

            if (form != null) {
                form.setVisible(false);
            }
        }

    }

    public void testAggressiveICESocket() throws SocketException, SdpException, InterruptedException, IOException, InvocationTargetException {
        final IceNegociationProgressForm form;
        if (!GraphicsEnvironment.isHeadless()) {
            IceNegociationProgressForm iceForm = null;
            try {
                iceForm = new IceNegociationProgressForm();
                iceForm.setTitle("AggressiveICESocket");
            } catch (Throwable t) {
            }

            form = iceForm;
        } else {
            form = null;
        }

        IcePeer localPeer = null;
        IcePeer remotePeer = null;

        try {
            MediaDescription[] medias = new MediaDescription[2];
            SdpFactory factory = SdpFactory.getInstance();
            medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
            medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
            final IceSocket[] localSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};
            final IceSocket[] remoteSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};

            // Create a local peer for a yet unspecified remote peer
            localPeer = IceFactory.createIcePeer("localPeer", true, localSockets);

            // Create a "remote" peer
            remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);

            // Establish the SDP connection
            new LoggingSdpExchanger(remotePeer, localPeer);
            //localPeer.setSdpListener(remotePeer);
            //remotePeer.setSdpListener(localPeer);

            // Start the local state machine.  This will send an SDP offer then
            //  wait for a reply.
            localPeer.start();

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.NOT_STARTED, remotePeer.getStatus());

            // Peek at the check pairs so we can see what ICE is actually doing during the test
            if (form != null) {
                form.getLocalPeer().setModel(new PairStatusTableModel(((IcePeerImpl) localPeer).checkPairs));
                form.getRemotePeer().setModel(new PairStatusTableModel(((IcePeerImpl) remotePeer).checkPairs));

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        form.setVisible(true);
                    }
                });
            }

            // Start the state machines
            remotePeer.start();
            // Wait for the threads to run a tiny bit
            Thread.sleep(100);

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


            long startTime = new Date().getTime();
            // Wait for the state machines to die, or 60 seconds to pass
            while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
                Thread.sleep(500);

                if (form != null) {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            form.getLocalPeer().updateUI();
                            form.getRemotePeer().updateUI();
                        }
                    });
                }
            }

            Assert.assertEquals(IceStatus.SUCCESS, localPeer.getStatus());
            //System.out.println(localPeer.createOffer());
            Assert.assertEquals(IceStatus.SUCCESS, remotePeer.getStatus());
            //System.out.println(remotePeer.createOffer());

            // Get the nominated connection
            Assert.assertNotNull(localPeer.getNominated());
            Assert.assertNotNull(remotePeer.getNominated());
            Assert.assertEquals(2, localPeer.getNominated().size());
            Assert.assertEquals(2, remotePeer.getNominated().size());

            List<IceSocketChannel> outputChannels = new LinkedList<IceSocketChannel>();
            List<IceSocketChannel> inputChannels = new LinkedList<IceSocketChannel>();

            for (IceSocket socket : localSockets) {
                outputChannels.addAll(localPeer.getChannels(socket));
            }
            for (IceSocket socket : remoteSockets) {
                outputChannels.addAll(remotePeer.getChannels(socket));
                inputChannels.addAll(remotePeer.getChannels(socket));
            }
            for (IceSocket socket : localSockets) {
                inputChannels.addAll(localPeer.getChannels(socket));
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Testing {0} data channels...", outputChannels.size());
            for (int i = 0; i < outputChannels.size(); i++) {
                ByteBuffer outputBytes = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                outputChannels.get(i).write(outputBytes);
            }

            // A little delay for the network            
            Thread.sleep(250);

            for (int i = 0; i < inputChannels.size(); i++) {
                ByteBuffer expected = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                ByteBuffer inputBytes = ByteBuffer.allocate(30);
                inputChannels.get(i).read(inputBytes);
                Assert.assertEquals(0, inputBytes.compareTo(expected));
            }
        } finally {
            if (localPeer != null) {
                localPeer.setSdpListener(null);
                localPeer.close();
            }
            if (remotePeer != null) {
                remotePeer.setSdpListener(null);
                remotePeer.close();
            }

            if (form != null) {
                form.setVisible(false);
            }
        }

    }

    public void testICESocketConflict() throws SocketException, SdpException, InterruptedException, IOException, InvocationTargetException {
        final IceNegociationProgressForm form;
        if (!GraphicsEnvironment.isHeadless()) {
            IceNegociationProgressForm iceForm = null;
            try {
                iceForm = new IceNegociationProgressForm();
                iceForm.setTitle("ICESocketConflict");
            } catch (Throwable t) {
            }

            form = iceForm;
        } else {
            form = null;
        }

        IcePeer localPeer = null;
        IcePeer remotePeer = null;

        try {
            MediaDescription[] medias = new MediaDescription[2];
            SdpFactory factory = SdpFactory.getInstance();
            medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
            medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
            final IceSocket[] localSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};
            final IceSocket[] remoteSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};

            // Create a local peer for a yet unspecified remote peer
            localPeer = IceFactory.createIcePeer("localPeer", localSockets);

            // Create a "remote" peer
            remotePeer = IceFactory.createIcePeer("remotePeer", remoteSockets);

            // Establish the SDP connection
            localPeer.setSdpListener(remotePeer);
            remotePeer.setSdpListener(localPeer);

            ((IceStateMachine) remotePeer).iceStatus = IceStatus.IN_PROGRESS;
            // Start the local state machine.  This will send an SDP offer then
            //  wait for a reply.
            localPeer.start();

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());

            // Peek at the check pairs so we can see what ICE is actually doing during the test
            if (form != null) {
                form.getLocalPeer().setModel(new PairStatusTableModel(((IcePeerImpl) localPeer).checkPairs));
                form.getRemotePeer().setModel(new PairStatusTableModel(((IcePeerImpl) remotePeer).checkPairs));

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        form.setVisible(true);
                    }
                });
            }

            // Start the state machines
            remotePeer.start();
            // Wait for the threads to run a tiny bit
            Thread.sleep(100);

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());

            long startTime = new Date().getTime();
            // Wait for the state machines to die, or 30 seconds to pass
            while (new Date().getTime() - startTime < 30000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
                Thread.sleep(500);

                if (form != null) {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            form.getLocalPeer().updateUI();
                            form.getRemotePeer().updateUI();
                        }
                    });
                }
            }

            Assert.assertEquals(IceStatus.SUCCESS, localPeer.getStatus());
            //System.out.println(localPeer.createOffer());
            Assert.assertEquals(IceStatus.SUCCESS, remotePeer.getStatus());
            //System.out.println(remotePeer.createOffer());

            // Get the nominated connection
            Assert.assertNotNull(localPeer.getNominated());
            Assert.assertNotNull(remotePeer.getNominated());
            Assert.assertEquals(2, localPeer.getNominated().size());
            Assert.assertEquals(2, remotePeer.getNominated().size());


            List<IceSocketChannel> outputChannels = new LinkedList<IceSocketChannel>();
            List<IceSocketChannel> inputChannels = new LinkedList<IceSocketChannel>();

            for (IceSocket socket : localSockets) {
                outputChannels.addAll(localPeer.getChannels(socket));
            }
            for (IceSocket socket : remoteSockets) {
                outputChannels.addAll(remotePeer.getChannels(socket));
                inputChannels.addAll(remotePeer.getChannels(socket));
            }
            for (IceSocket socket : localSockets) {
                inputChannels.addAll(localPeer.getChannels(socket));
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Testing {0} data channels...", outputChannels.size());
            for (int i = 0; i < outputChannels.size(); i++) {
                ByteBuffer outputBytes = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                outputChannels.get(i).write(outputBytes);
            }

            // A little delay for the network            
            Thread.sleep(250);

            for (int i = 0; i < inputChannels.size(); i++) {
                ByteBuffer expected = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                ByteBuffer inputBytes = ByteBuffer.allocate(30);
                inputChannels.get(i).read(inputBytes);
                Assert.assertEquals(0, inputBytes.compareTo(expected));
            }
        } finally {
            if (localPeer != null) {
                localPeer.setSdpListener(null);
                localPeer.close();
            }
            if (remotePeer != null) {
                remotePeer.setSdpListener(null);
                remotePeer.close();
            }

            if (form != null) {
                form.setVisible(false);
            }
        }
    }

    public void testAggressiveICESocketConflict() throws SocketException, SdpException, InterruptedException, IOException, InvocationTargetException {
        final IceNegociationProgressForm form;

        if (!GraphicsEnvironment.isHeadless()) {
            IceNegociationProgressForm iceForm = null;
            try {
                iceForm = new IceNegociationProgressForm();
                iceForm.setTitle("AggressiveICESocketConflict");
            } catch (Throwable t) {
            }

            form = iceForm;
        } else {
            form = null;
        }

        IcePeer localPeer = null;
        IcePeer remotePeer = null;

        try {
            MediaDescription[] medias = new MediaDescription[2];
            SdpFactory factory = SdpFactory.getInstance();
            medias[0] = factory.createMediaDescription("video", 0, 2, "RTP/AVP", new String[]{"26"});
            medias[1] = factory.createMediaDescription("audio", 0, 2, "RTP/AVP", new String[]{"8"});
            final IceSocket[] localSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};
            final IceSocket[] remoteSockets = new IceSocket[]{IceFactory.createIceSocket(medias[0].getMedia()),
                IceFactory.createIceSocket(medias[1].getMedia())};

            // Create a local peer for a yet unspecified remote peer
            localPeer = IceFactory.createIcePeer("localPeer", true, localSockets);

            // Create a "remote" peer
            remotePeer = IceFactory.createIcePeer("remotePeer", true, remoteSockets);

            // Establish the SDP connection
            localPeer.setSdpListener(remotePeer);
            remotePeer.setSdpListener(localPeer);

            ((IceStateMachine) remotePeer).iceStatus = IceStatus.IN_PROGRESS;
            // Start the local state machine.  This will send an SDP offer then
            //  wait for a reply.
            localPeer.start();

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


            // Peek at the check pairs so we can see what ICE is actually doing during the test
            if (form != null) {
                form.getLocalPeer().setModel(new PairStatusTableModel(((IcePeerImpl) localPeer).checkPairs));
                form.getRemotePeer().setModel(new PairStatusTableModel(((IcePeerImpl) remotePeer).checkPairs));

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        form.setVisible(true);
                    }
                });
            }

            // Start the state machines
            remotePeer.start();
            // Wait for the threads to run a tiny bit
            Thread.sleep(100);

            Assert.assertEquals(IceStatus.IN_PROGRESS, localPeer.getStatus());
            Assert.assertEquals(IceStatus.IN_PROGRESS, remotePeer.getStatus());


            long startTime = new Date().getTime();
            // Wait for the state machines to die, or 60 seconds to pass
            while (new Date().getTime() - startTime < 60000 && (localPeer.getStatus() == IceStatus.IN_PROGRESS || remotePeer.getStatus() == IceStatus.IN_PROGRESS)) {
                Thread.sleep(500);

                if (form != null) {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            form.getLocalPeer().updateUI();
                            form.getRemotePeer().updateUI();
                        }
                    });
                }
            }

            Assert.assertEquals(IceStatus.SUCCESS, localPeer.getStatus());
            //System.out.println(localPeer.createOffer());
            Assert.assertEquals(IceStatus.SUCCESS, remotePeer.getStatus());
            //System.out.println(remotePeer.createOffer());

            // Get the nominated connection
            Assert.assertNotNull(localPeer.getNominated());
            Assert.assertNotNull(remotePeer.getNominated());
            Assert.assertEquals(2, localPeer.getNominated().size());
            Assert.assertEquals(2, remotePeer.getNominated().size());


            List<IceSocketChannel> outputChannels = new LinkedList<IceSocketChannel>();
            List<IceSocketChannel> inputChannels = new LinkedList<IceSocketChannel>();

            for (IceSocket socket : localSockets) {
                outputChannels.addAll(localPeer.getChannels(socket));
            }
            for (IceSocket socket : remoteSockets) {
                outputChannels.addAll(remotePeer.getChannels(socket));
                inputChannels.addAll(remotePeer.getChannels(socket));
            }
            for (IceSocket socket : localSockets) {
                inputChannels.addAll(localPeer.getChannels(socket));
            }

            Logger.getLogger(getClass().getName()).log(Level.INFO, "Testing {0} data channels...", outputChannels.size());
            for (int i = 0; i < outputChannels.size(); i++) {
                ByteBuffer outputBytes = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                outputChannels.get(i).write(outputBytes);
            }

            // A little delay for the network            
            Thread.sleep(250);

            for (int i = 0; i < inputChannels.size(); i++) {
                ByteBuffer expected = ByteBuffer.wrap("Testing".concat("" + i).getBytes());
                ByteBuffer inputBytes = ByteBuffer.allocate(30);
                inputChannels.get(i).read(inputBytes);
                Assert.assertEquals(0, inputBytes.compareTo(expected));
            }
        } finally {
            if (localPeer != null) {
                localPeer.setSdpListener(null);
                localPeer.close();
            }
            if (remotePeer != null) {
                remotePeer.setSdpListener(null);
                remotePeer.close();
            }

            if (form != null) {
                form.setVisible(false);
            }
        }
    }
}
