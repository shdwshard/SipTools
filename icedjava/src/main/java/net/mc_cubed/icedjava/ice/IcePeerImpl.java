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

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;

/**
 * Extends the IceStateMachine and implements the IcePeer interface to form a
 * solution for negotiating multiple media lines with a peer.
 *
 * @author Charles Chappell
 * @since 0.9
 * @see IceStateMachine
 * @see IcePeer
 */
class IcePeerImpl extends IceStateMachine implements MultiDatagramListener, IcePeer {


    private ScheduledExecutorService threadpool;
    private final String peerId;
    private Map<String,IcePeer> myPeerMap;

    public IcePeerImpl(String peerId, AgentRole agentRole, IceSocket ... sockets) throws SdpException {
        this(peerId, agentRole, null, null, sockets);
    }

    public IcePeerImpl() throws SdpException {
        this((IceSocket)null);
    }

    public IcePeerImpl(IceSocket ... sockets) throws SdpException {
        this(null,AgentRole.CONTROLLING,sockets);
    }

    private MultiDatagramListener datagramListener;

    public MultiDatagramListener getDatagramListener() {
        return datagramListener;
    }

    public void setDatagramListener(MultiDatagramListener datagramListener) {
        this.datagramListener = datagramListener;
    }

    public IcePeerImpl(String peerId, AgentRole agentRole, String uFrag, String password,
            IceSocket... sockets) throws SdpParseException, SdpException {
        this(peerId,agentRole,uFrag,password,false,sockets);
    }
    
    public IcePeerImpl(String peerId, AgentRole agentRole, String uFrag, String password,
            boolean liteImplementation,IceSocket... sockets) throws SdpParseException, SdpException {

        super(null,agentRole,liteImplementation);
        
        if (sockets == null || sockets.length == 0) {
            throw new IllegalArgumentException("Null or zero number of sockets NOT permitted");
        }

        this.peerId = peerId;
        this.setIceSockets(sockets);

        if (agentRole == AgentRole.CONTROLLED) {
            this.setRemoteUFrag(uFrag);
            this.setRemotePassword(password);
        }

        // Set up the LocalCandidates for each Media Description

        setIceSockets(sockets);
    }

    @Override
    protected ScheduledExecutorService getThreadpool() {
        if (threadpool == null) {
            threadpool = Executors.newScheduledThreadPool(10);
        }

        return threadpool;
    }

    public void setThreadpool(ScheduledExecutorService threadpool) {
        this.threadpool = threadpool;
    }

    @Deprecated
    private int succeededPairs(List<CandidatePair> pairs) {
        int count = 0;
        for (CandidatePair pair : pairs) {
            if (pair.getState() == PairState.SUCCEEDED) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected Map<String, IcePeer> getPeerMap() {
        if (myPeerMap != null && !myPeerMap.containsKey(getLocalUFrag())) {
            myPeerMap = null;
        }
        
        if (myPeerMap == null || myPeerMap.isEmpty()) {
            myPeerMap = new HashMap<String,IcePeer>();
            myPeerMap.put(this.getLocalUFrag(), this);
        }
        return myPeerMap;
        
    }

    @Override
    public void deliverDatagram(DatagramPacket p, IceSocket source) {
        if (datagramListener != null) {
            datagramListener.deliverDatagram(p, source);
        } else {
            source.deliverDatagram(p);
        }
    }

    @Override
    public String getPeerId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
