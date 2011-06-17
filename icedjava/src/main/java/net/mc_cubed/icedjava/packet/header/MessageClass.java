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
 * MessageClass is used to indicate a class of STUN message, and differentiates
 * primarily whether the message is a request/indication, or a success/error
 * reply.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public enum MessageClass {

    REQUEST(0x0000),
    INDICATION(0x0010),
    SUCCESS(0x0100),
    ERROR(0x0110);
    private static final int BIT_MASK = 0x0110;
    private final int typeVal;

    MessageClass(int val) {
        typeVal = val;
    }

    /*
     * Get the value for the header field of this particular Message Class
     * return the Message Class suitible for bitwise combination
     */
    public int getValue() {
        return typeVal;
    }

    /**
     * Get the MessageClass enum from the type field integer
     * @param typeField the typeField of the STUN packet.  Will be separated
     * from the message method by a bit mask in this method.
     * @return the appropriate MessageClass
     */
    static MessageClass getMessageClass(int typeField) {
        switch (typeField & BIT_MASK) {
            // Had to hard code these constants
            case 0x0000:
                return REQUEST;
            case 0x0010:
                return INDICATION;
            case 0x0100:
                return SUCCESS;
            case 0x0110:
                return ERROR;
            // Should never happen, but needed to avoid a compile time error.
            default:
                return null;
        }

    }
}
