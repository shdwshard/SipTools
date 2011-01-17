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

import net.mc_cubed.icedjava.stun.TransportType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Candidate abstract class defines the basic behavior of a Candidate used
 * in the ICE state machine. There are two candidate types, Local and Remote,
 * and this class serves as a superclass to each of them.
 *
 * @author Charles Chappell
 * @since 0.9
 * @see LocalCandidate
 * @see RemoteCandidate
 */
public abstract class Candidate {

    protected Date lastKeepalive;
    protected Candidate base;
    protected InetSocketAddress socketAddress;

    public abstract String getFoundation();

    protected abstract void setFoundation(String foundation);

    @Deprecated
    public void computePriority(Map<CandidateType, Integer> typePreference, int localPreference) {
        computePriority(localPreference);
    }

    public void computePriority(int localPreference) {
        setPriority((2 << 23) * getType().getPriority() + (2 << 7) * localPreference + (256 - getComponentId()));
    }

    @Override
    public String toString() {
        return getClass().getName() + "[type=" + getType() + ":address=" + getAddress() + ":port=" + getPort() + ":priority=" + getPriority() + ":componentId=" + getComponentId() + ((getBase() != this) ? ":base=" + getBase() : ":base=this") + "]";
    }

    public String toAttributeFormat() {
        return getFoundation() + " " + getComponentId() + " " + getTransport() + " " + getPriority() +
                " " + getAddress().getHostAddress() + " " + getPort() + " typ " + getType().netVal() +
                ((getType() != getType().LOCAL) ? " " + getBaseAddress().getHostAddress() +
                " " + getBasePort() : "");
    }

    abstract protected void setBaseAddress(InetAddress baseAddress);

    abstract protected void setBasePort(int basePort);

    abstract protected InetAddress getBaseAddress();

    abstract protected int getBasePort();

    /**
     * @return the address
     */
    public abstract InetAddress getAddress();

    /**
     * @return the port
     */
    public abstract int getPort();

    /**
     * @return the lastKeepalive
     */
    public Date getLastKeepalive() {
        return lastKeepalive;
    }

    /**
     * @param lastKeepalive the lastKeepalive to set
     */
    public void setLastKeepalive(Date lastKeepalive) {
        this.lastKeepalive = lastKeepalive;
    }

    /**
     * @return the priority
     */
    public abstract long getPriority();

    /**
     * @param priority the priority to set
     * @deprecated
     */
    public abstract void setPriority(long priority);

    /**
     * @return the componentId
     */
    public abstract short getComponentId();

    /**
     * @return the transport
     */
    public abstract TransportType getTransport();

    /**
     * @return the base
     */
    public Candidate getBase() {
        return base;
    }

    /**
     * @return the type
     */
    public abstract CandidateType getType();

    public enum CandidateType {

        LOCAL("host",126),              // Priority recommended by RFC 5245:4.1.2.2
        PEER_REFLEXIVE("prflx",110),    // Priority recommended by RFC 5245:4.1.2.2
        NAT_ASSISTED("nat",105),        // Priority recommended by ICE-TCP
        SERVER_REFLEXIVE("srflx",100),  // Priority recommended by RFC 5245:4.1.2.2
        UDP_TUNNELLED("udptnl",75),     // Priority recommended by ICE-TCP
        RELAYED("relay",0);             // Priority recommended by RFC 5245:4.1.2.2

        static final Map<String, CandidateType> revLookup =
                new HashMap<String, CandidateType>();

        static {
            for (CandidateType ct : EnumSet.allOf(CandidateType.class)) {
                revLookup.put(ct.netVal(), ct);
            }
        }
        protected final String netVal;
        protected final short  priority;

        CandidateType(String netVal,int priority) {
            this.netVal = netVal;
            this.priority = (short)priority;
        }

        static public CandidateType netValOf(String netVal) {
            return revLookup.get(netVal);
        }

        String netVal() {
            return netVal;
        }

        public short getPriority() {
            return priority;
        }

    }

    public InetSocketAddress getSocketAddress() {
        if (socketAddress == null) {
            socketAddress = new InetSocketAddress(getAddress(), getPort());
        }

        return socketAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Candidate other = (Candidate) obj;
        if (this.getType() != other.getType()) {
            return false;
        }
        if (this.getAddress() != other.getAddress() && (this.getAddress() == null || !this.getAddress().equals(other.getAddress()))) {
            return false;
        }
        if (this.getPort() != other.getPort()) {
            return false;
        }
        if (this.getTransport() != other.getTransport()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.getType() != null ? this.getType().hashCode() : 0);
        hash = 53 * hash + (this.getAddress() != null ? this.getAddress().hashCode() : 0);
        hash = 53 * hash + this.getPort();
        hash = 53 * hash + (this.getTransport() != null ? this.getTransport().hashCode() : 0);
        return hash;
    }

    
}
