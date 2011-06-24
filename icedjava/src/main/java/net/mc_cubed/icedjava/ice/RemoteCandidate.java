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
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import javax.sdp.Attribute;
import javax.sdp.SdpParseException;
import net.mc_cubed.icedjava.stun.TCPSocketType;

/**
 * Implementation of a Remote Candidate, which is most often initialized from an
 * SDP Attribute line with the name "Candidate"
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class RemoteCandidate extends Candidate {

    private final CandidateType type;
    private final InetAddress address;
    private final int port;
    private long priority;
    private final short componentId;
    private final TransportType transport;
    protected String foundation;
    protected InetAddress baseAddress;
    protected int basePort;

    /**
     * Initialize from known parameters learned through SDP, or some other method
     *
     * @param type
     * @param address
     * @param port
     * @param componentId
     * @param transport
     * @param foundation
     */
    public RemoteCandidate(CandidateType type, InetAddress address, int port, short componentId, TransportType transport, String foundation) {
        this.type = type;
        this.address = address;
        this.port = port;
        this.componentId = componentId;
        this.transport = transport;
        this.foundation = foundation;
    }

    /**
     * Initialize from known parameters with a base candidate type provided
     *
     * @param type
     * @param address
     * @param port
     * @param foundation
     * @param base
     */
    public RemoteCandidate(CandidateType type, InetAddress address, int port, String foundation, Candidate base) {
        this(type, address, port, base.getComponentId(), base.getTransport(), foundation);
        this.base = base;
    }

    /**
     * Initialize from an SDP Attribute line
     *
     * @param srcAttribute
     * @throws SdpParseException
     * @throws UnknownHostException
     */
    public RemoteCandidate(Attribute srcAttribute) throws SdpParseException, UnknownHostException {
        StringTokenizer tokens = new StringTokenizer(srcAttribute.getValue());
        foundation = tokens.nextToken();
        componentId = Short.valueOf(tokens.nextToken());
        transport = TransportType.valueOf(tokens.nextToken());
        priority = Integer.valueOf(tokens.nextToken());
        address = InetAddress.getByName(tokens.nextToken());
        port = Integer.valueOf(tokens.nextToken());
        if (tokens.nextToken().compareTo("typ") != 0) {
            throw new SdpParseException(0, 0, "Expected 'typ' token, but did not find it!");
        };
        type = CandidateType.netValOf(tokens.nextToken());


        switch (type) {

            case LOCAL:
                // No remote values present
                break;
            default:
                // All other types give some kind of remote address/port combination
                if (tokens.nextToken().compareTo("raddr") != 0) {
                    throw new SdpParseException(0, 0, "Expected 'raddr' token, but did not find it!");
                }
                setBaseAddress(InetAddress.getByName(tokens.nextToken()));
                if (tokens.nextToken().compareTo("rport") != 0) {
                    throw new SdpParseException(0, 0, "Expected 'rport' token, but did not find it!");
                }
                setBasePort(Integer.valueOf(tokens.nextToken()));
                break;
        }
        
        if (transport == TransportType.TCP) {
            if (tokens.nextToken().compareTo("tcptype") != 0) {
                throw new SdpParseException(0, 0, "Expected 'tcptype' token, but did not find it!");                
            }
            socketType = TCPSocketType.fromNetworkString(tokens.nextToken());
        }
    }

    @Override
    public String getFoundation() {
        return this.foundation;
    }

    @Override
    protected final void setFoundation(String foundation) {
        this.foundation = foundation;
    }

    @Override
    protected void setBaseAddress(InetAddress baseAddress) {
        this.baseAddress = baseAddress;
    }

    @Override
    protected void setBasePort(int basePort) {
        this.basePort = basePort;
    }

    @Override
    protected InetAddress getBaseAddress() {
        return baseAddress;
    }

    @Override
    protected int getBasePort() {
        return basePort;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public short getComponentId() {
        return componentId;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public long getPriority() {
        return priority;
    }

    @Override
    public TransportType getTransport() {
        return transport;
    }

    @Override
    public CandidateType getType() {
        return type;
    }

    @Override
    public void setPriority(long priority) {
        this.priority = priority;
    }
}
