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
package net.mc_cubed.icedjava.stun;

import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.ErrorCodeAttribute;
import net.mc_cubed.icedjava.packet.attribute.FingerprintAttribute;
import net.mc_cubed.icedjava.packet.attribute.MappedAddressAttribute;
import net.mc_cubed.icedjava.packet.attribute.SoftwareAttribute;
import net.mc_cubed.icedjava.packet.attribute.XORMappedAddressAttribute;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A POJO representation of a STUN reply.  Encapsulates a STUN Packet
 *
 * @author Charles Chappell
 * @since 0.9
 */
class StunReplyImpl implements StunReply {

    private final StunPacket packet;
    private final boolean success;
    private InetSocketAddress mappedAddress;
    private int errorCode;
    private String errorReason;
    private Boolean validFingerprint;
    private Map<AttributeType, Attribute> attrMap = new HashMap<AttributeType, Attribute>();

    public StunReplyImpl(StunPacket packet) {
        success = packet.getMessageClass() == MessageClass.SUCCESS;
        this.packet = packet;
        for (Attribute attr : packet.getAttributes()) {
            attrMap.put(attr.getType(), attr);
            if (attr.getType() == AttributeType.MAPPED_ADDRESS) {
                MappedAddressAttribute maa = (MappedAddressAttribute) attr;
                mappedAddress = new InetSocketAddress(maa.getAddress(), maa.getPort());
            }
            if (attr.getType() == AttributeType.XOR_MAPPED_ADDRESS) {
                XORMappedAddressAttribute xmaa = (XORMappedAddressAttribute) attr;
                mappedAddress = new InetSocketAddress(xmaa.getAddress(packet.getTransactionId()), xmaa.getPort());
            }
            if (attr.getType() == AttributeType.ERROR_CODE) {
                ErrorCodeAttribute eca = (ErrorCodeAttribute) attr;
                errorCode = eca.getError();
                errorReason = eca.getReason();
            }
            if (attr.getType() == AttributeType.FINGERPRINT) {
                FingerprintAttribute finger = (FingerprintAttribute) attr;
                validFingerprint = finger.isValid();
            }
        }

    }
    public StunReplyImpl(Throwable ex) {
        packet = null;
        success = false;
        errorReason = ex.getLocalizedMessage();
        errorCode = -1;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorReason() {
        return errorReason;
    }

    @Override
    public InetSocketAddress getMappedAddress() {
        return mappedAddress;
    }

    @Override
    public Attribute getAttribute(AttributeType attrType) {
        return attrMap.get(attrType);
    }

    @Override
    public Collection<Attribute> getAttributes() {
        return packet.getAttributes();
    }

    @Override
    public String toString() {
        return getClass().getName() + "[success=" + success + ((success) ? ":mappedAddress=" + mappedAddress : ":errorCode=" + errorCode + ":errorReason=" + errorReason) + ((attrMap.containsKey(AttributeType.SOFTWARE)) ? ":software=" + ((SoftwareAttribute) attrMap.get(AttributeType.SOFTWARE)).getValue() : "") + ":validFingerprint=" + validFingerprint + "]";
    }

    @Override
    public Boolean isValidFingerprint() {
        return validFingerprint;
    }

    @Override
    public StunPacket getPacket() {
        return this.packet;
    }
}
