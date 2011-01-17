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

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

/**
 * A helper class used to bridge JMF formats to SDP formats
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class FormatUtils {

    static public final String AVP_STRING = "RTP/AVP";
    static public final int DYNAMIC = 96;
    
    static final Format avpTypes[];

    static {
        // AudioFormat(java.lang.String encoding, double sampleRate, int sampleSizeInBits, int channels)
        avpTypes = new Format[]{
                    new AudioFormat("PCMU/RTP", 8000, 8, 1), //0
                    null,//1
                    null,//2
                    new AudioFormat(AudioFormat.GSM_RTP, 8000, Format.NOT_SPECIFIED, 1),//3
                    new AudioFormat(AudioFormat.G723_RTP, 8000, Format.NOT_SPECIFIED, 1),//4
                    new AudioFormat(AudioFormat.DVI_RTP, 8000, 4, 1),//5
                    new AudioFormat(AudioFormat.DVI_RTP, 16000, 4, 1),//6
                    new AudioFormat("LPC/RTP", 8000, Format.NOT_SPECIFIED, 1),//7
                    new AudioFormat("PCMA/RTP", 8000, 8, 1),//8
                    new AudioFormat("G722/RTP", 8000, Format.NOT_SPECIFIED, 1),//9
                    new AudioFormat("L16/RTP", 44100, 16, 2, AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED),//10
                    new AudioFormat("L16/RTP", 44100, 16, 1, AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED),//11
                    new AudioFormat("QCELP/RTP", 8000, 16, 1), //12
                    new AudioFormat("CN/RTP", 8000, Format.NOT_SPECIFIED, 1), //13
                    new AudioFormat(AudioFormat.MPEG_RTP, 90000, AudioFormat.NOT_SPECIFIED, AudioFormat.NOT_SPECIFIED),//14
                    new AudioFormat(AudioFormat.G728_RTP, 8000, AudioFormat.NOT_SPECIFIED, 1),//15
                    new AudioFormat(AudioFormat.DVI_RTP, 11025, 4, 1),//16
                    new AudioFormat(AudioFormat.DVI_RTP, 22050, 4, 1),//17
                    new AudioFormat(AudioFormat.G729_RTP, 8000, Format.NOT_SPECIFIED, 1),//18
                    null,//19
                    null,//20
                    null,//21
                    null,//22
                    null,//23
                    null,//24
                    new VideoFormat("CelB/RTP"),//25
                    new VideoFormat(VideoFormat.JPEG_RTP),//26
                    null,//27
                    new VideoFormat("nv/rtp"),//28
                    null,//29
                    null,//30
                    new VideoFormat(VideoFormat.H261_RTP),//31
                    new VideoFormat(VideoFormat.MPEG_RTP),//32
                    new VideoFormat("MP2T/RTP"),//33
                    new VideoFormat(VideoFormat.H263_RTP)//34
                };
    }

    /**
     * Determines whether a given format is an AVP type
     * @param format The format to match
     * @return true if format is an AVP type, false if not
     */
    static public boolean isAVPType(Format format) {
        return getAVPType(format) != null;
    }

    /**
     * Finds the AVP type for a given Format
     * @param format The format to match
     * @return The AVP type or NULL if no match was found
     */
    static public Integer getAVPType(Format format) {
        int index = 0;
        for (Format checkFormat : avpTypes) {
            if (format instanceof VideoFormat && checkFormat instanceof VideoFormat) {
                if (format.isSameEncoding(checkFormat)) {
                    return index;
                }

            }
            if (format instanceof AudioFormat && checkFormat instanceof AudioFormat) {
                AudioFormat audioFormat = (AudioFormat) format;
                AudioFormat audioCheckFormat = (AudioFormat) checkFormat;
                if (audioFormat.isSameEncoding(audioCheckFormat) &&
                        (audioCheckFormat.getChannels() == Format.NOT_SPECIFIED ||
                        audioFormat.getChannels() == audioCheckFormat.getChannels()) &&
                        (audioCheckFormat.getSampleRate() == Format.NOT_SPECIFIED ||
                        audioFormat.getSampleRate() == audioCheckFormat.getSampleRate()) &&
                        (audioCheckFormat.getSampleSizeInBits() == Format.NOT_SPECIFIED ||
                        audioFormat.getSampleSizeInBits() == audioCheckFormat.getSampleSizeInBits()) &&
                        (audioCheckFormat.getEndian() == Format.NOT_SPECIFIED ||
                        audioFormat.getEndian() == audioCheckFormat.getEndian()) &&
                        (audioCheckFormat.getSigned() == Format.NOT_SPECIFIED ||
                        audioFormat.getSigned() == audioCheckFormat.getSigned())) {
                    return index;
                }
            }
            index++;
        }
        return null;
    }

    static public Format[] getAVPTypes() {
        return avpTypes;
    }
}
