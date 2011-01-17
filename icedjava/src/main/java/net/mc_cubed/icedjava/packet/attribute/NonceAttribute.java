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

import java.security.SecureRandom;

/**
 *
 * @author Charles Chappell
 */
public class NonceAttribute extends GenericAttribute {

    final String nonce;

    NonceAttribute() {
        String nonceGen = "";
        SecureRandom sr = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            Long nextLong = sr.nextLong();
            nonceGen += Long.toHexString(nextLong);
        }
        this.nonce = nonceGen;
        this.data = nonce.getBytes();
        this.length = data.length;
        this.type = AttributeType.NONCE;
    }

    NonceAttribute(String nonce) {
        this.nonce = nonce;
        this.data = nonce.getBytes();
        this.length = data.length;
        this.type = AttributeType.NONCE;
    }

    public NonceAttribute(AttributeType type, int length, byte[] value) {
        super(type, length, value);

        nonce = new String(data, 0, length);
    }

    public String getNonce() {
        return nonce;
    }
}
