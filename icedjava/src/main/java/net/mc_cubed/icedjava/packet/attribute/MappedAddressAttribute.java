/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.packet.attribute;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * MappedAddressAttributes indicate an address and port combination supplied to
 * or by a STUN peer.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface MappedAddressAttribute extends Attribute {

    /**
     * Get the address supplied by this attribute
     * 
     * @return an InetAddress representing the address stored in this attribute
     * May return either an Inet4Address or an Inet6Address
     * @see Inet4Address
     * @see Inet6Address
     */
    InetAddress getAddress();

    /**
     * Get the port number supplied by this attribute
     * 
     * @return the port number of this attribute
     */
    int getPort();

}
