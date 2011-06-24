/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.util.List;

/**
 *
 * @author charles
 */
public interface CandidateDiscovery {
    List<LocalCandidate> discoverCandidates(IcePeer peer,IceSocket iceSocket);
}
