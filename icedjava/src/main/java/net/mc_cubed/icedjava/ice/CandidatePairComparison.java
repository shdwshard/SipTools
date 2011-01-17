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

import java.util.Comparator;

/**
 * Compares the priority of CandidateParis for use by Arrays.sort and other
 * methods.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class CandidatePairComparison implements Comparator<CandidatePair> {

    /**
     * Provides a comparison of two candidate pairs for use in sorting. Ordering
     * is done by the priority of each candidate.
     *
     * @param firstPair first candidate pair to compare
     * @param secondPair second candidate pair to compare
     * @return the relative difference in priority of the second pair from
     * the first pair.  secondCandidate.priority - firstCandidate.priority
     * <br/>
     * &gt; 0 if firstPair has a higher priority<br/>
     * Zero if the priorities of both candidates are equal<br/>
     * &lt; 0 if secondPair has a higher priority
     */
    @Override
    public int compare(CandidatePair firstPair, CandidatePair secondPair) {
        if (secondPair.getPriority() > firstPair.getPriority()) {
            return 1;
        } else if (secondPair.getPriority() == firstPair.getPriority()) {
            return 0;
        } else {
            return -1;
        }
        //return (int) (o2.getPriority() - o1.getPriority()) >> 32;
    }
}
