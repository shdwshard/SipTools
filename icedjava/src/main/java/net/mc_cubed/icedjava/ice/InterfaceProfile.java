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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;

/**
 * Represents an interface on the local system, which may or may not have been
 * successfully tested against STUN. Interfaces are prioritized according to
 * their characteristics, including an estimation of how long the STUN process
 * took, to determine their relative usefulness and prioritization.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class InterfaceProfile {

    private final NetworkInterface netIf;
    private final InetAddress publicIP;
    private final InetAddress localIP;
    private final CandidateType type;
    private final long testInterval;

    public InterfaceProfile(NetworkInterface netIf, InetAddress localIP, InetAddress publicIP, CandidateType type, long testInterval) {
        this.netIf = netIf;
        this.publicIP = publicIP;
        this.localIP = localIP;
        this.type = type;
        this.testInterval = testInterval;
    }

    public NetworkInterface getNetIf() {
        return netIf;
    }

    public InetAddress getPublicIP() {
        return publicIP;
    }

    public InetAddress getLocalIP() {
        return localIP;
    }

    public CandidateType getType() {
        return type;
    }

    public int getPriority() {
        int priority = 0;

        // Prioritize non link local (ie, DHCP or statically assigned) addresses
        if (!getLocalIP().isLinkLocalAddress()) {
            priority |= 4096;
        }
        if (getLocalIP() instanceof Inet6Address) {
            priority |= 2048;
        }
        // Prioritize interfaces with publicly facing, or publicly available IPs
        if (getPublicIP() != null) {
            priority |= 1024;

            // Further prioritize interfaces that are 100% public
            if (getPublicIP().equals(getLocalIP())) {
                priority |= 512;
            }
        }

        // If the test completed in less than 500ms, boost the lower
        //  bits so it scores higher in its respective group
        if (testInterval < 512) {
            // This inversion is nessisary so that lower latency hosts
            //  are assigned higher priorities in their respective group
            priority |= 512 - testInterval;
        }
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InterfaceProfile other = (InterfaceProfile) obj;
        if (this.netIf != other.netIf && (this.netIf == null || !this.netIf.equals(other.netIf))) {
            return false;
        }
        if (this.publicIP != other.publicIP && (this.publicIP == null || !this.publicIP.equals(other.publicIP))) {
            return false;
        }
        if (this.localIP != other.localIP && (this.localIP == null || !this.localIP.equals(other.localIP))) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.netIf != null ? this.netIf.hashCode() : 0);
        hash = 37 * hash + (this.publicIP != null ? this.publicIP.hashCode() : 0);
        hash = 37 * hash + (this.localIP != null ? this.localIP.hashCode() : 0);
        hash = 37 * hash + this.type.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[localIP=" + localIP + ":publicIP=" + publicIP + ":type=" + type + ":priority=" + getPriority() + "]";
    }

    /**
     * A convenience method that gets the best IP address for the interface.
     *
     * @return the value of getPublicIP() if it is not null, otherwise the value
     * of getLocalIP()
     */
    public InetAddress getAddress() {
        if (getPublicIP() != null) {
            return getPublicIP();
        } else {
            return getLocalIP();
        }
    }
}
