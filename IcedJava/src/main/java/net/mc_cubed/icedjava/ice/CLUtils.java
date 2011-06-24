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

import java.util.Collection;

/**
 * CLUtils is a Utility Class used by the Ice State Machine to make determinations
 * about the current state of CandidatePair collections.
 *
 * @author Charles Chappell
 * @since 0.9
 */
class CLUtils {

    /**
     * Gets the next waiting candidate pair from a list of CandidatePairs, or null
     * if none exists.
     *
     * @param list List to check for waiting candidate pairs
     * @return The first candidate pair in the WAITING state, or null if none exist.
     */
    static public CandidatePair getNextWaiting(Collection<CandidatePair> list) {
        for (CandidatePair candidate : list) {
            if (candidate.getState() == PairState.WAITING) {
                return candidate;
            }
        }
        return null;
    }
}
