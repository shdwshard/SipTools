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
package net.mc_cubed.icedjava.ice.upnp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.ice.AddressDiscovery;
import net.mc_cubed.icedjava.ice.DiscoveryMechanism;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.ice.KeepaliveHandler;
import net.mc_cubed.icedjava.ice.LocalCandidate;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * Provides LocalCandidates by scanning the network for UPNP devices and
 * attempting to map local ports.
 *
 * @author Charles Chappell
 * @since 1.0
 * @see AddressDiscovery
 * @see DiscoveryMechanism
 * @see net.sbbi.upnp.impls.InternetGatewayDevice
 */
@DiscoveryMechanism
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class IceUPNPBridge implements AddressDiscovery, KeepaliveHandler {

    static final int discoveryTimeout = 5000; // 5 secs to receive a response from devices
    static InternetGatewayDevice[] devices;
    public final int KEEPALIVE_INTERVAL = 3600;

    public IceUPNPBridge() {
    }

    /**
     * Static initialization of this class does gateway device discovery to
     * avoid repeated discovery runs. In general, multiple discovery runs are
     * not useful for internet gateway devices.
     */
    static {
        try {
            devices = InternetGatewayDevice.getDevices(discoveryTimeout);
            if (devices != null) {
                for (int i = 0; i < devices.length; i++) {
                    Logger.getLogger(IceUPNPBridge.class.getName()).log(Level.FINE,
                            "Found device {0}",
                            devices[i].getIGDRootDevice().getModelName());
                }
            }
        } catch (IOException ex) {
            devices = null;
        }
    }

    /**
     * Performs UPNP discovery of candidates based on the LocalCandidates with
     * IPv4 addresses. IPv6 addresses are not, in general, modified by gateways
     * and the libraries used do not support IPv6 discovery
     *
     * @param localCandidates Candidates to use as a base for UPNP discovery.
     * This plugin filters all non-local candidates.
     * @return a list of UPNP discovered candidates based on the LOCAL candidates
     * supplied in the parameter
     */
    @Override
    public Collection<LocalCandidate> getCandidates(Collection<LocalCandidate> localCandidates) {
        Collection<LocalCandidate> retval = new LinkedList<LocalCandidate>();
        /**
         * Loop through all discovered gateway devices
         */
        for (InternetGatewayDevice device : devices) {
            try {
                /**
                 * Get the External IP address for the device being considered
                 */
                InetAddress natAddress = InetAddress.getByName(device.getExternalIPAddress());
                /**
                 * Loop through all candidates to consider which should be marked
                 * up by UPNP discovery
                 */
                for (LocalCandidate lc : localCandidates) {
                    /**
                     * Candidate collection should only be done on LOCAL
                     * candidates with ipv4 addresses
                     */
                    if (lc.getType() == CandidateType.LOCAL && lc.getAddress() instanceof Inet4Address) {
                        InetSocketAddress socketAddress = lc.getSocketAddress();
                        try {
                            /**
                             * Try to create a UPNP mapping for the given port
                             */
                            if (device.addPortMapping("IceUPNPBridge Mapping port: " + socketAddress.getPort(), null, socketAddress.getPort(), socketAddress.getPort(), socketAddress.getAddress().getHostAddress(), KEEPALIVE_INTERVAL, "UDP")) {
                                /**
                                 * If the mapping succeeds, add it to the return
                                 * list
                                 */
                                LocalCandidate newLc = new LocalCandidate(lc.getOwner(), lc.getIceSocket(), CandidateType.NAT_ASSISTED, natAddress, socketAddress.getPort(), lc);

                                newLc.setKeepaliveHandler(this);

                                newLc.setNextKeepalive(nextKeepaliveTime(KEEPALIVE_INTERVAL));

                                newLc.setKeepaliveObjectData(device);

                                retval.add(newLc);
                            }
                        } catch (IOException ex) {
                            /**
                             * In general, we're not TOO concerned about
                             * exceptions thrown by the UPNP library, but don't
                             * want to stop processing if one occurs.
                             */
                        } catch (UPNPResponseException ex) {
                            /**
                             * In general, we're not TOO concerned about
                             * exceptions thrown by the UPNP library, but don't
                             * want to stop processing if one occurs.
                             * 
                             * Here's what the documentation says this means.
                             * A more complete implementation to handle these
                             *  conditions would be optimal
                             * @throws UPNPResponseException if the device does not accept some settings :<br/>
                             *                               402 Invalid Args See UPnP Device Architecture section on Control<br/>
                             *                               501 Action Failed See UPnP Device Architecture section on Control<br/>
                             *                               715 WildCardNotPermittedInSrcIP The source IP address cannot be wild-carded<br/>
                             *                               716 WildCardNotPermittedInExtPort The external port cannot be wild-carded <br/>
                             *                               724 SamePortValuesRequired Internal and External port values must be the same<br/>
                             *                               725 OnlyPermanentLeasesSupported The NAT implementation only supports permanent lease times on port mappings<br/>
                             *                               726 RemoteHostOnlySupportsWildcard RemoteHost must be a wildcard and cannot be a specific IP address or DNS name<br/>
                             *                               727 ExternalPortOnlySupportsWildcard ExternalPort must be a wildcard and cannot be a specific port value
                             */
                        }
                    }
                }
            } catch (UPNPResponseException ex) {
                /**
                 * Exceptions thrown at this level are for the gateway device,
                 * so we don't want to try each and every port, but simply skip
                 * to the next UPNP device instead.
                 */
            } catch (IOException ex) {
                /**
                 * Exceptions thrown at this level are for the gateway device,
                 * so we don't want to try each and every port, but simply skip
                 * to the next UPNP device instead.
                 */
            }
        }

        return retval;
    }

    @Override
    public void doKeepalive(LocalCandidate lc) {
        InternetGatewayDevice device = (InternetGatewayDevice) lc.getKeepaliveObjectData();
        InetSocketAddress socketAddress = lc.getSocketAddress();
        try {
            /**
             * Try to create a UPNP mapping for the given port
             */
            if (device.addPortMapping("IceUPNPBridge Mapping port: " + socketAddress.getPort(), null, socketAddress.getPort(), socketAddress.getPort(), socketAddress.getAddress().getHostAddress(), KEEPALIVE_INTERVAL, "UDP")) {
                lc.setNextKeepalive(nextKeepaliveTime(KEEPALIVE_INTERVAL));
            } else {
                lc.setNextKeepalive(nextKeepaliveTime(20));
            }
        } catch (IOException ex) {
            /**
             * In general, we're not TOO concerned about
             * exceptions thrown by the UPNP library, but don't
             * want to stop processing if one occurs.
             */
            lc.setNextKeepalive(nextKeepaliveTime(20));
        } catch (UPNPResponseException ex) {
            /**
             * In general, we're not TOO concerned about
             * exceptions thrown by the UPNP library, but don't
             * want to stop processing if one occurs.
             *
             * Here's what the documentation says this means.
             * A more complete implementation to handle these
             *  conditions would be optimal
             * @throws UPNPResponseException if the device does not accept some settings :<br/>
             *                               402 Invalid Args See UPnP Device Architecture section on Control<br/>
             *                               501 Action Failed See UPnP Device Architecture section on Control<br/>
             *                               715 WildCardNotPermittedInSrcIP The source IP address cannot be wild-carded<br/>
             *                               716 WildCardNotPermittedInExtPort The external port cannot be wild-carded <br/>
             *                               724 SamePortValuesRequired Internal and External port values must be the same<br/>
             *                               725 OnlyPermanentLeasesSupported The NAT implementation only supports permanent lease times on port mappings<br/>
             *                               726 RemoteHostOnlySupportsWildcard RemoteHost must be a wildcard and cannot be a specific IP address or DNS name<br/>
             *                               727 ExternalPortOnlySupportsWildcard ExternalPort must be a wildcard and cannot be a specific port value
             */
            lc.setNextKeepalive(nextKeepaliveTime(20));
        }
    }

    /**
     * Calculate the next keep-alive time based on the given timeout interval
     * 
     * @param mapLifetime
     * @return 
     */
    Date nextKeepaliveTime(int mapLifetime) {
        // Calculate the next keepalive time                        
        Date nextKeepalive;
        if (mapLifetime > 600) {
            nextKeepalive = new Date(new Date().getTime() + mapLifetime * 900);
        } else {
            nextKeepalive = new Date(new Date().getTime() + mapLifetime * 500);
        }

        return nextKeepalive;
    }
}
