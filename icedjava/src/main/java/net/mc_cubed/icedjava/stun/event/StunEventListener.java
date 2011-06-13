/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.stun.event;

import java.util.EventListener;

/**
 *
 * @author charles
 */
public interface StunEventListener extends EventListener {
    
    void stunEvent(StunEvent event);
    
}
