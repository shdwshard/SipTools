/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.stun.event;

import net.mc_cubed.icedjava.stun.StunSocketChannel;

/**
 *
 * @author charles
 */
public interface BytesAvailableEvent extends StunEvent {
    StunSocketChannel getChannel();
}
