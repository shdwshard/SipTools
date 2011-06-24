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
 * Compares the priority of Candidates for use by Arrays.sort and other methods.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class CandidateComparison implements Comparator<Candidate> {

    /**
     * Provides a comparison of two candidates for use in sorting. Ordering is
     * done by the priority of each candidate.
     *
     * @param firstCandidate first candidate to compare
     * @param secondCandidate second candidate to compare
     * @return the relative difference in priority of the second candidate from
     * the first candidate.  secondCandidate.priority - firstCandidate.priority
     * <br/>
     * &gt; 0 if firstCandidate has a higher priority<br/>
     * Zero if the priorities of both candidates are equal<br/>
     * &lt; 0 if secondCandidate has a higher priority
     */
    @Override
    public int compare(Candidate firstCandidate, Candidate secondCandidate) {
        return (int) (secondCandidate.getPriority() - firstCandidate.getPriority());
    }
}
