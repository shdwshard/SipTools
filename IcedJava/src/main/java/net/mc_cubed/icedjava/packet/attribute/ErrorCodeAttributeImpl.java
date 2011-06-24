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
package net.mc_cubed.icedjava.packet.attribute;

/**
 *
 * @author Charles Chappell
 * @since 0.9
 */
class ErrorCodeAttributeImpl extends GenericAttribute implements ErrorCodeAttribute {

    protected String reason;

    /**
     * Get the value of reason
     *
     * @return the value of reason
     */
    @Override
    public String getReason() {
        return reason;
    }
    protected int error;

    /**
     * Get the value of error
     *
     * @return the value of error
     */
    @Override
    public int getError() {
        return error;
    }

    protected ErrorCodeAttributeImpl(int error, String reason) {
        // Set up the members
        this.error = error;
        this.reason = reason;
        this.type = AttributeType.ERROR_CODE;
        this.length = 4 + reason.length();
        this.data = new byte[length];

        // Load the error codes into the correct slots
        data[2] = (byte) (error / 100);
        data[3] = (byte) (error % 100);

        // Copy the reason code
        System.arraycopy(reason.getBytes(), 0, data, 4, length - 4);

    }

    public ErrorCodeAttributeImpl(AttributeType type, int length, byte[] value) {
        super(type, length, value);

        error = data[2] * 100 + data[3];
        reason = new String(data, 4, length - 4);
    }
}
