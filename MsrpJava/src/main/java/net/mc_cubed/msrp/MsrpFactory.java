package net.mc_cubed.msrp;

import java.io.IOException;
import java.net.InetSocketAddress;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sdp.SdpFactory;
import net.mc_cubed.msrp.annotation.MsrpAddress;

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
@Named
public class MsrpFactory {

    public MsrpFactory() {
    }

    @Inject
    public MsrpFactory(BeanManager beanManager) {
        this.beanManager = beanManager;
    }
    BeanManager beanManager;

    static SdpFactory getSdpFactory() {
        return SdpFactory.getInstance();
    }

    /**
     *
     * @return
     */
    public MsrpPeer createPeer() throws IOException {
        return createPeer(0);
    }

    /**
     *
     * @param port
     * @return
     */
    public MsrpPeer createPeer(int port) throws IOException {
        return createPeer(new InetSocketAddress(port));
    }

    /**
     *
     * @param sockAddr
     * @return
     */
    public MsrpPeer createPeer(InetSocketAddress sockAddr) throws IOException {
        MsrpPeerImpl retval = new MsrpPeerImpl(null,sockAddr);

        if (beanManager != null) {
            // Do injection if we are able to
            AnnotatedType<MsrpPeerImpl> msia = beanManager.createAnnotatedType(MsrpPeerImpl.class);
            InjectionTarget<MsrpPeerImpl> msiit = beanManager.createInjectionTarget(msia);
            CreationalContext context = beanManager.createCreationalContext(null);
            msiit.inject(retval, context);
            msiit.postConstruct(retval);
        }

        return retval;
    }

    /**
     * Factory method for the injected socket type
     * @param ip
     * @return
     */
    @Produces
    protected MsrpPeer createPeer(InjectionPoint ip) throws IOException {
        Annotated annotated = ip.getAnnotated();
        InetSocketAddress bindAddress = new InetSocketAddress(0);
        if (annotated.isAnnotationPresent(MsrpAddress.class)) {
            MsrpAddress address = annotated.getAnnotation(MsrpAddress.class);
            bindAddress = new InetSocketAddress(address.host(), address.port());
        }
        return createPeer(bindAddress);
    }
}
