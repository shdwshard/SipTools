/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice.event;

import net.mc_cubed.icedjava.ice.IceSocketChannel;

/**
 *
 * @author charles
 */
public interface BytesAvailableEvent extends IceEvent {

    IceSocketChannel getSocketChannel();
    
}
