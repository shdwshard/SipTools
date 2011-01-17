package net.mc_cubed.msrp;

import java.io.IOException;
import org.junit.Assert;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.AnnotatedType;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.Weld;
import java.net.URI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
public class MsrpPeerTest implements MsrpEventListener {

    static MsrpFactory factory;
    static MsrpPeerImpl remotePeer;
    static MsrpPeerImpl localPeer;
    static Weld weld;
    String thisMessage;
    
    public MsrpPeerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        weld = new Weld();
        WeldContainer container = weld.initialize();
        AnnotatedType<MsrpFactory> mfa = container.getBeanManager().createAnnotatedType(MsrpFactory.class);
        InjectionTarget<MsrpFactory> mfit = container.getBeanManager().createInjectionTarget(mfa);
        CreationalContext context = container.getBeanManager().createCreationalContext(null);
        factory = mfit.produce(context);
        remotePeer = (MsrpPeerImpl)factory.createPeer();
        localPeer = (MsrpPeerImpl)factory.createPeer();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        weld.shutdown();
    }

    /**
     * Test of addEventListener method, of class MsrpPeer.
     */
    @Test
    public void testAddEventListener() {
        remotePeer.addEventListener(this);
        assertTrue(remotePeer.listeners.contains(this));
    }

    /**
     * Test of removeEventListener method, of class MsrpPeer.
     */
    @Test
    public void testRemoveEventListener() {
        if (!remotePeer.listeners.contains(this)) {
            remotePeer.listeners.add(this);
        }

        remotePeer.removeEventListener(this);

        assertFalse(remotePeer.listeners.contains(this));
    }

    /**
     * Test of sendMessage method, of class MsrpPeer.
     */
    @Test
    public void testSendMessage() throws IOException {
        remotePeer.addEventListener(this);
        Assert.assertNull(thisMessage);
        System.out.println(remotePeer.getMsrpURIs()[0]);
        localPeer.sendMessage(remotePeer.getMsrpURIs()[0],"This is a test");
        Assert.assertNotNull(thisMessage);
    }

    /**
     * Test of getMsrpURIs method, of class MsrpPeer.
     */
    @Test
    public void testGetMsrpURIs() {
        for (URI uri : remotePeer.getMsrpURIs()) {
            System.out.println(uri);
        }
    }

    /**
     * Test of getMediaDescriptions method, of class MsrpPeer.
     */
    @Test
    public void testGetMediaDescription() {
        System.out.println(remotePeer.getMediaDescription());
    }

    /**
     * Test of isLocalPeer method, of class MsrpPeer.
     */
    @Test
    public void testIsLocalPeer() {
        assertTrue(remotePeer.isLocalPeer());
    }

    /**
     * Test of getSessionId method, of class MsrpPeer.
     */
    @Test
    public void testGetSessionId() {
        System.out.println(remotePeer.getSessionId());
    }

    @Override
    public void eventFired(MsrpEvent event) {
        if (event instanceof MsrpMessageEvent) {
            MsrpMessageEvent messageEvent = (MsrpMessageEvent) event;
            thisMessage = messageEvent.getMessage().getStringContent();
        }
    }
}