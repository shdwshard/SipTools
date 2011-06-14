/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice.event;

import java.util.EventListener;

/**
 *
 * @author charles
 */
public interface IceEventListener extends EventListener {

    void iceEvent(IceEvent event);
}
