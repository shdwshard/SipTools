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

    /**
     * Aligns numbers at an arbitrary multiple. Used mostly for byte alignment
     * @param number number to align
     * @param multiple multiple to align number to
     * @return number aligned to the next highest multiple of "multiple"
     */
    public static long makeMultipleOf(long number, long multiple) {
        return (number + (multiple - 1)) / multiple * multiple;
    }

    /**
     * Aligns numbers at an arbitrary multiple. Used mostly for byte alignment
     * @param number number to align
     * @param multiple multiple to align number to
     * @return number aligned to the next highest multiple of "multiple"
     */
    public static int makeMultipleOf(int number, int multiple) {
        return (number + (multiple - 1)) / multiple * multiple;
    }

    /**
     * Aligns numbers at an arbitrary multiple. Used mostly for byte alignment
     * @param number number to align
     * @param multiple multiple to align number to
     * @return number aligned to the next highest multiple of "multiple"
     */
    public static short makeMultipleOf(short number, short multiple) {
        return (short) ((number + (multiple - 1)) / multiple * multiple);
    }

    /**
     * Aligns numbers at an arbitrary multiple. Used mostly for byte alignment
     * @param number number to align
     * @param multiple multiple to align number to
     * @return number aligned to the next highest multiple of "multiple"
     */
    public static byte makeMultipleOf(byte number, byte multiple) {
        return (byte) ((number + (multiple - 1)) / multiple * multiple);
    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @return network ordered representation of number as a byte array
     */
    public static byte[] toNetworkBytes(long number) {
        byte[] retval = new byte[LONGSIZE];
        for (int i = LONGSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
        return retval;
    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @param buffer byte array to store the number in
     * @param off offset into the array to write the number to
     * @return network ordered representation of number as a byte array
     */
    public static void toNetworkBytes(long number, byte[] buffer, int off) {
        for (int i = LONGSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @return network ordered representation of number as a byte array
     */
    public static byte[] toNetworkBytes(int number) {
        byte[] retval = new byte[INTSIZE];
        for (int i = INTSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }
        return retval;

    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @param buffer byte array to store the number in
     * @param off offset into the array to write the number to
     * @return network ordered representation of number as a byte array
     */
    public static void toNetworkBytes(int number, byte[] buffer, int off) {
        for (int i = INTSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = number >>> 8;
        }

    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @return network ordered representation of number as a byte array
     */
    public static byte[] toNetworkBytes(short number) {
        byte[] retval = new byte[SHORTSIZE];
        for (int i = SHORTSIZE - 1; i >= 0; i--) {
            retval[i] = (byte) (0x00ff & number);
            number = (short) (number >>> 8);
        }
        return retval;

    }

    /**
     * Translates input number into bytes suitable for transmission over the
     * internet
     * @param number input number to translate into bytes
     * @param buffer byte array to store the number in
     * @param off offset into the array to write the number to
     * @return network ordered representation of number as a byte array
     */
    public static void toNetworkBytes(short number, byte[] buffer, int off) {
        for (int i = SHORTSIZE - 1; i >= 0; i--) {
            buffer[off + i] = (byte) (0x00ff & number);
            number = (short) (number >>> 8);
        }

    }

    /**
     * Translates a network order byte representation of a long back into a 
     * java primitive.
     * @param data network bytes representing a long
     * @return java primitive from the network bytes
     */
    public static long toLong(byte[] data) {
        return toLong(data, 0);
    }

    /**
     * Translates a network order byte representation of a long back into a 
     * java primitive.
     * @param data network bytes representing a long
     * @param offset offset into the array to start reading from
     * @return java primitive from the network bytes
     */
    public static long toLong(byte[] data, int offset) {
        long number = 0;
        for (int i = 0; i < LONGSIZE; i++) {
            number = number << 8 | (0x00ff & data[offset + i]);
        }
        return number;
    }

    /**
     * Translates a network order byte representation of an int back into a 
     * java primitive.
     * @param data network bytes representing a int
     * @return java primitive from the network bytes
     */
    public static long toInt(byte[] data) {
        return toInt(data, 0);
    }

    /**
     * Translates a network order byte representation of an int back into a 
     * java primitive.
     * @param data network bytes representing a int
     * @param offset offset into the array to start reading from
     * @return java primitive from the network bytes
     */
    public static long toInt(byte[] data, int offset) {
        long number = 0;
        for (int i = 0; i < INTSIZE; i++) {
            number = number << 8 | (0x00ff & data[offset + i]);
        }
        return number & 0x00ffffffff;
    }

    /**
     * Translates a network order byte representation of a short back into a 
     * java primitive.
     * @param data network bytes representing a short
     * @return java primitive from the network bytes
     */
    public static int toShort(byte[] data) {
        return toShort(data, 0);
    }

    /**
     * Translates a network order byte representation of a short back into a 
     * java primitive.
     * @param data network bytes representing a short
     * @param offset offset into the array to start reading from
     * @return java primitive from the network bytes
     */
    public static int toShort(byte[] data, int offset) {
        int number = 0;
        for (int i = 0; i < SHORTSIZE; i++) {
            number = (short) (number << 8 | (0x00ff & data[offset + i]));
        }
        return number & 0x0000ffff;
    }
}
