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
package net.mc_cubed.icedjava.turn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Named;
import net.mc_cubed.icedjava.ice.AddressDiscovery;
import net.mc_cubed.icedjava.ice.DiscoveryMechanism;
import net.mc_cubed.icedjava.ice.LocalCandidate;

/**
 * Implements the AddressDiscovery interface for discovering TURN per addresses
 * for use during the ICE procedure.
 * 
 * WARNING: Currently just stubbed out, this class does nothing yet.
 *
 * @author Charles Chappell
 * @since 1.0
 */
@Named
@DiscoveryMechanism
public class TURNAddressDiscovery implements AddressDiscovery {

    @Override
    public Collection<LocalCandidate> getCandidates(Collection<LocalCandidate> baseCandidates) {
        List<LocalCandidate> retval = new LinkedList<LocalCandidate>();
        
        // TODO: Implement TURN address discovery options
        
        return retval;
    }
    
}
