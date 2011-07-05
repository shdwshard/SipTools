/*
 * Copyright 2010 Charles Chappell.
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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.ice.pmp.IcePMPBridge;
import net.mc_cubed.icedjava.ice.upnp.IceUPNPBridge;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.AttributeFactory;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.FingerprintAttribute;
import net.mc_cubed.icedjava.packet.attribute.IceControlledAttribute;
import net.mc_cubed.icedjava.packet.attribute.IceControllingAttribute;
import net.mc_cubed.icedjava.packet.attribute.IntegrityAttribute;
import net.mc_cubed.icedjava.packet.attribute.NullAttribute;
import net.mc_cubed.icedjava.packet.attribute.RealmAttribute;
import net.mc_cubed.icedjava.packet.attribute.UsernameAttribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import net.mc_cubed.icedjava.stun.StunReply;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.TransportType;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

/**
 * Implements the ICE state machine
 *
 * @author Charles Chappell
 * @since 1.0
 */
abstract class IceStateMachine extends BaseFilter implements Runnable,
        SDPListener, IcePeer {

    /*
     * Class constants
     */
    public static final String ROLE_CONFLICT_REASON = "Role Conflict Error";
    public static final int ROLE_CONFLICT = 487;
    public static final String SDP_UFRAG = "ice-ufrag";
    public static final String SDP_PWD = "ice-pwd";
    public static final String SDP_ICE_LITE = "ice-lite";
    public static final String CANDIDATE_NAME = "candidate";
    public static final String PROP_PEERID = "peerId";
    public static final int UFRAG_LENGTH = 4;
    public static final int PASSWORD_LENGTH = 22;
    public static final int PEER_REFLEXIVE_PRIORITY = CandidateType.PEER_REFLEXIVE.getPriority();
    private String localUFrag;
    private String localPassword;
    private String remoteUFrag;
    private String remotePassword;
    private Logger log = Logger.getLogger(IceStateMachine.class.getName());
    private int iceInterval = 500;
    private ScheduledFuture task = null;
    private long lastSent = 0;
    private boolean sdpTimeout = true;
    private long refreshDelay = 15000; // 15 seconds
    //private long sdpTimeoutTime = 15000;
    //private long lastTouch = 0;
    private NominationType nomination = NominationType.REGULAR;
    private boolean restartFlag = false;
    protected IceStatus iceStatus = IceStatus.NOT_STARTED;
    private AgentRole localRole = AgentRole.CONTROLLING;
    // Initialized by the constructor
    private boolean icelite;
    private SDPListener sdpListener;
    private InetSocketAddress stunServer = StunUtil.getCachedStunServerSocket();
    private InterfaceProfile defaultInterface = IceUtil.getBestInterfaceCandidate(StunUtil.getCachedStunServerSocket());
    private SdpFactory sdpFactory = SdpFactory.getInstance();
    // These are final to avoid the list being pulled out from under a thread
    private final List<IceSocket> iceSockets = new ArrayList<IceSocket>();
    private final Map<IceSocket, Media> mediaCandidates = new LinkedHashMap<IceSocket, Media>();
    private final Queue<CandidatePair> triggeredCheckQueue = new LinkedList<CandidatePair>();
    //private final Queue<SessionDescription> offers = new ConcurrentLinkedQueue<SessionDescription>();
    protected final Map<IceSocket, List<CandidatePair>> checkPairs = new ConcurrentHashMap<IceSocket, List<CandidatePair>>();
    //private final Map<CandidateType, Integer> priorities = new HashMap<CandidateType, Integer>();
    private final Map<IceSocket, List<LocalCandidate>> socketCandidateMap = new LinkedHashMap<IceSocket, List<LocalCandidate>>();
    //private Timer checktimer;
    private final long tieBreaker;
    protected static SecureRandom random = new SecureRandom();
    protected final Map<IceSocket, List<CandidatePair>> nominated = new HashMap<IceSocket, List<CandidatePair>>();
    protected final Map<IceSocket, List<CandidatePair>> using = new HashMap<IceSocket, List<CandidatePair>>();
    protected final List<InterfaceProfile> interfaceData;
    @Inject
    @DiscoveryMechanism
    Instance<AddressDiscovery> discoveryMechanisms;
    @Inject
    @DiscoveryMechanism
    Instance<CandidateDiscovery> localCandidateDiscoveryMechs;
    boolean localOnly = false;
    boolean sendKeepalives = false;
    private long lastRemoteVersion = 0;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        getThreadpool().shutdownNow();
        close();
    }

    public boolean isSendKeepalives() {
        return sendKeepalives;
    }

    public void setSendKeepalives(boolean sendKeepalives) {
        this.sendKeepalives = sendKeepalives;
    }

    /**
     * Sets a flag used for testing only to restrict the scope of the ICE tests
     * to only use local type IP/port combinations
     * 
     * @param localOnlyFlag if true, only use local IPs.
     */
    void setLocalOnly(boolean localOnlyFlag) {
        this.localOnly = localOnlyFlag;
    }

    protected abstract ScheduledExecutorService getThreadpool();

    public NominationType getNomination() {
        return nomination;
    }

    public void setNomination(NominationType nomination) {
        if (!isLocalControlled() && nomination == NominationType.AGGRESSIVE) {
            throw new IllegalArgumentException("Aggressive Nomination is only "
                    + "supported for the controlling agent!");
        }
        this.nomination = nomination;

        if (getStatus() != IceStatus.NOT_STARTED) {
            doReset(isLocalControlled(), true);
        }
    }

    @Override
    public long getTieBreaker() {
        return tieBreaker;
    }

    public SDPListener getSdpListener() {
        return sdpListener;
    }

    @Override
    public void setSdpListener(SDPListener sdpListener) {
        this.sdpListener = sdpListener;
    }

    @Override
    public Map<IceSocket, List<CandidatePair>> getNominated() {
        return nominated;
    }

    public Map<IceSocket, List<CandidatePair>> getUsing() {
        return using;
    }

    /**
     * The designated constructor
     * @param listener The listener to send SDP updates to
     * @param role The role of this state machine
     * @param icelite true if we're running a "lite" ICE state machine, false otherwise
     * @param nomination The nomination type used by this ICE machine, either
     * aggressive or normal.
     */
    public IceStateMachine(SDPListener listener, boolean icelite, NominationType nomination) {
        this.sdpListener = listener;
        this.icelite = icelite;
        this.nomination = nomination;

        // Initialize internal state
        this.tieBreaker = random.nextLong();
        this.localUFrag = generateHashString(UFRAG_LENGTH);
        this.localPassword = generateHashString(PASSWORD_LENGTH);

        interfaceData = IceUtil.getInterfaceCandidates(stunServer);
    }

    /**
     * Convenience Constructor
     * @param listener The listener to send SDP updates to
     * @param role The role of this state machine
     * @param icelite true if we're running a "lite" ICE state machine, false otherwise
     */
    public IceStateMachine(SDPListener listener, boolean icelite) {
        this(listener, icelite, NominationType.REGULAR);
    }

    /**
     * Constructs a full Implementation ICE state machine
     * @param listener The listener to send SDP updates to
     */
    public IceStateMachine(SDPListener listener) {
        this(listener, false);
    }

    /**
     * Gets the global attributes associated with this ICE state machine.
     * This generally includes the ICE ufrag and password, and additionally
     * may indicate whether this state machine is a lite implementation
     *
     * @return the global SDP attributes for this state machine
     */
    @Override
    public List<Attribute> getGlobalAttributes() {
        SdpFactory factory = SdpFactory.getInstance();
        LinkedList<Attribute> retval = new LinkedList<Attribute>();
        retval.add(factory.createAttribute(SDP_PWD,
                localPassword));
        retval.add(factory.createAttribute(SDP_UFRAG,
                localUFrag));

        // TODO: Implement ICE-LITE processing for public hosts
        if (icelite) {
            retval.add(factory.createAttribute(SDP_ICE_LITE, null));
        }

        return retval;

    }

    /**
     * Based on the current ICE processing state, returns the best default C line
     * for use in SDP.
     *
     * @return an SDP C line appropriate for this ICE State machine
     * @throws SdpException
     */
    public Connection getDefaultConnection() throws SdpException {
        String addrType = "";

        InetAddress addr = getDefaultConnectionBasis();

        if (addr instanceof Inet6Address) {
            addrType = Connection.IP6;
        } else if (addr instanceof Inet4Address) {
            addrType = Connection.IP4;
        }
        return sdpFactory.createConnection(Connection.IN, addrType,
                addr.getHostAddress());


    }

    private InetAddress getDefaultConnectionBasis() {
        if (nominated.isEmpty() || nominated.values().iterator().next() == null
                || nominated.values().iterator().next().get(0) == null) {
            return defaultInterface.getAddress();
        } else {
            // Use the first nominated candidate to form the basis of our C line
            CandidatePair candidate = nominated.values().iterator().next().get(0);
            return candidate.getLocalCandidate().getAddress();
        }
    }

    public void sendSessionUpdate() {
        try {
            lastSent = new Date().getTime();

            List<Attribute> iceAttributes = getGlobalAttributes();
            List<MediaDescription> iceMedias = getMediaDescriptions();
            Connection conn = getDefaultConnection();
            Origin origin = sdpFactory.createOrigin("-", hashCode(),
                    new Date().getTime(), conn.getNetworkType(),
                    conn.getAddressType(), conn.getAddress());

            setSdpTimeout(false);

            if (sdpListener != null) {
                sdpListener.updateMedia(origin, conn, iceAttributes, iceMedias);
            }
        } catch (SdpException ex) {
            Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, "Caught exception sending an SDP update", ex);
        }
    }

    /*
     * This method implements the ICE State machine.  It is called periodically
     */
    @Override
    public synchronized void run() {
        log.entering(getClass().getName(), "run");
        // Ice negociation phase
        try {

            if (iceStatus == IceStatus.IN_PROGRESS) {
                // First check for any finished pairs
                for (Entry<IceSocket, List<CandidatePair>> pairsEntry : checkPairs.entrySet()) {
                    IceSocket socket = pairsEntry.getKey();
                    List<CandidatePair> pairs = pairsEntry.getValue();
                    for (CandidatePair pair : getPairsInState(pairs, PairState.IN_PROGRESS)) {
                        if (pair.getReplyFuture() != null) {
                            if (pair.getReplyFuture().isDone()) {
                                checkTestResult(socket, pair);
                            }
                        } else {
                            log.log(Level.SEVERE, "BUG: Found an In-Progress pair with a null future: {0}", pair);
                            pair.setState(PairState.FAILED);
                        }
                    }
                }

                // If we're controlling, send the SDP offer if timeout has occured,
                // or it hasn't been sent yet.
                if (localRole == AgentRole.CONTROLLING) {
                    // This prevents packet storms
                    if (isSdpTimeout()) {
                        sendSessionUpdate();
                    }
                }

                // Do we have a remote uFrag and Password?
                if (remoteUFrag == null || remotePassword == null || checkPairs == null) {
                    log.log(Level.INFO, "{0} waiting on peer for offer in {1} role.", new Object[]{getPeerId(), localRole});
                    checkStatus();
                } else {
                    // Check ICE status
                    checkStatus();
                    if (iceStatus == IceStatus.SUCCESS) {
                        return;
                    }

                    // Form check pairs
                    if (checkPairs != null) {
                        for (List<CandidatePair> candidatepairs : checkPairs.values()) {
                            Collections.sort(candidatepairs, new CandidatePairComparison());
                        }
                    }

                    boolean didTest = false;

                    // If there's a triggered check, do it now
                    CandidatePair triggeredPair = triggeredCheckQueue.poll();
                    if (triggeredPair != null) {
                        log.log(Level.FINE, "TriggeredTest: {0}:{1} ({2}) -> {3}:{4}",
                                new Object[]{triggeredPair.getLocalCandidate().getAddress(),
                                    triggeredPair.getLocalCandidate().getPort(),
                                    triggeredPair.getLocalCandidate().getComponentId(),
                                    triggeredPair.getRemoteCandidate().getAddress(),
                                    triggeredPair.getRemoteCandidate().getPort()});
                        runOneTest(triggeredPair.getLocalCandidate().getIceSocket(), triggeredPair);
                        didTest = true;
                    } else {
                        // Check for a waiting pair in each channel
                        for (Entry<IceSocket, List<CandidatePair>> pairsEntry : checkPairs.entrySet()) {
                            IceSocket socket = pairsEntry.getKey();
                            List<CandidatePair> pairs = pairsEntry.getValue();
                            // No waiting pairs, is there a successful pair?
                            if (pairsInState(pairs, PairState.WAITING) == 0) {
                                // No, unfreeze pairs if they exist
                                if (pairsInState(pairs, PairState.SUCCEEDED) == 0) {

                                    // Find the current lowest component ID
                                    short lowestComponentId = 256;
                                    for (CandidatePair pair : pairs) {
                                        if (pair.getComponentId() < lowestComponentId) {
                                            lowestComponentId = pair.getComponentId();
                                        }
                                    }

                                    // Unfreeze all candidates for this component ID
                                    for (CandidatePair pair : pairs) {
                                        if (pair.getComponentId() == lowestComponentId
                                                && pair.getState() == PairState.FROZEN) {
                                            pair.setState(PairState.WAITING);
                                        }
                                    }
                                }
                            }
                            if (pairsInState(pairs, PairState.WAITING) != 0) {
                                // Non frozen pairs exist, test one, then break out of the loop
                                CandidatePair pair = getFirstWaitingPair(pairs);
                                runOneTest(pair.getLocalCandidate().getIceSocket(), pair);
                                didTest = true;
                                break;
                            }
                        }

                        // Test for completion
                        if (!didTest && !checkPairs.isEmpty()) {
                            // Check for pairs still in-progress
                            for (List<CandidatePair> pairs : checkPairs.values()) {
                                int inProgressPairCounts = pairsInState(pairs, PairState.IN_PROGRESS);
                                if (inProgressPairCounts > 0) {

                                    // End this round
                                    log.log(Level.FINE, "{0} Waiting for {1} tests to finish...", new Object[]{localUFrag, inProgressPairCounts});
                                    return;
                                }
                            }
                            for (Entry<IceSocket, List<CandidatePair>> pairsEntry : checkPairs.entrySet()) {
                                IceSocket socket = pairsEntry.getKey();
                                List<CandidatePair> pairs = pairsEntry.getValue();

                                // If controller, nominate a pair, or restart ICE
                                if (localRole == AgentRole.CONTROLLING) {
                                    List<CandidatePair> successPairs = new LinkedList<CandidatePair>();
                                    for (CandidatePair cp : pairs) {
                                        if (cp.getState() == PairState.SUCCEEDED) {
                                            successPairs.add(cp);
                                        }
                                    }
                                    if (!successPairs.isEmpty()) {
                                        Map<Short, List<CandidatePair>> separatedCandidates = separateByComponent(successPairs);

                                        for (List<CandidatePair> nominateOne : separatedCandidates.values()) {
                                            // Sort to get the highest priority pair on top
                                            Collections.sort(nominateOne, new CandidatePairComparison());
                                            // Nominate the highest priority pair that succeeded
                                            CandidatePair nominatePair = nominateOne.get(0);

                                            nominate(nominatePair);

                                            // Send the nomination to the remote ICE peer
                                            doIceTest(
                                                    nominatePair,
                                                    localUFrag,
                                                    remoteUFrag,
                                                    remotePassword,
                                                    isLocalControlled(),
                                                    PEER_REFLEXIVE_PRIORITY,
                                                    tieBreaker,
                                                    true);
                                        }
                                        checkPairs.putAll(nominated);
                                    }
                                }
                            }
                            //iceStatus = IceStatus.SUCCESS;
                            checkStatus();

                            if (iceStatus == IceStatus.SUCCESS) {
                                sendSessionUpdate();
                            } else {
                                if (isLocalControlled()) {
                                    doReset(isLocalControlled(), true);
                                    return;
                                }

                            }
                        }
                    }
                }
            }

            // Upkeep Phase
            if (iceStatus == IceStatus.SUCCESS) {
                if (sendKeepalives) {
                    if (new Date().getTime() - lastSent > refreshDelay) {
                        lastSent = new Date().getTime();
                        for (List<CandidatePair> pairList : nominated.values()) {
                            for (CandidatePair pair : pairList) {
                                /**
                                 * Repeat connectivity checks at a regular interval on
                                 * nominated candidates to keep the candidates available
                                 */
                                IceReply result = doIceTest(
                                        pair,
                                        localUFrag,
                                        remoteUFrag,
                                        remotePassword,
                                        isLocalControlled(),
                                        PEER_REFLEXIVE_PRIORITY,
                                        tieBreaker,
                                        true).get();
                                if (result != null) {
                                    if (pair == null || pair.getLocalCandidate() == null || pair.getRemoteCandidate() == null) {
                                        log.log(Level.WARNING, "Got a strange candidate pair: {0}", pair);
                                    } else {
                                        log.log(Level.FINEST, "Keepalive: {0}:{1} -> {2}:{3} - {4} - {5}", new Object[]{
                                                    pair.getLocalCandidate().getAddress(),
                                                    pair.getLocalCandidate().getPort(),
                                                    pair.getRemoteCandidate().getAddress(),
                                                    pair.getRemoteCandidate().getPort(),
                                                    pair.getState(),
                                                    (result.isSuccess()) ? result.getMappedAddress() : result.getErrorReason()});


                                    }
                                } else {
                                    log.log(Level.WARNING, "Got a null reply from an ICE test during keepalive.  This is abnormal. {0}:{1} -> {2}:{3} - {4}", new Object[]{pair.getLocalCandidate().getAddress(), pair.getLocalCandidate().getPort(), pair.getRemoteCandidate().getAddress(), pair.getRemoteCandidate().getPort(), pair.getState()});
                                }

                            }
                        }
                    }
                } else {
                    // Stop executing the ice loop, but don't interrupt already executing processes.
                    task.cancel(false);
                }

            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                log.log(Level.WARNING, "Got interrupted Exception", ex);
            } else {
                log.log(Level.SEVERE, "Got an unchecked exception in the ICE State Machine", ex);
                throw new RuntimeException(ex);
            }
        }
        log.exiting(getClass().getName(), "run");
    }

    protected void startOneTest(IceSocket socket, CandidatePair pair) {
        synchronized (pair) {
            try {
                if (pair.getState() == PairState.WAITING) {
                    pair.setState(PairState.IN_PROGRESS);
                    Future<IceReply> resultFuture = doIceTest(
                            pair,
                            localUFrag, // Local UserFrag
                            remoteUFrag, // Remote UserFrag
                            remotePassword, // Password
                            isLocalControlled(),
                            PEER_REFLEXIVE_PRIORITY,
                            tieBreaker,
                            nomination == NominationType.AGGRESSIVE);
                    pair.setReplyFuture(resultFuture);
                }
            } catch (Exception ex) {
                // Ensure any errors will knock the pair out of the IN_PROGRESS state
                if (pair.getState() == PairState.IN_PROGRESS) {
                    pair.setState(PairState.FAILED);
                }
            }
        }
    }

    protected void checkTestResult(IceSocket socket, CandidatePair pair) {
        // Fast abort: we shouldn't check this if it's not done.        
        if (pair.getReplyFuture() == null || pair.getReplyFuture().isDone()) {
            try {
                synchronized (pair) {
                    IceReply result = pair.getReplyFuture().get();
                    if (result.isSuccess()) {

                        // Check for a Peer Reflexive Candidate
                        InetSocketAddress mappedAddress = result.getMappedAddress();
                        List<LocalCandidate> lclist = socketCandidateMap.get(socket);

                        /**
                         * Check whether the reply address matched the address 
                         *  we thought it was sent from.  If not, this test did
                         *  not succeed, and might generate a reflexive pair.
                         */
                        if (!mappedAddress.getAddress().getHostAddress().equalsIgnoreCase(pair.getLocalCandidate().getAddress().getHostAddress())
                                || mappedAddress.getPort() != pair.getLocalCandidate().getPort()) {
                            pair.setState(PairState.FAILED);
                            log.log(Level.INFO, "Test failed due to mismatching Address.  Expected {0} but got {1}", new Object[]{pair.getLocalCandidate().getSocketAddress(), mappedAddress});

                            /**
                             * Search for a matching pair.  We should set any 
                             * matching pairs to success as an optimization.
                             */
                            boolean matched = false;
                            for (LocalCandidate candidate : lclist) {
                                if (mappedAddress.getAddress().getHostAddress().equalsIgnoreCase(candidate.getAddress().getHostAddress())
                                        && mappedAddress.getPort() == candidate.getPort()) {
                                    matched = true;
                                    break;
                                }
                            }

                            /**
                             * If we didn't match any candidates, then create a
                             *  a peer reflexive candidate to represent this 
                             *  result.
                             */
                            if (!matched) {
                                // Generate a peer reflexive candidate, mark it succeeded and add it to the list
                                LocalCandidate local = new LocalCandidate(
                                        pair.getLocalCandidate().getOwner(),
                                        pair.getLocalCandidate().getIceSocket(),
                                        CandidateType.PEER_REFLEXIVE,
                                        mappedAddress.getAddress(),
                                        mappedAddress.getPort(), pair.getLocalCandidate());
                                CandidatePair peerReflexPair = new CandidatePair(local, pair.getRemoteCandidate(), isLocalControlled());
                                peerReflexPair.setState(PairState.SUCCEEDED);
                                log.log(Level.FINE, "New peer reflexive pair: {0} <-> {1}", new Object[]{peerReflexPair.getLocalCandidate().getSocketAddress(), peerReflexPair.getRemoteCandidate().getSocketAddress()});

                                checkPairs.get(socket).add(peerReflexPair);
                            }
                        } else {
                            pair.setState(PairState.SUCCEEDED);

                            // Unfreeze other pairs with the same foundation
                            for (IceSocket updateSocket : checkPairs.keySet()) {
                                for (CandidatePair candidate : checkPairs.get(updateSocket)) {
                                    if (candidate.getFoundation().compareTo(pair.getFoundation()) == 0
                                            && candidate.getState() == PairState.FROZEN) {
                                        candidate.setState(PairState.WAITING);
                                    }
                                }
                            }

                        }

                        /**
                         * Special case:
                         * Aggressive Nomination type nominates the first 
                         * successful test on each channel/component, so we 
                         * should re-freeze all other tests in the WAITING state
                         * with the same channel/componentId
                         */
                        if (nomination == NominationType.AGGRESSIVE) {
                            nominate(pair);
                            for (CandidatePair checkPair : checkPairs.get(pair.getLocalCandidate().getIceSocket())) {
                                if (checkPair.getComponentId() == pair.getComponentId() && checkPair.getState() == PairState.WAITING) {
                                    checkPair.setState(PairState.FROZEN);
                                }
                            }
                        }
                    } else {
                        if (result.getErrorCode() == ROLE_CONFLICT) {
                            long remoteTieBreaker;
                            if (result.getAttribute(AttributeType.ICE_CONTROLLED) != null) {
                                IceControlledAttribute remote = (IceControlledAttribute) result.getAttribute(AttributeType.ICE_CONTROLLED);
                                remoteTieBreaker = remote.getNumber();
                            } else if (result.getAttribute(AttributeType.ICE_CONTROLLING) != null) {
                                IceControllingAttribute remote = (IceControllingAttribute) result.getAttribute(AttributeType.ICE_CONTROLLING);
                                remoteTieBreaker = remote.getNumber();
                            } else {
                                throw new java.lang.IllegalStateException("Got an ICE packet without an ICE_CONTROLLED or ICE_CONTROLLING attribute");
                            }

                            final boolean control;
                            if (remoteTieBreaker >= tieBreaker) {
                                // We switch to controlled
                                control = false;
                            } else {
                                // We switch to controlling
                                control = true;
                            }

                            setLocalControlled(control);

                            return;
                        }
                        pair.setState(PairState.FAILED);
                    }
                    log.log(Level.FINE, "{0}:{1} -> {2}:{3} - {4} - {5}", new Object[]{pair.getLocalCandidate().getAddress(), pair.getLocalCandidate().getPort(), pair.getRemoteCandidate().getAddress(), pair.getRemoteCandidate().getPort(), pair.getState(), (result.isSuccess()) ? result.getMappedAddress() : result.getErrorReason()});
                }
            } catch (ExecutionException ex) {
                log.log(Level.WARNING, "Caught an exception on a finished future.  This is probably a bug", ex);
            } catch (InterruptedException ex) {
                log.log(Level.WARNING, "Caught an exception on a finished future.  This is probably a bug", ex);
            } finally {
                // Ensure there won't be any pairs still in progress after this function ends
                if (pair.getState() == PairState.IN_PROGRESS) {
                    pair.setState(PairState.FAILED);
                }
            }
        }

    }

    @Deprecated
    protected void runOneTest(IceSocket socket, CandidatePair pair) {
        try {
            synchronized (pair) {
                if (pair.getState() == PairState.WAITING) {
                    pair.setState(PairState.IN_PROGRESS);
                    IceReply result = doIceTest(
                            pair,
                            localUFrag, // Local UserFrag
                            remoteUFrag, // Remote UserFrag
                            remotePassword, // Password
                            isLocalControlled(),
                            PEER_REFLEXIVE_PRIORITY,
                            tieBreaker,
                            nomination == NominationType.AGGRESSIVE).get();
                    if (result.isSuccess()) {
                        pair.setState(PairState.SUCCEEDED);

                        // Check for a Peer Reflexive Candidate
                        InetSocketAddress mappedAddress = result.getMappedAddress();
                        List<LocalCandidate> lclist = socketCandidateMap.get(socket);
                        boolean matched = false;
                        for (LocalCandidate candidate : lclist) {
                            if (candidate.getSocketAddress().equals(mappedAddress)) {
                                matched = true;
                                break;
                            }
                        }

                        if (!matched) {
                            // Generate a peer reflexive candidate, mark it succeeded and add it to the list
                            LocalCandidate local = new LocalCandidate(
                                    pair.getLocalCandidate().getOwner(),
                                    pair.getLocalCandidate().getIceSocket(),
                                    CandidateType.PEER_REFLEXIVE,
                                    mappedAddress.getAddress(),
                                    mappedAddress.getPort(), pair.getLocalCandidate());
                            CandidatePair peerReflexPair = new CandidatePair(local, pair.getRemoteCandidate(), isLocalControlled());
                            peerReflexPair.setState(PairState.SUCCEEDED);
                            log.log(Level.FINE, "New peer reflexive pair: {0} <-> {1}", new Object[]{peerReflexPair.getLocalCandidate().getSocketAddress(), peerReflexPair.getRemoteCandidate().getSocketAddress()});

                            checkPairs.get(socket).add(peerReflexPair);
                        } else {
                            // Unfreeze other pairs with the same foundation
                            for (IceSocket updateSocket : checkPairs.keySet()) {
                                for (CandidatePair candidate : checkPairs.get(updateSocket)) {
                                    if (candidate.getFoundation().compareTo(pair.getFoundation()) == 0
                                            && candidate.getState() == PairState.FROZEN) {
                                        candidate.setState(PairState.WAITING);
                                    }
                                }
                            }

                        }

                        /**
                         * Special case:
                         * Aggressive Nomination type nominates the first 
                         * successful test on each channel/component, so we 
                         * should re-freeze all other tests in the WAITING state
                         * with the same channel/componentId
                         */
                        if (nomination == NominationType.AGGRESSIVE) {
                            nominate(pair);
                            for (CandidatePair checkPair : checkPairs.get(pair.getLocalCandidate().getIceSocket())) {
                                if (checkPair.getComponentId() == pair.getComponentId() && checkPair.getState() == PairState.WAITING) {
                                    checkPair.setState(PairState.FROZEN);
                                }
                            }

                        }
                    } else {
                        if (result.getErrorCode() == ROLE_CONFLICT) {
                            long remoteTieBreaker;
                            if (result.getAttribute(AttributeType.ICE_CONTROLLED) != null) {
                                IceControlledAttribute remote = (IceControlledAttribute) result.getAttribute(AttributeType.ICE_CONTROLLED);
                                remoteTieBreaker = remote.getNumber();
                            } else if (result.getAttribute(AttributeType.ICE_CONTROLLING) != null) {
                                IceControllingAttribute remote = (IceControllingAttribute) result.getAttribute(AttributeType.ICE_CONTROLLING);
                                remoteTieBreaker = remote.getNumber();
                            } else {
                                throw new java.lang.IllegalStateException("Got an ICE packet without an ICE_CONTROLLED or ICE_CONTROLLING attribute");
                            }

                            final boolean control;
                            if (remoteTieBreaker >= tieBreaker) {
                                // We switch to controlled
                                control = false;
                            } else {
                                // We switch to controlling
                                control = true;
                            }

                            setLocalControlled(control);

                            return;
                        }
                        pair.setState(PairState.FAILED);
                    }
                    log.log(Level.FINE, "{0}:{1} -> {2}:{3} - {4} - {5}", new Object[]{pair.getLocalCandidate().getAddress(), pair.getLocalCandidate().getPort(), pair.getRemoteCandidate().getAddress(), pair.getRemoteCandidate().getPort(), pair.getState(), (result.isSuccess()) ? result.getMappedAddress() : result.getErrorReason()});
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, "Caught Interrupted Exception during an ICE/STUN test", ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, "Caught Execution Exception during an ICE/STUN test", ex);
        } finally {
            // Ensure there won't be any pairs still in progress after this function ends
            if (pair.getState() == PairState.IN_PROGRESS) {
                pair.setState(PairState.FAILED);
            }
        }


    }

    @Override
    public synchronized void start() {
        if (task == null || task.isDone() == true) {
            setIceStatus(IceStatus.IN_PROGRESS);

            // Do first run now
            run();
            task = getThreadpool().scheduleAtFixedRate(this, iceInterval, iceInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Delegates to stop(boolean) to implement a non-immediate stop
     */
    public void stop() {
        stop(false);
    }

    /**
     * Stops the ICE state machine
     *
     * @param immediate interrupt the processing thread if true, let it stop
     * gracefully if false
     */
    public synchronized void stop(boolean immediate) {
        if (task != null && !task.isDone()) {
            task.cancel(immediate);
        }

        if (immediate) {
            getThreadpool().shutdownNow();
        } else {
            getThreadpool().shutdown();
        }
        try {
            getThreadpool().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, "Exception during threadpool shutdown", ex);
        }
    }

    /**
     * @return the sdpTimeout
     */
    @Override
    public boolean isSdpTimeout() {
        return this.sdpTimeout;
//        return (new Date().getTime() - lastTouch > sdpTimeoutTime);
    }

    /**
     * @param sdpTimeout the sdpTimeout to set
     */
    @Override
    public void setSdpTimeout(boolean sdpTimeout) {
        this.sdpTimeout = sdpTimeout;
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        if (ctx.getMessage() instanceof StunPacket) {
            StunPacket stunPacket = (StunPacket) ctx.getMessage();
            switch (stunPacket.getMessageClass()) {
                case REQUEST:
                case INDICATION:
                    processPacket(stunPacket, (SocketAddress) ctx.getAddress(), ctx);
                    return ctx.getStopAction();
            }
        }
        return super.handleRead(ctx);
    }

    /**
     * Stun/ICE packet processing
     *
     * @param packet The packet in question
     * @param sourceAddress The source address, since one ICE channel may have 
     * several DemultiplexerSockets
     * @param ctx The filterChainContext associated with this request
     * @return Whether the packet was processed by STUN/ICE.
     */
    public boolean processPacket(StunPacket packet, SocketAddress sourceAddress, FilterChainContext ctx) throws IOException {
        Map<AttributeType, net.mc_cubed.icedjava.packet.attribute.Attribute> attrMap =
                new HashMap<AttributeType, net.mc_cubed.icedjava.packet.attribute.Attribute>();
        // Extract the essential attributes we need
        for (net.mc_cubed.icedjava.packet.attribute.Attribute attr :
                packet.getAttributes()) {
            switch (attr.getType()) {
                case MAPPED_ADDRESS:
                case USERNAME:
                case REALM:
                case PRIORITY:
                case USE_CANDIDATE:
                case ICE_CONTROLLED:
                case ICE_CONTROLLING:
                case FINGERPRINT:
                case MESSAGE_INTEGRITY:
                    attrMap.put(attr.getType(), attr);
                    break;
            }
        }
        // Isolate which peer by the Username parameter
        UsernameAttribute userAttribute =
                (UsernameAttribute) attrMap.get(AttributeType.USERNAME);
        RealmAttribute realmAttribute =
                (RealmAttribute) attrMap.get(AttributeType.REALM);
        if (userAttribute == null || realmAttribute == null) {
            // Ordinary STUN request, not expecting this
            return false;
        }

        log.log(Level.FINEST, "Received ICE packet {0}", packet);

        StringTokenizer st = new StringTokenizer(userAttribute.getValue(), ":");
        if (st.countTokens() != 2) {
            log.log(Level.WARNING, "Username attribute invalid: {0}", userAttribute.getValue());
            return false;
        }

        String localUfrag = st.nextToken();

        if (localUfrag.compareTo(this.localUFrag) != 0) {
            log.log(Level.WARNING, "From a nonexistent peer: {0}", st.nextToken());
            return false;
        }

        // Check message integrity and fingerprint validity
        IntegrityAttribute integrityAttribute =
                (IntegrityAttribute) attrMap.get(AttributeType.MESSAGE_INTEGRITY);
        if (packet.getMessageClass() == MessageClass.REQUEST && !integrityAttribute.verifyHash(userAttribute.getValue(), realmAttribute.getValue(), localPassword)) {
            log.info("Invalid integrity attribute");
            return false;
        }

        FingerprintAttribute fingerprintAttribute =
                (FingerprintAttribute) attrMap.get(AttributeType.FINGERPRINT);
        if (!fingerprintAttribute.isValid()) {
            log.info("Invalid fingerprint attribute");
            return false;
        }

        // Check that ICE_CONTROLLED/ICE_CONTROLLING is set properly
        //  and resolve any conflicts
        Boolean localControl = null;
        long remoteTieBreaker = 0;
        if (attrMap.containsKey(AttributeType.ICE_CONTROLLING)) {
            localControl = false;
            remoteTieBreaker = ((IceControllingAttribute) attrMap.get(AttributeType.ICE_CONTROLLING)).getNumber();
        } else if (attrMap.containsKey(AttributeType.ICE_CONTROLLED)) {
            localControl = true;
            remoteTieBreaker = ((IceControlledAttribute) attrMap.get(AttributeType.ICE_CONTROLLED)).getNumber();
        }

        if (localControl == null) {
            log.info("Local control state is unknown!");
            return false;
        }

        if (isLocalControlled() ^ localControl
                && packet.getMessageClass() == MessageClass.REQUEST
                && remoteTieBreaker != 0) {
            log.warning("AgentType Mismatch");

            // Determine who switches roles
            if ((isLocalControlled()
                    && getTieBreaker() >= remoteTieBreaker)
                    || (!isLocalControlled()
                    && getTieBreaker() < remoteTieBreaker)) {

                StunPacket response = StunUtil.createReplyPacket(packet, MessageClass.ERROR);
                response.getAttributes().add(AttributeFactory.createErrorCodeAttribute(ROLE_CONFLICT, ROLE_CONFLICT_REASON));
                if (isLocalControlled()) {
                    response.getAttributes().add(AttributeFactory.createIceControllingAttribute(getTieBreaker()));
                } else {
                    response.getAttributes().add(AttributeFactory.createIceControlledAttribute(getTieBreaker()));
                }
                // Authentication Attributes
                if (getRemotePassword() != null && getRemoteUFrag() != null) {
                    String realm = "icedjava";
                    String username = getRemoteUFrag() + ":" + getLocalUFrag();
                    response.getAttributes().add(AttributeFactory.createUsernameAttribute(username));
                    response.getAttributes().add(AttributeFactory.createRealmAttribute(realm));
                    response.getAttributes().add(AttributeFactory.createIntegrityAttribute(username, realm, getRemotePassword()));
                }
                response.getAttributes().add(AttributeFactory.createFingerprintAttribute());

                ctx.write(sourceAddress, response, null);

                log.warning("Peer will switch roles");
                return true;
            } else {

                // Flip the local role and continue (no error)
                log.warning("Local role switching");
                setLocalControlled(!isLocalControlled());
            }

        }

        // Check whether this is a nomination request, and we're the controlled peer
        if (attrMap.containsKey(AttributeType.USE_CANDIDATE) && !isLocalControlled()) {
            // Nominate this candidate with the peer
            setUseCandidate(
                    (InetSocketAddress) ctx.getConnection().getLocalAddress(),
                    (InetSocketAddress) sourceAddress);
        }
        remoteTouch(
                (InetSocketAddress) ctx.getConnection().getLocalAddress(),
                (InetSocketAddress) sourceAddress);
        // We should reply
        switch (packet.getMessageClass()) {
            case REQUEST:
                StunPacket response = StunUtil.createReplyPacket(packet, MessageClass.SUCCESS);
                response.getAttributes().add(AttributeFactory.createXORMappedAddressAttribute(
                        ((InetSocketAddress) sourceAddress).getAddress(),
                        ((InetSocketAddress) sourceAddress).getPort(),
                        packet.getTransactionId()));

                if (isLocalControlled()) {
                    response.getAttributes().add(AttributeFactory.createIceControllingAttribute(getTieBreaker()));
                } else {
                    response.getAttributes().add(AttributeFactory.createIceControlledAttribute(getTieBreaker()));
                }
                // Authentication Attributes
                if (getRemotePassword() != null && getRemoteUFrag() != null) {
                    String realm = "icedjava";
                    String username = getRemoteUFrag() + ":" + getLocalUFrag();
                    response.getAttributes().add(AttributeFactory.createUsernameAttribute(username));
                    response.getAttributes().add(AttributeFactory.createRealmAttribute(realm));
                    response.getAttributes().add(AttributeFactory.createIntegrityAttribute(username, realm, getRemotePassword()));
                }
                response.getAttributes().add(AttributeFactory.createFingerprintAttribute());

                ctx.write(sourceAddress, response, null);

                break;
            default:
                log.log(Level.WARNING, "Got a strange packet: {0}", packet);
        }
        return true;
    }

    List<LocalCandidate> getLocalCandidates(IceSocket iceSocket) {
        return getLocalCandidates(iceSocket, false);
    }

    /**
     * Get a list of local candidates for a specified iceSocket.  Will perform
     * collectCandidates if they haven't already been collected, or if refresh
     * is set to true
     *
     * @param iceSocket channel to return the candidates of
     * @param refresh refresh candidates if true, normal behavior if false
     * @return A list of LocalCandidates for the specified channel
     */
    List<LocalCandidate> getLocalCandidates(IceSocket iceSocket, boolean refresh) {
        if (!socketCandidateMap.containsKey(iceSocket) || refresh) {
            socketCandidateMap.put(iceSocket, collectCandidates(iceSocket));
        }
        return socketCandidateMap.get(iceSocket);
    }

    /**
     * Collect a list of candidates for a specified channel.
     *
     * @param iceSocket the channel to collect candidates for
     * @return a list of LocalCandidates
     */
    private List<LocalCandidate> collectCandidates(IceSocket iceSocket) {
        log.log(Level.FINE, "Collecting Candidates for peer: {0} on socket {1}", new Object[]{localUFrag, iceSocket});
        List<LocalCandidate> retval = new LinkedList<LocalCandidate>();


        /**
         * If we're in a WELD environment, take advantage of it by using the 
         * extensibility mechanisms
         */
        if (localCandidateDiscoveryMechs != null && !localCandidateDiscoveryMechs.isUnsatisfied()) {
            /**
             * Each individual mechanism potentially has something to offer, so
             * collect all possibilities
             */
            for (CandidateDiscovery discoveryMechanism : localCandidateDiscoveryMechs) {
                try {
                    retval.addAll(discoveryMechanism.discoverCandidates(this, iceSocket));
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Caught an exception during candidate discovery", ex);
                }
            }
        } else {
            /**
             * If we're not in a WELD environment, that's OK too, but we will
             * only find IPv4 and IPv6 candidates.
             */
            for (Class discoveryClass : new Class[]{UDPCandidateDiscovery.class, TCPCandidateDiscovery.class}) {
                try {
                    retval.addAll(((CandidateDiscovery) discoveryClass.newInstance()).discoverCandidates(this, iceSocket));
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Caught an exception during candidate discovery", ex);
                }
            }
        }

        /**
         * Skip additional candidate processing if we're in local only mode.
         */
        if (!localOnly) {
            // If we're in a WELD environment, take advantage of it
            if (discoveryMechanisms != null && !discoveryMechanisms.isUnsatisfied()) {
                // For each address discovery mechanism...
                for (AddressDiscovery discoveryMechanism : discoveryMechanisms) {
                    try {
                        // Add additional candidates to the list
                        retval.addAll(discoveryMechanism.getCandidates(retval));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Exception during address discovery.", ex);
                    }
                }
            } else {
                /**
                 * If we're not in a weld environment, that's OK too, but the
                 * extensibile address discovery mechanisms won't be used.
                 */
                try {
                    // Collect Server Reflexive candidates
                    retval.addAll(new StunAddressDiscovery().getCandidates(retval));
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Caught an Exception during STUN procedures.", ex);
                }

                try {
                    // Collect UPNP candidates
                    retval.addAll(new IceUPNPBridge().getCandidates(retval));
                } catch (Exception ex) {
                    log.log(Level.FINE, "Caught an Exception during UPNP procedures.", ex);
                }

                try {
                    // Collect PMP candidates
                    retval.addAll(new IcePMPBridge().getCandidates(retval));
                } catch (Exception ex) {
                    log.log(Level.FINE, "Caught an Exception during PMP procedures.", ex);
                }


                // TODO: Collect Server Relayed Candidates if supported by server


            }
        }
        /**
         * Prioritize, then remove duplicate candidates of lower priority.
         */
        return removeDuplicates(prioritize(retval));
    }

    private void remoteTouch(final InetSocketAddress localSocket, final InetSocketAddress remoteSocket) {
        // Check for an existing check pair
        List<CandidatePair> pairs = new LinkedList<CandidatePair>();
        LocalCandidate local = null;
        for (List<CandidatePair> localCheckPairs : checkPairs.values()) {
            for (CandidatePair checkPair : localCheckPairs) {
                if (checkPair.getLocalCandidate().getSocketAddress().equals(localSocket)) {
                    local = checkPair.getLocalCandidate();
                    if (checkPair.getRemoteCandidate().getSocketAddress().equals(remoteSocket)) {
                        pairs.add(checkPair);
                        break;
                    }
                }
            }
        }

        if (local == null) {
            // TODO: Unknown use candidate!
            log.log(Level.WARNING, "Unknown stun touch on socket: " + "{0} <-> {1}", new Object[]{localSocket, remoteSocket});
        } else {
            if (!pairs.isEmpty()) {
                for (CandidatePair pair : pairs) {
                    if (pair.getState() == PairState.FROZEN
                            || pair.getState() == PairState.FAILED) {
                        pair.setState(PairState.WAITING);
                        triggeredCheckQueue.offer(pair);
                    }
                }
            } else {
                // Form the remote candidate using the local for reference
                RemoteCandidate remote = new RemoteCandidate(CandidateType.PEER_REFLEXIVE, remoteSocket.getAddress(), remoteSocket.getPort(), local.getComponentId(), local.getTransport(), local.getFoundation());
                CandidatePair pair = new CandidatePair(local, remote, isLocalControlled());
                pair.setState(PairState.SUCCEEDED);
                if (checkPairs.get(local.getIceSocket()) != null) {
                    checkPairs.get(local.getIceSocket()).add(pair);
                }

            }
        }
        //lastTouch = new Date().getTime();
    }

    /**
     * Check the current status of Ice Processing and adjust it as appropriate
     */
    private void checkStatus() {

        /**
         * Keep checking for a success condition if we're either in-progress,
         * or have failed
         */
        if (iceStatus == IceStatus.IN_PROGRESS || iceStatus == IceStatus.FAILED) {
            // Fast abort: If we have no checkpairs, we cannot yet succeed or fail.
            if (checkPairs != null && !checkPairs.isEmpty()) {
                // Being eternally optimistic, we assume success first
                boolean didSucceed = true;
                for (IceSocket socket : checkPairs.keySet()) {
                    if (nominated.containsKey(socket)
                            && nominated.get(socket) != null
                            && !nominated.get(socket).contains(null)) {
                        // Replace the contents of the Using sockets with the
                        // nominated pairs.  This usage method supports hot
                        // re-negociation.
                        using.putAll(nominated);
                        // TODO: Shut down unused sockets, preserving only the used
                        // socket pairs
                    } else {
                        didSucceed = false;
                    }
                }

                if (didSucceed) {
                    // Update ICE status
                    setIceStatus(IceStatus.SUCCESS);

                }
            }
        }
    }

    @Override
    final public void updateMedia(String sdpText) throws SdpParseException {
        SessionDescription session = sdpFactory.createSessionDescription(sdpText);
        Origin origin = session.getOrigin();
        Connection connection = session.getConnection();
        List<Attribute> iceAttributes = new LinkedList<Attribute>(session.getAttributes(true));
        List<MediaDescription> medias = new LinkedList<MediaDescription>(session.getAttributes(true));
        updateMedia(origin, connection, iceAttributes, medias);

    }

    /**
     * Takes vectors and delegates to the List version of the updateMedia method
     * 
     * @param attributes SDP global Attributes including ICE information
     * @param mediaDescriptions media descriptions
     */
    @Override
    final public void updateMedia(Origin origin, Connection conn, Vector attributes, Vector mediaDescriptions)
            throws SdpParseException {
        updateMedia(origin, conn, new LinkedList<Attribute>(attributes), new LinkedList<MediaDescription>(mediaDescriptions));
    }

    /**
     * Takes a remote update of ICE data to communicate new ICE state to the local
     * peer
     *
     * @param iceAttributes
     * @param iceMedias
     * @throws SdpException
     */
    @Override
    public void updateMedia(final Origin origin, final Connection conn, final List<Attribute> iceAttributes, final List<MediaDescription> iceMedias)
            throws SdpParseException {
        log.log(Level.FINE, "SDP Update to {0}\n{1}\n{2}", new Object[]{localUFrag, iceAttributes, iceMedias});
        /**
         * Special case - Received a Session Description before processing
         * started. In this event, we are definitely the controlled peer, and
         * should behave as such
         */
        if (this.getStatus() == IceStatus.NOT_STARTED) {
            log.log(Level.FINE, "Received a session description before ICE "
                    + "processing start. Switching to CONTROLLED role");
            localRole = AgentRole.CONTROLLED;
        }

        getThreadpool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    _updateMedia(origin, conn, iceAttributes, iceMedias);
                } catch (SdpException ex) {
                    Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Throwable ex) {
                    Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    synchronized void _updateMedia(Origin origin, Connection conn, List<Attribute> iceAttributes, List<MediaDescription> iceMedias)
            throws SdpParseException, SdpException, UnknownHostException {

        /**
         * Fast abort: If we got a duplicate, or old session, end here.
         */
        if (lastRemoteVersion >= origin.getSessionVersion()) {
            return;
        }

        lastRemoteVersion = origin.getSessionVersion();

        boolean uFragChanged = false, pwdChanged = false;
        String newRemoteUFrag = null, newRemotePassword = null;
        /**
         * 9.1.1.1.  ICE Restarts
         *
         * These rules imply that setting the IP address in the c line to
         * 0.0.0.0 will cause an ICE restart.  Consequently, ICE implementations
         * MUST NOT utilize this mechanism for call hold, and instead MUST use
         * a=inactive and a=sendonly as described in [RFC3264].
         */
        if (Connection.IP4.equalsIgnoreCase(conn.getAddressType())
                && conn.getAddress().equalsIgnoreCase("0.0.0.0")) {
            restartFlag = true;
        }

        /**
         * Process the ICE attributes and set state appropriately.
         */
        for (Attribute attr : iceAttributes) {
            if (attr.getName().equalsIgnoreCase(SDP_ICE_LITE)) {
                // TODO: Remote is LITE implementation
            } else if (attr.getName().equalsIgnoreCase(SDP_UFRAG)) {
                if (remoteUFrag == null
                        || !remoteUFrag.equalsIgnoreCase(attr.getValue())) {
                    // It's not a uFrag change if we're setting it the first time
                    if (remoteUFrag != null) {
                        uFragChanged = true;
                    }
                    newRemoteUFrag = attr.getValue();
                }
            } else if (attr.getName().equalsIgnoreCase(SDP_PWD)) {
                if (remotePassword == null || !remotePassword.equalsIgnoreCase(attr.getValue())) {
                    // It's not a password change if we're setting it the first time
                    if (remotePassword != null) {
                        pwdChanged = true;
                    }
                    newRemotePassword = attr.getValue();
                }
            }
        }

        /**
         * 9.1.1.1.  ICE Restarts
         *
         * To restart ICE, an agent MUST change both the ice-pwd and the ice-
         * ufrag for the media stream in an offer.  Note that it is permissible
         * to use a session-level attribute in one offer, but to provide the
         * same ice-pwd or ice-ufrag as a media-level attribute in a subsequent
         * offer.  This is not a change in password, just a change in its
         * representation, and does not cause an ICE restart.
         */
        if (uFragChanged && pwdChanged) {
            restartFlag = true;
        }


        Map<MediaDescription, List<RemoteCandidate>> remoteMediaMap = extractRemoteCandidates(conn, iceMedias);

        if (restartFlag) {
            doReset(isLocalControlled(), false);
        }

        // These need to survive the restart
        if (newRemoteUFrag != null) {
            setRemoteUFrag(newRemoteUFrag);
        }

        if (newRemotePassword != null) {
            setRemotePassword(newRemotePassword);
        }

        for (IceSocket socket : iceSockets) {
            getLocalCandidates(socket, false);
        }

        if (iceStatus != IceStatus.SUCCESS) {
            if (checkPairs.isEmpty()) {
                checkPairs.putAll(matchCandidates(socketCandidateMap, remoteMediaMap));
            } else {
                checkPairs.putAll(matchAndUpdate(matchCandidates(socketCandidateMap, remoteMediaMap), checkPairs));
            }
        }

    }

    public int getIceInterval() {
        return iceInterval;
    }

    public void setIceInterval(int iceInterval) {
        this.iceInterval = iceInterval;

    }

    public boolean isIcelite() {
        return icelite;
    }

    /**
     * Set whether the local ICE implementation should use the ICE-LITE
     * algorithm instead of the full ICE processing algorithm
     *
     * @param icelite use ice-lite if true, full ICE if false.
     */
    public void setIcelite(boolean icelite) {
        this.icelite = icelite;
    }

    /**
     * Get the refresh timer used when keeping established ICE sockets alive,
     * measured in MS
     *
     * @return
     */
    public long getRefreshDelay() {
        return refreshDelay;
    }

    /**
     * Set the refresh timer used when keeping established ICE sockets alive,
     * measured in MS
     *
     * @param refreshDelay refresh STUN packets are sent this frequently, measured
     * in MS
     */
    public void setRefreshDelay(long refreshDelay) {
        this.refreshDelay = refreshDelay;
    }

    /**
     * Get the remote password used during ICE processing
     *
     * @return
     */
    @Override
    public String getRemotePassword() {
        return remotePassword;
    }

    /**
     * Set the remote password to use in ICE processing
     *
     * @param remotePassword
     */
    protected void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    /**
     * Get the remote User Frag attribute used in ICE processing
     *
     * @return
     */
    @Override
    public String getRemoteUFrag() {
        return remoteUFrag;
    }

    /**
     * Manually set the remote User Frag used in ICE processing
     *
     * @param remoteUFrag
     */
    protected void setRemoteUFrag(String remoteUFrag) {
        this.remoteUFrag = remoteUFrag;
    }

    /**
     * Return whether the local State Machine is the controlling ICE peer
     *
     * @return the state of the Local Controlled flag
     */
    @Override
    public boolean isLocalControlled() {
        return localRole == AgentRole.CONTROLLING;
    }

    /**
     * Get a list of the IceSockets associated with this State Machine
     *
     * @return a list of associated ICE sockets
     */
    public List<IceSocket> getIceSockets() {
        return iceSockets;
    }

    /**
     * Set the list of IceSockets associated with this Ice State Machine
     * @param sockets
     */
    public synchronized void setIceSockets(IceSocket[] sockets) {
        for (IceSocket socket : iceSockets) {
            ((IceDatagramSocket) socket).removePeer(this);
        }
        iceSockets.clear();
        iceSockets.addAll(Arrays.asList(sockets));
        for (IceSocket socket : iceSockets) {
            ((IceDatagramSocket) socket).addPeer(this);
        }
    }

    /**
     * Change the localControlled flag of ICE processing
     *
     * @param localControl the new localControl flag
     */
    @Override
    public synchronized void setLocalControlled(boolean localControl) {
        // Need to change the local role, this requires a bit of work
        if (isLocalControlled() != localControl) {

            // Reset the priorities of pairs and re-sort them
            if (checkPairs != null) {
                for (List<CandidatePair> pairs : checkPairs.values()) {
                    for (CandidatePair pair : pairs) {
                        // Reset their local control flag
                        pair.setLocalControlled(localControl);
                        pair.setState(PairState.FROZEN);
                    }
                    // Re-sort candidates
                    Collections.sort(pairs, new CandidatePairComparison());
                }
            }

            localRole = (localControl ? AgentRole.CONTROLLING : AgentRole.CONTROLLED);

            // If we're not the controlling agent, we'll always use regular nomination.
            if (!localControl) {
                nomination = NominationType.REGULAR;
            }

            // Reset the overall ICE Status
            iceStatus = IceStatus.IN_PROGRESS;

            if (!localControl) {
                // Need to emit an SDP reply, as we thought we were the
                // controller before, and so never replied
                sendSessionUpdate();
            }
        }
    }

    /**
     * Return the status of ICE processing
     * 
     * @return the current status of ICE processing
     */
    @Override
    public IceStatus getStatus() {
        checkStatus();

        return iceStatus;
    }

    /**
     * Generate a random number hex code string of a specified length
     *
     * @param chars number of characters of random hex to generate
     * @return a String containing chars characters of random hex code
     */
    public static String generateHashString(int chars) {
        String hashString = Long.toHexString(random.nextLong());
        while (hashString.length() < chars) {
            hashString = hashString.concat(Long.toHexString(random.nextLong()));
        }
        return hashString.substring(0, chars);
    }

    /**
     * Count the number of pairs in a specified state
     *
     * @param pairs Source pairs
     * @param pairState State to search for
     * @return number of pairs in the specified state
     */
    private int pairsInState(List<CandidatePair> pairs, PairState pairState) {
        int count = 0;
        for (CandidatePair pair : pairs) {
            if (pair.getState() == pairState) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get a list of pairs in a specified state
     *
     * @param pairs Source pairs
     * @param pairState State to search for
     * @return a list of pairs in the specified state
     */
    private List<CandidatePair> getPairsInState(List<CandidatePair> pairs, PairState pairState) {
        List<CandidatePair> retval = new LinkedList<CandidatePair>();
        for (CandidatePair pair : pairs) {
            if (pair.getState() == pairState) {
                retval.add(pair);
            }
        }
        return retval;
    }

    private CandidatePair getFirstWaitingPair(List<CandidatePair> pairs) {
        for (CandidatePair pair : pairs) {
            if (pair.getState() == PairState.WAITING) {
                return pair;
            }
        }
        return null;
    }

    /**
     * Separate the given candidate pairs into their respective componentIds
     * @param candidatePairs Mixed Candidate Pairs
     * @return Separated by ComponentId
     */
    private Map<Short, List<CandidatePair>> separateByComponent(List<CandidatePair> candidatePairs) {
        Map<Short, List<CandidatePair>> result = new LinkedHashMap<Short, List<CandidatePair>>();
        for (CandidatePair pair : candidatePairs) {
            if (!result.containsKey(pair.getComponentId())) {
                result.put(pair.getComponentId(), new LinkedList<CandidatePair>());
            }
            result.get(pair.getComponentId()).add(pair);
        }
        return result;
    }

    /**
     * Do an ICE type STUN test on a specific candidate pair
     * @param pair the pair to send the test on
     * @param localUFrag local username fragment
     * @param remoteUFrag remote username fragment
     * @param remotePassword remote peer's password
     * @param controlling whether we're controlling the ICE session
     * @param peerReflexPriority if a reflexive candidate is created, assign
     *      this priority to it
     * @param tieBreaker if there's a disagreement about who's controlling,
     *      this number is used to decide who takes over
     * @param nominate Send a USE-CANDIDATE flag
     * @return the ICE Reply, whether successful or not
     */
    public Future<IceReply> doIceTest(CandidatePair pair, String localUFrag, String remoteUFrag,
            String remotePassword, boolean controlling, int peerReflexPriority,
            long tieBreaker, boolean nominate) {
        try {
            StunPacket stunPacket = StunUtil.createStunRequest(MessageClass.REQUEST, MessageMethod.BINDING);
            // ICE Specific Attributes
            stunPacket.getAttributes().add(AttributeFactory.createPriorityAttribute(peerReflexPriority));
            if (controlling) {
                stunPacket.getAttributes().add(AttributeFactory.createIceControllingAttribute(tieBreaker));
            } else {
                stunPacket.getAttributes().add(AttributeFactory.createIceControlledAttribute(tieBreaker));
            }
            if (nominate) {
                stunPacket.getAttributes().add(new NullAttribute(AttributeType.USE_CANDIDATE));
            }
            // Authentication Attributes
            String realm = "icedjava";
            String username = remoteUFrag + ":" + localUFrag;
            stunPacket.getAttributes().add(AttributeFactory.createUsernameAttribute(username));
            stunPacket.getAttributes().add(AttributeFactory.createRealmAttribute(realm));
            stunPacket.getAttributes().add(AttributeFactory.createIntegrityAttribute(username, realm, remotePassword));
            stunPacket.getAttributes().add(AttributeFactory.createFingerprintAttribute());
            // Stun it
            log.log(Level.FINER, "Ice Test {0} -> {1}", new Object[]{pair.getLocalCandidate().getSocketAddress(), pair.getRemoteCandidate().getSocketAddress()});

            Future<StunReply> replyFuture = pair.localCandidate.socket.doTest(pair.getRemoteCandidate().getSocketAddress(), stunPacket);

            return new IceReplyFuture(replyFuture);
        } catch (IOException ex) {
            return new IceReplyFuture(ex);
        } catch (InterruptedException ex) {
            return new IceReplyFuture(ex);
        }

    }

    private void setIceStatus(IceStatus iceStatus) {
        this.iceStatus = iceStatus;
        sendSessionUpdate();
    }

    class IceReplyFuture implements Future<IceReply> {

        final Future<StunReply> stunReplyFuture;
        final Throwable cause;

        IceReplyFuture(Future<StunReply> stunReplyFuture) {
            this.stunReplyFuture = stunReplyFuture;
            this.cause = null;
        }

        IceReplyFuture(Throwable ex) {
            this.cause = ex;
            this.stunReplyFuture = null;
        }

        @Override
        public boolean cancel(boolean bln) {
            if (stunReplyFuture != null) {
                return stunReplyFuture.cancel(bln);
            } else {
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            if (cause != null) {
                return true;
            } else {
                return stunReplyFuture.isCancelled();
            }
        }

        @Override
        public boolean isDone() {
            if (cause != null) {
                return true;
            } else {
                return stunReplyFuture.isDone();
            }
        }

        @Override
        public IceReply get() throws InterruptedException, ExecutionException {
            if (cause != null) {
                return new IceReplyImpl(cause);
            } else {
                StunReply reply = stunReplyFuture.get();
                return reply != null ? new IceReplyImpl(reply) : null;
            }
        }

        @Override
        public IceReply get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
            if (cause != null) {
                return new IceReplyImpl(cause);
            } else {
                StunReply reply = stunReplyFuture.get(l, tu);
                return reply != null ? new IceReplyImpl(reply) : null;
            }
        }
    }

    /**
     * Extract a list of RemoteCandidates from a specified Default Connection line
     * and a list of MediaDescriptions extracted from an SDP offer.
     *
     * @param remoteConnection Default Connection (c-line) of the SDP offer
     * @param remoteOffer A list of MediaDescriptions (m-lines) from the SDP offer
     * @return A list of Candidates extracted from the map key MediaDescriptions
     * @throws SdpException
     * @throws SdpParseException
     * @throws UnknownHostException
     */
    private Map<MediaDescription, List<RemoteCandidate>> extractRemoteCandidates(
            Connection remoteConnection,
            List<MediaDescription> remoteOffer)
            throws SdpException, SdpParseException, UnknownHostException {
        Map<MediaDescription, List<RemoteCandidate>> retval = new LinkedHashMap<MediaDescription, List<RemoteCandidate>>();
        for (MediaDescription media : remoteOffer) {
            List<RemoteCandidate> candidateList = new LinkedList<RemoteCandidate>();
            int candidateCount = 0;
            for (Attribute attribute : (Vector<Attribute>) media.getAttributes(true)) {
                if (attribute.getName().compareTo(CANDIDATE_NAME) == 0) {
                    candidateList.add(new RemoteCandidate(attribute));
                    candidateCount++;
                }
            }
            if (candidateCount == 0) {
                // Use the media and C line to form a candidate
                TransportType tt;
                if (media.getMedia().getProtocol().startsWith("TCP")) {
                    tt = TransportType.TCP;
                } else {
                    tt = TransportType.UDP;
                }

                // Use the C line in the media description if available
                String remoteAddr = (media.getConnection() != null
                        ? media.getConnection().getAddress()
                        : remoteConnection.getAddress());

                candidateList.add(new RemoteCandidate(
                        CandidateType.LOCAL,
                        InetAddress.getByName(remoteAddr),
                        media.getMedia().getMediaPort(),
                        (short) 0,
                        tt,
                        "1"));
            }
            retval.put(media, candidateList);
        }

        return retval;

    }

    /**
     * Match a list of LocalCandidates to a list of RemoteCandidates
     *
     * @param localMediaMap Local Candidates to match against
     * @param remoteMediaMap Remote Candidates to match against
     * @return a list of valid CandidatePairs formed from the two input maps
     * @throws SdpParseException
     * @throws SdpException
     */
    private Map<IceSocket, List<CandidatePair>> matchCandidates(
            Map<IceSocket, List<LocalCandidate>> localMediaMap,
            Map<MediaDescription, List<RemoteCandidate>> remoteMediaMap)
            throws SdpParseException, SdpException {
        Map<IceSocket, List<CandidatePair>> matchCandidates = new HashMap<IceSocket, List<CandidatePair>>();
        for (Entry<IceSocket, List<LocalCandidate>> localEntry :
                localMediaMap.entrySet()) {
            IceSocket localSocket = localEntry.getKey();

            for (Entry<MediaDescription, List<RemoteCandidate>> remoteEntry :
                    remoteMediaMap.entrySet()) {
                MediaDescription remoteMedia = remoteEntry.getKey();

                // Determine a match
                if (localSocket.getMedia().getMediaType().compareTo(
                        remoteMedia.getMedia().getMediaType()) == 0
                        && localSocket.getMedia().getProtocol().compareTo(
                        remoteMedia.getMedia().getProtocol()) == 0) {
                    // Make the pairs
                    matchCandidates.put(localSocket, CandidatePair.getPairs(
                            localEntry.getValue(),
                            remoteEntry.getValue(),
                            this.isLocalControlled()));
                    // Go on to the next Local Media
                    break;
                }
            }

        }


        return matchCandidates;
    }

    /**
     * Create a list of Candidate Pairs from a MediaDescription containing ICE
     * attributes, and LocalCandidates from this State Machine
     *
     * @param md MediaDescrition containing ICE attributes to match against
     * @param localCandidates LocalCandidates to match against
     * @return A list of valid matched Candidate Pairs to use for ICE processing
     */
    List<CandidatePair> createPairs(MediaDescription md,
            List<LocalCandidate> localCandidates) {
        List<CandidatePair> pairs;
        boolean localControlling = localRole == AgentRole.CONTROLLING;
        List<RemoteCandidate> remoteCandidates =
                new LinkedList<RemoteCandidate>();
        // Extract the remote candidates from the attributes
        for (Attribute attr : (Vector<Attribute>) md.getAttributes(false)) {
            try {
                if (attr.getName().compareTo(CANDIDATE_NAME) == 0) {
                    RemoteCandidate candidate = new RemoteCandidate(attr);
                    if (candidate != null) {
                        remoteCandidates.add(candidate);
                    }
                }
            } catch (SdpParseException ex) {
                log.log(Level.SEVERE,
                        null, ex);
            } catch (UnknownHostException ex) {
                log.log(Level.SEVERE,
                        null, ex);
            }
        }

        // Form pairs from the local and remote candidates
        pairs = CandidatePair.getPairs(localCandidates, remoteCandidates,
                localControlling);

        // Sort them by priority
        Collections.sort(pairs, new CandidatePairComparison());

        return pairs;
    }

    /**
     * A utility function that gets the list of MediaDescriptions associated with
     * this ICE state machine.
     *
     * @return A list of MediaDescriptions associated with this state machine
     * @throws SdpException
     */
    @Override
    public List<MediaDescription> getMediaDescriptions() throws SdpException {
        return getMediaDescriptions(false);
    }

    /**
     * A utility function that converts a vector of strings to an array of strings
     *
     * @param stringVector a vector of strings
     * @return an array of strings
     */
    String[] stringVectorToArray(Vector stringVector) {
        String[] retval = new String[stringVector.size()];
        for (int pos = 0; pos < stringVector.size(); pos++) {
            retval[pos] = (String) stringVector.get(pos);
        }
        return retval;
    }

    /**
     * Get the MediaDescriptions associated with this ICE State Machine
     *
     * @param refresh force a refresh of the LocalCandidates before creating the
     * MediaDescription lines
     * @return a list of MediaDescriptions describing the media offered by this
     * State Machine
     * @throws SdpException
     */
    public synchronized List<MediaDescription> getMediaDescriptions(boolean refresh) throws SdpException {
        List<MediaDescription> retval = new LinkedList<MediaDescription>();
        for (IceSocket socket : iceSockets) {
            if (mediaCandidates.get(socket) == null) {
                mediaCandidates.put(socket, socket.getMedia());
            }

            getLocalCandidates(socket, refresh);

            // Convert the Media line to a MediaDescription
            Media rawMedia = socket.getMedia();
            MediaDescription media = sdpFactory.createMediaDescription(rawMedia.getMediaType(), rawMedia.getMediaPort(), rawMedia.getPortCount(), rawMedia.getProtocol(), stringVectorToArray(rawMedia.getMediaFormats(false)));
            // Remove all the candidates
            List<Attribute> checkAttrs = new LinkedList<Attribute>();

            checkAttrs.addAll(media.getAttributes(true));

            for (Attribute attr : checkAttrs) {
                if (attr.getName().equals(CANDIDATE_NAME)) {
                    media.getAttributes(true).remove(attr);
                }
            }

            List<InetSocketAddress> useAddrs = new LinkedList<InetSocketAddress>();

            // Select the candidates to use
            if (nominated.get(socket) != null
                    && nominated.get(socket).size() == socket.getMedia().getPortCount()
                    && !nominated.get(socket).contains(null)) {
                for (CandidatePair pair : nominated.get(socket)) {
                    if (pair == null || pair.getLocalCandidate() == null) {
                        log.log(Level.SEVERE, "Got a null local candidate, THIS IS A BUG: {0}", pair);
                    } else {
                        useAddrs.add(pair.getLocalCandidate().getSocketAddress());
                    }
                }
            } else {
                for (LocalCandidate candidate : getHighestPriorityLCs(socketCandidateMap.get(socket), socket.getComponents())) {
                    useAddrs.add(candidate.getSocketAddress());
                }

                // Add candidates to the Media Description
                for (LocalCandidate candidate : socketCandidateMap.get(socket)) {
                    media.getAttributes(true).add(sdpFactory.createAttribute(
                            "candidate",
                            candidate.toAttributeFormat()));
                }
            }

            // Set the port according to the first nominated pair
            media.getMedia().setMediaPort(useAddrs.get(0).getPort());

            // Get the address we're submitting in the C line
            InetAddress defaultAddress = getDefaultConnectionBasis();

            // Add a c line to the media if nessisary
            InetAddress address = useAddrs.get(0).getAddress();
            if (address != null && !address.equals(defaultAddress)) {
                String addrClass = "IP4";
                if (address instanceof Inet4Address) {
                    addrClass = Connection.IP4;
                }
                if (address instanceof Inet6Address) {
                    addrClass = Connection.IP6;
                }
                media.setConnection(sdpFactory.createConnection(Connection.IN, addrClass, address.getHostAddress()));
            }

            /**
             * If we're creating a dual port RTP connection, make sure to
             * add an RTCP attribute and set the port count to 1
             */
            if (socket.getMedia().getProtocol().startsWith("RTP")
                    && socket.getMedia().getPortCount() == 2
                    && useAddrs.size() > 1
                    && useAddrs.get(1) != null) {
                media.getMedia().setPortCount(1);
                media.setAttribute("rtcp", Integer.toString(useAddrs.get(1).getPort()));
            }

            retval.add(media);
        }

        return retval;


    }

    /**
     * Create a SessionDescription that can be used to notify peers of the media
     * offered by this ICE State Machine.
     *
     * @return A session description describing the media offered by this ICE
     * State Machine
     * @throws SdpException
     * @deprecated use getDefaultConnect(), getGlobalAttributes() and
     * getMediaDescriptions() instead
     */
    @Deprecated
    @Override
    public SessionDescription createOffer() throws SdpException {
        SdpFactory factory = SdpFactory.getInstance();

        SessionDescription session = factory.createSessionDescription();

        session.setConnection(getDefaultConnection());
        // Add authorization attributes
        session.getAttributes(true).addAll(getGlobalAttributes());
        // Set the MediaDescriptions
        session.getMediaDescriptions(true).addAll(getMediaDescriptions());

        return session;


    }

    /**
     * Prioritize and sort a specified list of LocalCandidates
     *
     * @param candidates Candidate list to prioritize and sort
     * @return Prioritized and sorted list of candidates
     */
    private List<LocalCandidate> prioritize(List<LocalCandidate> candidates) {
        // Compute the priority of each candidate
        // Get the interface priorities
        // TODO: allow this to be customized

        // Compute a priority for each
        for (LocalCandidate candidate : candidates) {
            int localPriority = 0;
            // Find a matching interface


            for (InterfaceProfile intProfile : interfaceData) {
                switch (candidate.getType()) {
                    case LOCAL:
                        if (intProfile.getLocalIP().equals(candidate.getAddress())) {
                            localPriority = intProfile.getPriority();


                            break;


                        }
                    default:
                        if (candidate.getAddress().equals(intProfile.getPublicIP())) {
                            localPriority = intProfile.getPriority();


                            break;


                        }
                }
            }

            // Compute the priority
            candidate.computePriority(localPriority);


        } // Sort the candidates by priority
        Collections.sort(candidates, new CandidateComparison());

        // Return the value
        return candidates;


    }

    /**
     * Removes Makes a new list which has no duplicate candidates in it.  Does not
     * change the original list.
     * 
     * @param candidates Candidates to remove duplicates from.  Input candidates
     * must be prioritized for best results.
     * @return 
     */
    private List<LocalCandidate> removeDuplicates(List<LocalCandidate> candidates) {
        List<LocalCandidate> retval = new LinkedList<LocalCandidate>();

        // Loop through all input candidates, adding them one at a time
        for (LocalCandidate inputCandidate : candidates) {
            boolean addCandidate = true;
            LocalCandidate replaceCandidate = null;

            /**
             * Loop through existing candidates, searching for a matching public
             *  address
             */
            for (LocalCandidate checkCandidate : retval) {
                /**
                 * If we find an address match, determine which is the higher priority candidate
                 */
                if (checkCandidate.getAddress().equals(inputCandidate.getAddress())
                        && checkCandidate.getPort() == inputCandidate.getPort()) {
                    /**
                     * If the candidate we're checking has a higher or equal priority,
                     * don't add the new candidate.
                     * 
                     * Otherwise, replace the "checkCandidate" with this new candidate.
                     */
                    if (checkCandidate.getPriority() >= inputCandidate.getPriority()) {
                        addCandidate = false;
                        break;
                    } else {
                        replaceCandidate = checkCandidate;
                        break;
                    }
                }
            }

            /**
             * Adding and replacing is done after the check loop to avoid 
             *  concurrent modification issues
             */
            if (addCandidate) {
                retval.add(inputCandidate);
                if (replaceCandidate != null) {
                    retval.remove(replaceCandidate);
                }
            }
        }
        return retval;
    }

    /**
     * Match and update CandidatePairs that already exist with a newly formed list.
     * Called as part of the Media update process.
     *
     * @param matchCandidates Existing candidates to match new media to
     * @param checkPairs new candidates to match with existing media
     * @return merged and updated list of CandidatePairs
     */
    private Map<IceSocket, List<CandidatePair>> matchAndUpdate(Map<IceSocket, List<CandidatePair>> matchCandidates, Map<IceSocket, List<CandidatePair>> checkPairs) {
        for (IceSocket socket : matchCandidates.keySet()) {
            List<CandidatePair> original = matchCandidates.get(socket);
            List<CandidatePair> updates = checkPairs.get(socket);


            for (CandidatePair update : updates) {
                // Do exact matches and update status
                if (original.contains(update)) {
                    original.get(original.indexOf(update)).setState(update.getState());


                } // Find peer reflectives that match and add them
                if (!original.contains(update)
                        && update.getLocalCandidate().getBase() != null
                        && original.contains(new CandidatePair((LocalCandidate) update.getLocalCandidate().getBase(), update.getRemoteCandidate(), isLocalControlled()))) {
                    original.add(update);

                }

            }

        }

        return matchCandidates;

    }

    /**
     * Send data to a specific channel on a specific channel belonging to this peer
     *
     * @param channel Socket to send to
     * @param channel channel on channel to send to
     * @param buf data to send
     */
    void sendTo(IceSocket socket, short channel, ByteBuffer buf) throws IOException {
        if (nominated.containsKey(socket) && nominated.get(socket).size() > channel && nominated.get(socket).get(channel) != null) {
            CandidatePair pair = nominated.get(socket).get(channel);
            pair.getLocalCandidate().socket.send(buf, pair.remoteCandidate.getSocketAddress());
        }
    }

    /**
     * Nominate a specified candidate pair
     *
     * @param pair CandidatePair to nominate
     */
    private void nominate(CandidatePair pair) {
        IceSocket socket = pair.getLocalCandidate().getIceSocket();
        if (!nominated.containsKey(socket)) {
            nominated.put(socket, new ArrayList<CandidatePair>(socket.getComponents()));
            for (int i = 0; i < socket.getComponents(); i++) {
                nominated.get(socket).add(null);
            }
        }
        nominated.get(socket).set(pair.getComponentId(), pair);
    }

    /**
     * Get the highest priority candidates from a sorted list of Candidates for
     * use in constructing the default candidate list
     *
     * @param localCandidates A sorted list of local candidates
     * @param values the number of top values to siphon off
     * @return "values" number of the highest priority candidates from the localCandidates list
     */
    private List<LocalCandidate> getHighestPriorityLCs(List<LocalCandidate> localCandidates, short values) {
        List<LocalCandidate> retval = new LinkedList<LocalCandidate>();
        for (int i = 0; i < values; i++) {
            retval.add(localCandidates.get(i));
        }

        return retval;
    }

    public enum AgentRole {

        CONTROLLING, CONTROLLED
    }

    public enum NominationType {

        REGULAR, AGGRESSIVE
    }

    @Override
    public String toString() {
        String stringRep = "";


        boolean first = true;


        for (IceSocket socket : getIceSockets()) {
            if (!first) {
                stringRep += ",";


            }
            stringRep += socket;
            first = false;


        }

        return getClass().getName() + "[hashCode=" + hashCode() + ":sockets=" + stringRep + "]";


    }

    /**
     * Safely marks a particular candidate as a Nominated candidate.
     * This function can be called asynchronously to ICE processing
     * @param localSocket Local channel address to nominate
     * @param remoteSocket Remote channel address to nominate
     */
    void setUseCandidate(InetSocketAddress localSocket, InetSocketAddress remoteSocket) {
        //Set this target as the use case.
        // Check for an existing check pair
        CandidatePair pair = null;
        LocalCandidate local = null;

        for (List<CandidatePair> localCheckPairs : checkPairs.values()) {
            for (CandidatePair checkPair : localCheckPairs) {
                if (checkPair.getLocalCandidate().getSocketAddress().equals(localSocket)) {
                    local = checkPair.getLocalCandidate();
                    if (checkPair.getRemoteCandidate().getSocketAddress().equals(remoteSocket)) {
                        pair = checkPair;
                        break;


                    }
                }
            }
        }

        if (local == null) {
            // TODO: Unknown use candidate!
            log.log(Level.WARNING, "Request to nominate an unknown socket: {0} <-> {1}", new Object[]{localSocket, remoteSocket});


        } else {
            if (pair != null) {
                synchronized (nominated) {
                    if (!nominated.containsKey(pair.getLocalCandidate().getIceSocket())) {
                        nominated.put(pair.getLocalCandidate().getIceSocket(), new ArrayList<CandidatePair>(pair.getLocalCandidate().getIceSocket().getComponents()));
                        for (int i = 0; i < pair.getLocalCandidate().getIceSocket().getComponents(); i++) {
                            nominated.get(pair.getLocalCandidate().getIceSocket()).add(null);
                        }

                    }
                }
                nominated.get(pair.getLocalCandidate().getIceSocket()).set(pair.getLocalCandidate().getComponentId(), pair);


            } else {
                // Form the remote candidate using the local for reference
                RemoteCandidate remote = new RemoteCandidate(CandidateType.PEER_REFLEXIVE, remoteSocket.getAddress(), remoteSocket.getPort(), local.getComponentId(), local.getTransport(), local.getFoundation());
                pair = new CandidatePair(local, remote, isLocalControlled());
                pair.setState(PairState.SUCCEEDED);


                if (!nominated.containsKey(pair.getLocalCandidate().getIceSocket())) {
                    nominated.put(pair.getLocalCandidate().getIceSocket(), new ArrayList<CandidatePair>(pair.getLocalCandidate().getIceSocket().getComponents()));


                    for (int i = 0; i
                            < pair.getLocalCandidate().getIceSocket().getComponents(); i++) {
                        nominated.get(pair.getLocalCandidate().getIceSocket()).add(null);


                    }
                }
                nominated.get(pair.getLocalCandidate().getIceSocket()).set(pair.getLocalCandidate().getComponentId(), pair);


            }
        }
        checkStatus();


    }

    Collection<CandidatePair> getPairsFor(IceSocket socket) {
        return checkPairs.get(socket);


    }

    void addReflexiveCandidate(IceDatagramSocket socket, CandidatePair pair) {
        // TODO: Handle the new reflexive candidate
        log.log(Level.FINE, "New Reflexive Candidate: {0} <-> {1}", new Object[]{pair.getLocalCandidate().getSocketAddress(), pair.getRemoteCandidate().getSocketAddress()});


    }

    @Override
    public void close() {
        // Stop any ongoing ICE processing
        stop();
        // Close down all connections
        for (List<LocalCandidate> localCandidates : socketCandidateMap.values()) {
            for (LocalCandidate localCandidate : localCandidates) {
                try {
                    if (localCandidate.socket.isOpen()) {
                        localCandidate.socket.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(IceStateMachine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // Release references
        socketCandidateMap.clear();
        channels.clear();
        checkPairs.clear();
        iceSockets.clear();
        mediaCandidates.clear();
        nominated.clear();
        triggeredCheckQueue.clear();
    }

    @Override
    public void doReset(final boolean localControl) {
        doReset(localControl, true);
    }

    public synchronized void doReset(final boolean localControl, boolean resetPeer) {

        log.log(Level.WARNING, "{0} peer reset, type {1}", new Object[]{getPeerId(), resetPeer ? "hard" : "soft"});

        remoteUFrag = null;
        remotePassword = null;

        iceStatus = IceStatus.IN_PROGRESS;

        checkPairs.clear();
        triggeredCheckQueue.clear();

        if (resetPeer) {
            localUFrag = generateHashString(UFRAG_LENGTH);
            localPassword = generateHashString(PASSWORD_LENGTH);
        }
        nominated.clear();

        for (IceSocket socket : iceSockets) {
            getLocalCandidates(socket, true);
        }

        setLocalControlled(localControl);

        sendSessionUpdate();

    }

    public IceStatus getIceStatus() {
        return iceStatus;


    }

    public long getLastSent() {
        return lastSent;


    }

    @Override
    public String getLocalPassword() {
        return localPassword;


    }

    public AgentRole getLocalRole() {
        return localRole;


    }

    @Override
    public String getLocalUFrag() {
        return localUFrag;


    }

    public Map<IceSocket, Media> getMediaCandidates() {
        return mediaCandidates;


    }

    Map<IceSocket, List<LocalCandidate>> getSocketCandidateMap() {
        return socketCandidateMap;


    }

    public InetSocketAddress getStunServer() {
        return stunServer;

    }
    Map<IceSocket, List<IceSocketChannel>> channels = new HashMap<IceSocket, List<IceSocketChannel>>();

    @Override
    public List<IceSocketChannel> getChannels(final IceSocket socket) {
        if (!channels.containsKey(socket)) {
            LinkedList<IceSocketChannel> channelList = new LinkedList<IceSocketChannel>();
            for (short component = (short) 0; component < socket.getComponents(); component++) {
                channelList.add(new IceDatagramSocketChannel(this, socket, component));
            }
            List<LocalCandidate> localCandidates = getLocalCandidates(socket);
            for (LocalCandidate candidate : localCandidates) {
                candidate.socket.setStunEventListener((IceDatagramSocketChannel) channelList.get(candidate.getComponentId()));
            }
            channels.put(socket, channelList);
        }

        return channels.get(socket);
    }

    @Override
    public boolean hasRemoteAddress(SocketAddress address, IceSocket socket, Short componentId) {
        if (socket != null) {
            if (getNominated().get(socket) != null
                    && getNominated().get(socket).size() > componentId) {
                List<CandidatePair> pairs = getNominated().get(socket);
                if (componentId != null) {
                    if (pairs.get(componentId).getRemoteCandidate().getSocketAddress().equals(address)) {
                        return true;
                    }
                } else {
                    for (CandidatePair pair : pairs) {
                        if (pair.getRemoteCandidate().getSocketAddress().equals(address)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (List<CandidatePair> pairs : getNominated().values()) {
                if (componentId != null) {
                    if (pairs.get(componentId).getRemoteCandidate().getSocketAddress().equals(address)) {
                        return true;
                    }
                } else {
                    for (CandidatePair pair : pairs) {
                        if (pair.getRemoteCandidate().getSocketAddress().equals(address)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }
}
