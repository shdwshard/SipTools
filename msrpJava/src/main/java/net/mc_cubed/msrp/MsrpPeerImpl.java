/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.msrp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import net.mc_cubed.icedjava.ice.InterfaceProfile;

/**
 * Copyright 2010 Charles Chappell.
 *
 * This file is part of MsrpJava.
 *
 * MsrpJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * MsrpJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MsrpJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Charles Chappell <shdwshard@me.com>
 * @version 2010.1112
 * @since 1.0
 */

class MsrpPeerImpl extends MsrpAbstractEventHandler implements MsrpPeer {

    long messageId;
    
    protected Map<URI,MsrpConnection> connectionCache = new HashMap<URI,MsrpConnection>();
    
    protected final ServerSocket localSocket;

    protected boolean active = false;

    protected final String[] acceptTypes;

    @Inject
    Instance<List<InterfaceProfile>> interfaceProfiles;

    @Inject
    Instance<InterfaceProfile> bestInterfaceProfile;

    InterfaceProfile usedInterfaceProfile;

    SdpFactory sdpFactory = SdpFactory.getInstance();

    public MsrpPeerImpl() throws IOException {
        this((String)null);
    }

    public MsrpPeerImpl(String sessionId) throws IOException {
        this(sessionId,new InetSocketAddress(0));
    }
    /**
     * Constructor for local peer
     *
     * @param sessionId null to have a randomly generated session-id
     */
    public MsrpPeerImpl(String sessionId, InetSocketAddress sockAddr) throws IOException {
        if (sessionId == null) {
            this.sessionId = MsrpUtil.generateMsrpUriSessionId();
        } else {
            this.sessionId = sessionId;
        }
        this.localPeer = true;
        this.localSocket = new ServerSocket();

        if (sockAddr.getPort() == 0) {
            try {
                // First try the MSRP port
                localSocket.bind(new InetSocketAddress(sockAddr.getAddress(), MsrpUtil.MSRP_DEFAULT_PORT));
            } catch (IOException ex) {
                localSocket.bind(sockAddr);
            }

        } else {
            localSocket.bind(sockAddr);
        }

        this.acceptTypes = MsrpUtil.IMPL_SUPPORTED_TYPES;

        SecureRandom random = new SecureRandom();

        messageId = random.nextLong();
    }

    /**
     * Constructor for remote peer
     *
     */
    public MsrpPeerImpl(MediaDescription description) throws SdpParseException, URISyntaxException {
        // Basic sanity checks
        if (description.getMedia().getMediaType().compareTo(MsrpUtil.MSRP_SDP_MEDIA_TYPE) != 0 ||
                !MsrpUtil.MSRP_SDP_PROTOCOLS.contains(description.getMedia().getProtocol()) ||
                description.getAttribute(MsrpUtil.MSRP_SDP_ACCEPT_TYPES_ATTRIBUTE) == null ||
                description.getAttribute(MsrpUtil.MSRP_SDP_PATH_ATTRIBUTE) == null) {
            // Fast abort
            throw new IllegalArgumentException("MediaDescription did not"
                    + " contain a valid MSRP SDP entry");
        }
        String acceptTypesRaw = description.getAttribute(MsrpUtil.MSRP_SDP_ACCEPT_TYPES_ATTRIBUTE);
        String rawUri = description.getAttribute(MsrpUtil.MSRP_SDP_PATH_ATTRIBUTE);
        URI msrpUri = new URI(rawUri);
        // TODO: Additional checks to ensure this URI is a valid MSRP endpoint
        this.sessionId = msrpUri.getPath().substring(1);
        // Parse accept types
        String delims = "[ ]+";
        this.acceptTypes = acceptTypesRaw.split(delims);
        this.localPeer = false;
        this.localSocket = null;
    }

    /**
     * Constructor for remote peer
     */
    public MsrpPeerImpl(URI msrpUri,String ... acceptTypes) {
        // TODO: Additional checks to ensure this URI is a valid MSRP endpoint
        this.sessionId = msrpUri.getPath().substring(1);
        this.localPeer = false;
        this.localSocket = null;
        this.acceptTypes = acceptTypes;
    }
    
    @PostConstruct
    protected void init() {
        InetSocketAddress bindAddr = (InetSocketAddress) localSocket.getLocalSocketAddress();
        // Find matching local interface for this socket address
        if (!bindAddr.getAddress().isAnyLocalAddress()
                && !bindAddr.getAddress().isLinkLocalAddress()
                && !bindAddr.getAddress().isLoopbackAddress()
                && interfaceProfiles != null && !interfaceProfiles.isUnsatisfied()) {
            for (InterfaceProfile interfaceProfile : interfaceProfiles.get()) {
                if (interfaceProfile.getLocalIP().equals(bindAddr.getAddress())) {
                    // Found a matching profile
                    usedInterfaceProfile = interfaceProfile;
                }
            }
        }
    }

    /**
     * Gets a list of addresses to use as candidates for this socket
     * @return a list of candidate addresses for this socket, or the raw socket
     * information if candidates cannot be determined
     */
    @Override
    public InetSocketAddress[] getAddresses() {
        // Get the socket address for processing
        InetSocketAddress sockAddr = (InetSocketAddress) localSocket.getLocalSocketAddress();

        /*
         * If this is an "any" type address, do further processing to get the
         * list of addresses associated with this system
         */
        if (sockAddr.getAddress().isAnyLocalAddress()) {
            // Check whether we have Weld/IcedJava
            if (interfaceProfiles != null && !interfaceProfiles.isUnsatisfied()) {
                // Get the list of public addresses and pair them with the local port
                // TODO: Get a list of remote port numbers instead of using the local port
                ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>(interfaceProfiles.get().size());
                for (InterfaceProfile profile : interfaceProfiles.get()) {
                    list.add(new InetSocketAddress(profile.getPublicIP(), sockAddr.getPort()));
                }
                // Return the result
                return list.toArray(new InetSocketAddress[0]);
            } else {
                // Weld/IcedJava not present, return the socket address
                return new InetSocketAddress[]{sockAddr};
            }
        } else {
            // If we're bound to a single address, check for an interface profile
            if (usedInterfaceProfile != null) {
                // If present, pair it with the port and return this single result
                // TODO: Return the remote port, not the local port
                return new InetSocketAddress[]{
                            new InetSocketAddress(usedInterfaceProfile.getPublicIP(),
                            sockAddr.getPort())};
            } else {
                // Return the raw socket address
                return new InetSocketAddress[]{sockAddr};
            }
        }
    }
    
    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean sendMessage(URI toPath, String message) throws IOException  {
        return sendMessage(new MsrpMessage(null,toPath,"text/plain",message,messageId));
    }

    @Override
    public boolean sendMessage(MsrpMessage message) throws IOException {
        URI simplifiedURI = simplifyUri(message.toPath);
        if (!connectionCache.containsKey(simplifiedURI)) {
            connectionCache.put(simplifiedURI, new MsrpConnectionImpl(message.getToPath(),getSessionId()));
        }
        MsrpConnection conn = connectionCache.get(simplifiedURI);

        return conn.sendMessage(message);
    }

    @Override
    public URI[] getMsrpURIs() {
        ArrayList<URI> retval = new ArrayList<URI>();
        for (InetSocketAddress addr : getAddresses()) {
            try {
                retval.add(new URI((isSecure()) ? MsrpUtil.MSRP_SSL_URI_SCHEMA : MsrpUtil.MSRP_URI_SCHEMA, null, addr.getAddress().getCanonicalHostName(), addr.getPort(), "/" + getSessionId(), null, MsrpUtil.MSRP_FRAGMENT));
            } catch (URISyntaxException ex) {
                Logger.getLogger(MsrpPeerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retval.toArray(new URI[0]);
    }

    @Override
    public MediaDescription getMediaDescription() {
        MediaDescription md = null;
        try {
            if (sdpFactory == null) {
                sdpFactory = MsrpFactory.getSdpFactory();
            }
            md = sdpFactory.createMediaDescription(MsrpUtil.MSRP_SDP_MEDIA_TYPE, getAddresses()[0].getPort(), 1, (isSecure() ? MsrpUtil.MSRP_SDP_PROTOCOLS.get(1):MsrpUtil.MSRP_SDP_PROTOCOLS.get(0)), new String[] {"*"});
            md.setAttribute(MsrpUtil.MSRP_SDP_PATH_ATTRIBUTE, getMsrpURIs()[0].toString());
            String types = null;
            for (String type : MsrpUtil.IMPL_SUPPORTED_TYPES) {
                if (types == null) {
                    types = type;
                } else {
                    types = types + " " + type;
                }
            }
            md.setAttribute(MsrpUtil.MSRP_SDP_ACCEPT_TYPES_ATTRIBUTE, types);
        } catch (SdpException ex) {
            Logger.getLogger(MsrpPeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return md;
    }

    protected final String sessionId;

    /**
     * Get the value of transactionId
     *
     * @return the value of transactionId
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    protected final boolean localPeer;

    /**
     *
     * @return
     */
    @Override
    public boolean isLocalPeer() {
        return this.localPeer;
    }

    protected boolean secure;

    /**
     * Get the value of secure
     *
     * @return the value of secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Set the value of secure
     *
     * @param secure new value of secure
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public MsrpPeer[] getPeers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MsrpPeer getPeer(String sessionId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @PreDestroy
    public void shutdown() {
        try {
            localSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(MsrpPeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private URI simplifyUri(URI toPath) {
        // TODO: implement this in a meaningful way
        return toPath;
    }
}
