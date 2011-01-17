/*
 * Copyright 2009 Charles Chappell.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.stun.StunListenerType;
import net.mc_cubed.icedjava.stun.StunReply;
import net.mc_cubed.icedjava.stun.DatagramStunSocket;
import net.mc_cubed.icedjava.stun.StunUtil;
import net.mc_cubed.icedjava.stun.annotation.StunServer;
import net.mc_cubed.icedjava.util.ExpiringCache;

/**
 * A series of useful ICE functions for both internal, and application use.
 *
 * @author Charles Chappell
 * @since 0.9
 */
@Named
public class IceUtil {

    protected static ExpiringCache<InetSocketAddress, List<InterfaceProfile>> cacheList =
            new ExpiringCache<InetSocketAddress, List<InterfaceProfile>>();

    /**
     * Returns the most likely to succeed interface candidate based on tests
     *  from a specified Stun Server.  Results will be different for an IP6 vs
     *  IP4 stun server.
     *
     * Prioritization is as follows:
     * 1. Public Network Interfaces (Stun reflected address = Local Address)
     * 2. Server reflection successful (Mapped address is the PublicIP field of
     *  InterfaceProfile)
     * 3. Local IPs only (PublicIP field will be null)
     * @param stunServer The stun server to use in these tests
     * @return The most likely to succeed interface based on the tests
     */
    @Produces
    public static InterfaceProfile getBestInterfaceCandidate(@StunServer InetSocketAddress stunServer) {
        List<InterfaceProfile> ifaceList = cacheList.get(stunServer);
        if (ifaceList == null) {
            ifaceList = doInterfaceDiscovery(stunServer);
            cacheList.admit(stunServer, ifaceList);
        }

        if (ifaceList != null) {
            return ifaceList.get(0);
        } else {
            return null;
        }

    }

    /**
     * Gets a list of interface candidates.  This draws on the same data as
     * "getBestInterfaceCandidate" and both functions will cache results, so
     * successive calls with the same InetSocketAddress will result in no test
     * duplication.
     * @param stunServer Stun server to test against
     * @return A list of candidates, prioritized and sorted
     */
    @Produces
    public static List<InterfaceProfile> getInterfaceCandidates(@StunServer InetSocketAddress stunServer) {
        List<InterfaceProfile> ifaceList = cacheList.get(stunServer);
        if (ifaceList == null) {
            ifaceList = doInterfaceDiscovery(stunServer);
            cacheList.admit(stunServer, ifaceList);
        }

        return ifaceList;
    }

    /**
     * This function does Interface discovery, and should not be used directly,
     *  as it does not implement caching.  Instead, use getBestInterfaceCandidate
     *  or GetInterfaceCandidates.
     * @param stunServer Stun server to run tests against
     * @return A list of candidates prioritized and sorted
     */
    protected static List<InterfaceProfile> doInterfaceDiscovery(InetSocketAddress stunServer) {
        // Check the interfaces to see which can contact the internet
        List<InterfaceProfile> ifaceList = new ArrayList<InterfaceProfile>();

        try {

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();

                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    DatagramStunSocket socket = null;
                    if (!addr.isLinkLocalAddress() && !addr.isLoopbackAddress() 
                            && !addr.isMulticastAddress()) {
                        try {
                            // Create a stun socket to measure this IP.
                            socket = StunUtil.getStunSocket(new InetSocketAddress(addr, 0), StunListenerType.CLIENT);

                            long startTime = new Date().getTime();
                            StunReply reply = socket.doTest(stunServer).get();
                            long testInterval = new Date().getTime() - startTime;

                            if (reply.isSuccess()) {
                                ifaceList.add(new InterfaceProfile(
                                        iface,
                                        addr,
                                        ((InetSocketAddress) reply.getMappedAddress()).getAddress(),
                                        CandidateType.SERVER_REFLEXIVE,
                                        testInterval));
                            } else {
                                ifaceList.add(new InterfaceProfile(iface,
                                        addr,
                                        null,
                                        CandidateType.LOCAL,
                                        testInterval));
                            }
                        } catch (IOException ex) {
                            // Ignore this type of exception and go on to the next test.
                            // Sockets that return an IOException on testing won't be
                            //  included in the candidate list.
                        } finally {
                            socket.close();
                        }
                    }
                }
            }

            Collections.sort(ifaceList, new InterfaceProfileComparator());

            return ifaceList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
