/*
 * Copyright 2011 Charles Chappell.
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.stun.DemultiplexerSocket;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.TransportType;

/**
 * Does LocalCandidate discovery for IPv4 and IPv6 networks
 *
 * @author Charles Chappell
 * @since 1.0
 */
@Named
@DiscoveryMechanism
public class UDPCandidateDiscovery implements CandidateDiscovery {

    @Override
    public List<LocalCandidate> discoverCandidates(IcePeer peer, IceSocket iceSocket) {
        List<LocalCandidate> retval = new LinkedList<LocalCandidate>();        
        /**
         * This method only covers UDP peers over UDP transport, so ignore
         * non-UDP protocols
         */
        if (iceSocket.getTransport() == TransportType.UDP) {
            for (int componentId = 0; componentId < iceSocket.getComponents(); componentId++) {
                try {
                    Enumeration<NetworkInterface> ifaces =
                            NetworkInterface.getNetworkInterfaces();
                    while (ifaces.hasMoreElements()) {
                        NetworkInterface iface = ifaces.nextElement();

                        Enumeration<InetAddress> addresses = iface.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();

                            // Basic checking to eliminate unusable addresses
                            if (!address.isLoopbackAddress()
                                    && !address.isLinkLocalAddress()
                                    && !address.isAnyLocalAddress()
                                    && !address.isMulticastAddress()) {
                                DemultiplexerSocket socket = StunUtil.getCustomStunPipeline(new InetSocketAddress(address, 0), iceSocket.getTransport(),false, peer);
                                socket.setMaxRetries(4);
                                retval.add(new LocalCandidate(
                                        peer,
                                        iceSocket,
                                        CandidateType.LOCAL,
                                        socket,
                                        (short) componentId));
                            }
                        }

                    }
                } catch (IOException ex) {
                    Logger.getLogger(IceDatagramSocket.class.getName()).log(
                            Level.INFO, "Caught an exception during interface discovery.  Probably not serious.", ex);
                }
            }
        }
        return retval;
    }
}
