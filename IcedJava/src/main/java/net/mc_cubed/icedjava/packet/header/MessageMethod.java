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
package net.mc_cubed.icedjava.packet.header;

/**
 * MessageMethod represents a specific type of STUN message.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public enum MessageMethod {

    BINDING(0x0001),
    ALLOCATE(0x0003),
    REFRESH(0x0004),
    CHANNELBIND(0x0009),
    CREATE_PERMISSION(0x0008);
    private static final int BIT_MASK = 0xCEEF;
    private final int typeVal;

    MessageMethod(int val) {
        typeVal = val;
    }

    /**
     * Get the value for the header field of this particular Message Method
     * return the Message Method suitable for bitwise combination
     */
    public int getValue() {
        return typeVal;
    }

    /**
     * Get the MessageClass enum from the type field integer
     * @param typeField the typeField of the STUN packet.  Will be separated
     * from the message class by a bit mask in this method.
     * @return the appropriate MessageClass
     *
     */
    static MessageMethod getMessageMethod(int typeField) {
        switch (typeField & BIT_MASK) {
            // Had to hard code these constants
            case 0x0001:
                return BINDING;
            case 0x0003:
                return ALLOCATE;
            case 0x0004:
                return REFRESH;
            case 0x0009:
                return CHANNELBIND;
            case 0x0008:
                return CREATE_PERMISSION;
            // Not a valid Message Method type
            default:
                return null;
        }

    }
}
