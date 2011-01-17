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
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.mc_cubed.icedjava.ice.AddressDiscovery;
import net.mc_cubed.icedjava.ice.AddressDiscoveryMechanism;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
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
 * @see AddressDiscoveryMechanism
 * @see net.sbbi.upnp.impls.InternetGatewayDevice
 */
@AddressDiscoveryMechanism
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class IceUPNPBridge implements AddressDiscovery {

    static final int discoveryTimeout = 5000; // 5 secs to receive a response from devices
    static InternetGatewayDevice[] devices;

    public IceUPNPBridge() {
    }

    static {
        try {
            devices = InternetGatewayDevice.getDevices(discoveryTimeout);
            if (devices != null) {
                for (int i = 0; i < devices.length; i++) {
                    Logger.getLogger(IceUPNPBridge.class.getName()).log(Level.INFO,
                            "Found device {0}",
                            devices[i].getIGDRootDevice().getModelName());
                }
            }
        } catch (IOException ex) {
            devices = null;
        }
    }

    @Override
    public Collection<LocalCandidate> getCandidates(Collection<LocalCandidate> lcs) {
        Collection<LocalCandidate> retval = new LinkedList<LocalCandidate>();
        for (InternetGatewayDevice device : devices) {
            try {
                InetAddress natAddress = InetAddress.getByName(device.getExternalIPAddress());
                for (LocalCandidate lc : lcs) {
                    /**
                     * Candidate collection should only be done on LOCAL
                     * candidates with ipv4 addresses
                     */
                    if (lc.getType() == CandidateType.LOCAL && lc.getAddress() instanceof Inet4Address) {
                        InetSocketAddress socketAddress = lc.getSocketAddress();
                        if (devices != null) {
                            try {
                                if (device.addPortMapping("IceUPNPBridge Mapping port: " + socketAddress.getPort(), null, socketAddress.getPort(), socketAddress.getPort(), socketAddress.getAddress().getHostAddress(), 0, "UDP")) {
                                    // IceStateMachine owner, IceSocket iceSocket, CandidateType type, InetAddress address, int port, LocalCandidate base
                                    retval.add(new LocalCandidate(lc.getOwner(), lc.getIceSocket(), CandidateType.NAT_ASSISTED, natAddress, socketAddress.getPort(), lc));
                                }
                            } catch (IOException ex) {
                            } catch (UPNPResponseException ex) {
                            }
                        }
                    }
                }
            } catch (UPNPResponseException ex) {
            } catch (IOException ex) {
            }
        }

        return retval;
    }
}
