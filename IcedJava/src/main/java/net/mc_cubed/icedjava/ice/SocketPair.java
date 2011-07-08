/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.net.InetSocketAddress;

/**
 *
 * @author shdwshard
 */
class SocketPair {
 
    protected final InetSocketAddress localAddress;
    protected final InetSocketAddress remoteAddress;

    /**
     * Get the value of localAddress
     *
     * @return the value of localAddress
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public SocketPair(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    
}
