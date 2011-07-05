/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import net.mc_cubed.icedjava.stun.StunUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author shadow
 */
public class IceUtilTest {

    final InetSocketAddress stunServer;

    public IceUtilTest() throws UnknownHostException {
        stunServer = StunUtil.getCachedStunServerSocket();
    }

    /**
     * Test of getBestInterfaceCandidate method, of class IceUtil.
     */
    @Test
    public void testGetBestInterfaceCandidate() throws Exception {
        System.out.println("getBestInterfaceCandidate");
        InterfaceProfile result = IceUtil.getBestInterfaceCandidate(stunServer);
        Assert.assertNotNull(result);

    }

    /**
     * Test of getInterfaceCandidates method, of class IceUtil.
     */
    @Test
    public void testGetInterfaceCandidates() {
        System.out.println("getInterfaceCandidates");
        List<InterfaceProfile> result = IceUtil.getInterfaceCandidates(stunServer);
        Assert.assertNotSame(0, result.size());
        for (InterfaceProfile ip : result) {
            System.out.println(ip);
        }

    }
}
