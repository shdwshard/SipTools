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

import java.util.logging.Level;
import net.mc_cubed.icedjava.stun.StunAuthenticator;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A generic StunAuthenticator used to authenticate ICE peers
 *
 * @author Charles Chappell
 * @since 0.9
 * @see StunAuthenticator
 * @see IcePeer
 * @see net.mc_cubed.icedjava.stun.GenericStunListener
 */
public class IcePeerAuthenticator implements StunAuthenticator {

    String username;
    String realm;
    final private Collection<IcePeer> peerList;

    public IcePeerAuthenticator(Collection<IcePeer> peerList) {
        this.peerList = peerList;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getPassword() {
        if (username == null) {
            return null;
        }
        for (IcePeer peer : peerList) {
            // Peer SENDING comes last, so local is first on the receiving side
            String peerUser = peer.getLocalUFrag() + ":";
            if (username.startsWith(peerUser)) {
                // Use the Peer's (Receiver's) password, so local here.
                return peer.getLocalPassword();
            }
        }
        Logger.getAnonymousLogger().log(Level.WARNING, "Didn''t find peer for {0}", username);
        return null;
    }

    @Override
    public void setUsername(String user) {
        this.username = user;
    }

    @Override
    public void setRealm(String realm) {
        this.realm = realm;
    }
}
