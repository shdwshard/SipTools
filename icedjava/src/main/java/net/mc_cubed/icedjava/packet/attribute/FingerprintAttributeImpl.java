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

import net.mc_cubed.icedjava.util.NumericUtils;
import java.util.zip.CRC32;

/**
 *
 * @author Charles Chappell
 */
class FingerprintAttributeImpl extends GenericAttribute implements FingerprintAttribute {

    long crc32Value;
    boolean valid;

    public FingerprintAttributeImpl(AttributeType type, int length, byte[] value) {
        super(type, length, value);
        if (length != 4) {
            throw new IllegalArgumentException("Not a valid fingerprint attribute");
        } else {
            crc32Value = 0x00ffffffffL & NumericUtils.toInt(data);
        }

    }

    protected long computeCRC32(byte[] data, int offset, int length) {
        CRC32 crc = new CRC32();
        crc.reset();
        crc.update(data, offset, length);
        return crc.getValue() ^ 0x5354554e;

    }

    @Override
    public void computeHash(byte[] data, int offset, int length) {
        crc32Value = computeCRC32(data, offset, length);
        this.data = NumericUtils.toNetworkBytes((int) crc32Value);
        valid = true;
    }

    @Override
    public boolean verifyHash(byte[] data, int offset, int length) {
        long computedHash = computeCRC32(data, offset, length);
        valid = computedHash == crc32Value;
        return valid;
    }

    public FingerprintAttributeImpl() {
        this.type = AttributeType.FINGERPRINT;
        data = new byte[4];
        length = 4;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}
