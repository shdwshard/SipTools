package net.mc_cubed.icedjava.ice;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author shadow
 */
public class FormatUtilsTest extends TestCase{

    public FormatUtilsTest(String name) {
        super(name);
    }

    /**
     * Test of getAVPType method, of class FormatUtils.
     */
    public void testGetAVPType() {
        System.out.println("getAVPType");
        Format[] formats = new Format[] {
            new AudioFormat("bogus",8000,Format.NOT_SPECIFIED,1),
            new VideoFormat(VideoFormat.H263_RTP),
            new VideoFormat(VideoFormat.H261_RTP),
            new AudioFormat(AudioFormat.MPEG_RTP,90000,8,1),
            new AudioFormat(AudioFormat.MPEG_RTP,91000,8,1)
        };
        Integer[] expResults = new Integer[] {
            null,
            34,
            31,
            14,
            null
        };
        for (int i = 0; i < formats.length; i++) {
            Integer result = FormatUtils.getAVPType(formats[i]);
            Assert.assertEquals("Testing format " + formats[i],expResults[i], result);
        }
    }

    /**
     * Test of getAVPTypes method, of class FormatUtils.
     */
    public void testGetAVPTypes() {
        System.out.println("getAVPTypes");
        Format[] result = FormatUtils.getAVPTypes();
        Assert.assertEquals(35, result.length);
    }

    public void testKnownTypes() {
        System.out.println("testKnownTypes");
        Format[] formats = FormatUtils.getAVPTypes();
        int numTested = 0;
        for (int i = 0; i < formats.length; i++) {
            // Exclude null types from this test
            if (formats[i] != null) {
                Assert.assertEquals(i,FormatUtils.getAVPType(formats[i]).intValue());
                System.out.println(formats[i].toString() + " == " + i);
                numTested++;
            }
        }
        System.out.println(numTested + " unique formats tested");
    }

}