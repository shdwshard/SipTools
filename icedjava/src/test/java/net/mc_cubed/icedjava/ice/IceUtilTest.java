/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.ice;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;
import net.mc_cubed.icedjava.ice.Candidate.CandidateType;
import net.mc_cubed.icedjava.stun.StunUtil;

/**
 *
 * @author shadow
 */
public class IceUtilTest extends TestCase {
    final InetSocketAddress stunServer;
    
    public IceUtilTest(String testName) throws UnknownHostException {
        super(testName);
         stunServer = StunUtil.getCachedStunServerSocket();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getBestInterfaceCandidate method, of class IceUtil.
     */
    public void testGetBestInterfaceCandidate() throws Exception {
        System.out.println("getBestInterfaceCandidate");
        InterfaceProfile result = IceUtil.getBestInterfaceCandidate(stunServer);
        Assert.assertNotNull(result);
        
    }

    /**
     * Test of getInterfaceCandidates method, of class IceUtil.
     */
    public void testGetInterfaceCandidates() {
        System.out.println("getInterfaceCandidates");
        List<InterfaceProfile> result = IceUtil.getInterfaceCandidates(stunServer);
        Assert.assertNotSame(0, result.size());
        for (InterfaceProfile ip : result) {
            System.out.println(ip);
        }

    }

}
