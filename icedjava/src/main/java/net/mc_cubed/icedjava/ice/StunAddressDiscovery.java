/*
 * Copyright 2011 Charles Chappell.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.stun.StunReply;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.annotation.StunServer;

/**
 * An AddressDiscoveryMechanism which provides LocalCandidates by performing
 * STUN tests on Host type candidates.  Used to be a part of IcePeer
 *
 * @author Charles Chappell
 * @since 1.0
 * @see AddressDiscovery
 * @see AddressDiscoveryMechanism
 * @see IcePeer
 */
@AddressDiscoveryMechanism
public class StunAddressDiscovery implements AddressDiscovery {

    @Inject
    @StunServer
    InetSocketAddress stunServer;

    @Override
    public Collection<LocalCandidate> getCandidates(Collection<LocalCandidate> lcs) {
        // In case we're not using WELD
        if (stunServer == null) {
            stunServer = StunUtil.getCachedStunServerSocket();
        }

        Map<LocalCandidate, Future<StunReply>> replyMap = new HashMap<LocalCandidate, Future<StunReply>>();
        // Collect Server Reflexive candidates
        List<LocalCandidate> reflexiveCandidates = new LinkedList<LocalCandidate>();
        for (LocalCandidate hostCandidate : lcs) {
            if (hostCandidate.getType() == CandidateType.LOCAL) {
                try {
                    Future<StunReply> replyFuture = hostCandidate.socket.doTest(stunServer);
                    replyMap.put(hostCandidate, replyFuture);
                } catch (IOException ex) {
                } catch (InterruptedException ex) {
                }
            }
        }

        for (Entry<LocalCandidate, Future<StunReply>> replyEntry : replyMap.entrySet()) {
            try {
                StunReply reply = replyEntry.getValue().get();
                LocalCandidate hostCandidate = replyEntry.getKey();
                if (reply != null && reply.isSuccess()) {
                    InetSocketAddress sockAddr = reply.getMappedAddress();
                    // If we got a reply, and it's not the same as our local entry
                    if (sockAddr != null
                            && sockAddr.getAddress() != hostCandidate.getAddress()
                            && sockAddr.getPort() != hostCandidate.getPort()) {
                        reflexiveCandidates.add(new LocalCandidate(
                                hostCandidate.getOwner(),
                                hostCandidate.getIceSocket(),
                                CandidateType.SERVER_REFLEXIVE,
                                sockAddr.getAddress(),
                                sockAddr.getPort(),
                                hostCandidate));
                    }
                }
            } catch (ExecutionException ex) {
                Logger.getLogger(StunAddressDiscovery.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            }
        }
        // Return everything we found
        return reflexiveCandidates;
    }
}
