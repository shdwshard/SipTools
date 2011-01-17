/*
 * Copyright 2010 Charles Chappell.
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

/**
 * A private interface to allow attaching and detaching peers from a socket<br/>
 * Generally performed by the peer object, hence the interface being package
 * local.<br/>
 * Introduced in 1.0 to allow peers to attach and de-attach from sockets without
 * needing to be destroyed and re-created.
 *
 * @author Charles Chappell
 * @since 1.0
 */
interface IcePeerMaintenance {
    public void removePeer(IcePeer peer);

    public void addPeer(IcePeer peer);

}
