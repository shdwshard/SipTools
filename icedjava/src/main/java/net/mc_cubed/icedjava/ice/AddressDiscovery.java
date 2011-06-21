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

import java.util.Collection;

/**
 * Provides a pluggable mechanism to facilitate LocalCandidate discovery prior
 * to commencing ICE processing. An extensible mechanism was chosen due to the
 * nature of ICE as a framework to incorporate many different discovery and
 * negotiation mechanisms.
 *
 * @author Charles Chappell
 * @since 1.0
 */
public interface AddressDiscovery {

    /**
     * Using the supplied local candidates as a base, implementing classes
     * form additional LocalCandidates and return them to the caller.
     * 
     * @param baseCandidates Base Candidates to use when forming new Local
     * Candidates to use during ICE processing.
     * @return An updated list of LocalCandidates with new candidates formed
     * by this Address Discovery Mechanism.
     */
    Collection<LocalCandidate> getCandidates(Collection<LocalCandidate> baseCandidates);

}
