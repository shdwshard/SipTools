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

import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import java.util.LinkedList;
import java.util.List;

/**
 * Pairs a LocalCandidate and a RemoteCandidate and stores limited state
 * about them such as the pair status and overall priority of the pair
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class CandidatePair {

    protected final LocalCandidate localCandidate;
    protected final RemoteCandidate remoteCandidate;

    protected boolean localControlled;
    protected Long priority;

    /**
     * Get the value of localCandidate
     *
     * @return the value of localCandidate
     */
    public LocalCandidate getLocalCandidate() {
        return localCandidate;
    }

    /**
     * Get the value of remoteCandidate
     *
     * @return the value of remoteCandidate
     */
    public RemoteCandidate getRemoteCandidate() {
        return remoteCandidate;
    }

    /**
     * Constructs a minimal CandidatePair. localControlled must also be initialized,
     * but can be changed later.
     *
     * @param localCandidate The local candidate (to this machine) to be used in
     * this pair
     * @param remoteCandidate The remote candidate (from another machine) to be
     * used in this pair
     * @param localControlled true if the local machine is the controller in the
     * ICE relationship
     */
    public CandidatePair(LocalCandidate localCandidate, RemoteCandidate remoteCandidate, boolean localControlled) {
        // All pairs constructed MUST be valid pairs.
        if (!isValidPair(localCandidate,remoteCandidate)) {
            throw new IllegalArgumentException("Not a valid pair");
        }

        this.localCandidate = localCandidate;
        this.remoteCandidate = remoteCandidate;
        this.localControlled = localControlled;
    }

    /**
     * Evaluates whether a given LocalCandidate and RemoteCandidate would make a
     * valid pair
     * 
     * @param localCandidate Local Candidate to evaluate
     * @param remoteCandidate Remote Candidate to evaluate
     * @return true if the pair is valid, false otherwise
     */
    public static boolean isValidPair(LocalCandidate localCandidate, RemoteCandidate remoteCandidate) {
        // Address families MUST match
        if (localCandidate.getAddress().getClass() != remoteCandidate.getAddress().getClass()) {
            return false;
        }

        // Component IDs must match
        if (localCandidate.getComponentId() != remoteCandidate.getComponentId()) {
            return false;
        }

        // Only pair link local addresses with other link local addresses.
        if (localCandidate.getAddress().isLinkLocalAddress() ^ remoteCandidate.getAddress().isLinkLocalAddress()) {
            return false;
        }

        // Only pair site local addresses with other site local addresses.
        /*if (localCandidate.getAddress().isSiteLocalAddress() ^ remoteCandidate.getAddress().isSiteLocalAddress()) {
            return false;
        }*/

        // If the pairs are site local or link local, check to make sure their
        // network addresses match, otherwise we'll just waste time testing them
        if (/*localCandidate.getAddress().isSiteLocalAddress() ||*/ localCandidate.getAddress().isLinkLocalAddress()) {
            // TODO: Do this as specified by http://www.ietf.org/rfc/rfc1918.txt
            if (localCandidate.getAddress().getAddress()[0] != localCandidate.getAddress().getAddress()[0]) {
                return false;
            }
        }

        return true;
    }

    /**
     * From a given list of local and remote candidates, construct a list of valid
     * candidate pairs.
     *
     * @param locals A list of local candidates to consider
     * @param remotes a list of remote candidates to consider
     * @param localControlled true if the local machine is the controller in the
     * ICE relationship
     *
     * @return a list of valid CandidatePairs for the given local and remote
     * candidates
     */
    public static List<CandidatePair> getPairs(List<LocalCandidate> locals,
            List<RemoteCandidate> remotes, boolean localControlled) {

        List<CandidatePair> pairs = new LinkedList<CandidatePair>();
        for (LocalCandidate local : locals) {
            for (RemoteCandidate remote : remotes) {
                if (isValidPair(local, remote)) {
                    CandidatePair newPair;
                    // Cannot directly bind to a remote address, so use base
                    if (local.getBase() != null && local.getBase() != local &&
                            local.getType() != CandidateType.LOCAL) {
                        newPair = new CandidatePair((LocalCandidate) local.getBase(), remote, localControlled);
                    } else {
                        newPair = new CandidatePair(local, remote, localControlled);
                    }

                    // Make sure we don't already have this pair
                    if (!pairs.contains(newPair)) {
                        pairs.add(newPair);
                    }
                }
            }
        }

        return pairs;
    }

    /**
     * Candidate Pairs have a limited amount of data that needs to match for them
     * to be considered equal.
     *
     * @param obj Object to check for equality to this CandidatePair
     * @return true if the candidates in this CandidatePair match those of obj
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CandidatePair)) {
            return false;
        }

        final CandidatePair other = (CandidatePair) obj;

        if (this.localCandidate != other.localCandidate && (this.localCandidate == null || !this.localCandidate.equals(other.localCandidate))) {
            return false;
        }

        if (this.remoteCandidate != other.remoteCandidate && (this.remoteCandidate == null || !this.remoteCandidate.equals(other.remoteCandidate))) {
            return false;
        }
        return true;
    }

    /**
     * Similar to the equals method, only construct a hash code from the components
     * that will be tested for equality.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.localCandidate != null ? this.localCandidate.hashCode() : 0);
        hash = 17 * hash + (this.remoteCandidate != null ? this.remoteCandidate.hashCode() : 0);
        return hash;
    }
    protected PairState state = PairState.FROZEN;

    /**
     * Get the value of state
     *
     * @return the value of state
     */
    public PairState getState() {
        return state;
    }

    /**
     * Set the value of state
     *
     * @param state new value of state
     */
    public void setState(PairState state) {
        this.state = state;
    }

    /**
     * Returns the componentId, which identifies which socket in a group used by
     * a single media line this pair refers to.
     *
     * @return the componentId
     */
    short getComponentId() {
        return localCandidate.getComponentId();
    }

    /**
     * A user friendly representation of this CandidatePair that's informational
     * enough to be used in debugging.
     *
     * @return
     */
    @Override
    public String toString() {
        return getClass().getName() + "[local=" + localCandidate + ":remote=" + remoteCandidate + ":localControlled=" + localControlled + ":state=" + state + "]";
    }

    /**
     * Compute the priority of this candidatePair if it has not already been
     * computed, otherwise return the cached result.
     *
     * @return the priority of the candidate pair
     */
    public long getPriority() {
        long controlling = (localControlled) ? localCandidate.getPriority() : remoteCandidate.getPriority();
        long controlled = (localControlled) ? remoteCandidate.getPriority() : localCandidate.getPriority();

        if (priority == null) {
            priority = (2L << 31) * Math.min(controlling, controlled) +
                2 * Math.max(controlling, controlled) +
                ((controlling > controlled) ? 1 : 0);
        }
        
        return priority;

    }

    void setLocalControlled(boolean localControl) {
        this.localControlled = localControl;
        priority = null;
    }

    /**
     * Returns a string representing the foundation of this candidate pair.
     * Used during ICE processing to unfreeze candidates with the same foundation
     * when one is found that successfully completes. Acts to speed ICE processing.
     *
     * @return A string representation of the Local and Remote candidates suitable
     * for matching similar paths for ICE processing and testing.
     */
    public String getFoundation() {
        return localCandidate.getFoundation() + remoteCandidate.getFoundation();
    }
}
