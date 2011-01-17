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

/**
 * A few utilities for manipulating numbers.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class NumericUtils {

    public static final int LONGSIZE = 8;
    public static final int INTSIZE = 4;
    public static final int SHORTSIZE = 2;

    public static long makeMultipleOf(long number, long multiple) {
        return (number + (multiple - 1)) / multiple * multiple;
    }

    public static int makeMultipleOf(int number, int multiple) {
        return (number + (multiple - 1)) / multiple * multiple;
    }

    public static short makeMultipleOf(short number, short multiple) {
        return (short) ((number + (multiple - 1)) / multiple * multiple);
    }

    public static byte makeMultipleOf(byte number, byte multiple) {
        return (byte) ((number + (multiple - 1)) / multiple * multiple);
    }

    public static byte[] toNetworkBytes(long number) {
        byte[] retval = new byte[LONGSIZE];
        for (int i = LONGSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
        return retval;
    }

    public static void toNetworkBytes(long number, byte[] buffer, int off) {
        for (int i = LONGSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
    }

    public static byte[] toNetworkBytes(int number) {
        byte[] retval = new byte[INTSIZE];
        for (int i = INTSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
        return retval;

    }

    public static void toNetworkBytes(int number, byte[] buffer, int off) {
        for (int i = INTSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }

    }

    public static byte[] toNetworkBytes(short number) {
        byte[] retval = new byte[SHORTSIZE];
        for (int i = SHORTSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = (short) (number >>> 8);
        }
        return retval;

    }

    public static void toNetworkBytes(short number, byte[] buffer, int off) {
        for (int i = SHORTSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = (short) (number >>> 8);
        }

    }

    public static long toLong(byte[] data) {
        return toLong(data, 0);
    }

    public static long toLong(byte[] data, int offset) {
        long number = 0;
        for (int i = 0; i < LONGSIZE; i++) {
            number = number << 8 | (0x00ff & data[offset + i]);
        }
        return number;
    }

    public static int toInt(byte[] data) {
        return toInt(data, 0);
    }

    public static int toInt(byte[] data, int offset) {
        int number = 0;
        for (int i = 0; i < INTSIZE; i++) {
            number = number << 8 | (0x00ff & data[offset + i]);
        }
        return number;
    }

    public static short toShort(byte[] data) {
        return toShort(data, 0);
    }

    public static short toShort(byte[] data, int offset) {
        short number = 0;
        for (int i = 0; i < SHORTSIZE; i++) {
            number = (short) (number << 8 | (0x00ff & data[offset + i]));
        }
        return number;
    }
}
