/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import net.mc_cubed.icedjava.ice.event.BytesAvailableEvent;

/**
 *
 * @author charles
 */
class BytesAvailableEventImpl implements BytesAvailableEvent {

    final IceSocketChannel socketChannel;

    BytesAvailableEventImpl(IceSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public IceSocketChannel getSocketChannel() {
        return socketChannel;
    }
}