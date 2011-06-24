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
package net.mc_cubed.icedjava.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains a few utility methods for Hex String manipulation
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class StringUtils {

    static final byte[] HEX_CHAR_TABLE = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3',
        (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    /**
     * Gets a hex string representation of a byte stream
     * @param raw raw bytes to make into hex
     * @return a hex string representation from raw bytes 
     */
    public static String getHexString(byte[] raw) {
        // Special Case
        if (raw == null) {
            return "";
        }

        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        try {
            return new String(hex, "ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Gets a hex string representation of a portion of a byte stream
     * @param raw raw bytes to make into hex
     * @param length how many bytes to make into hex
     * @return a hex string representation from length bytes 
     */
    public static String getHexString(byte[] raw, int length) {
        byte[] hex = new byte[2 * length];
        int index = 0;

        for (int i = 0; i < length; i++) {
            int v = raw[i] & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        try {
            return new String(hex, "ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Gets a hex string representation of a portion of a byte stream starting
     * at off and ending after length bytes
     * @param raw raw bytes to make into hex
     * @param off offset from the beginning of the byte stream to start at
     * @param length how many bytes to make into hex
     * @return a hex string representation of length bytes starting at off bytes
     * into the byte array raw
     */
    public static String getHexString(byte[] raw, int off, int length) {
        byte[] hex = new byte[2 * length];
        int index = 0;

        for (int i = off; i < off + length; i++) {
            int v = raw[i] & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        try {
            return new String(hex, "ASCII");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

