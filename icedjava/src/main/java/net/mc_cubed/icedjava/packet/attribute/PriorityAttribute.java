/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.packet.attribute;

/**
 * Priority attributes are used during ICE processing to communicate the
 * priority which should be assigned by the remote peer to this test if it
 * succeeds.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface PriorityAttribute extends Attribute {
    /**
     * Get the priority which should be assigned to any new CandidatePairs
     * created during STUN processing.
     * 
     * @return The new pair's priority.
     */
    public int getNumber();
}
