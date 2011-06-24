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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the type of attribute, and maps between the attribute numbers and
 * POJO representations of those attributes
 *
 * @author Charles Chappell
 * @since 0.9
 */
public enum AttributeType {
    // Not an attribute, but an unknown attribute coping mechanism
    __UNKNOWN_ATTRIBUTE(-1,NullAttribute.class),
    // Mandatory Attributes
    MAPPED_ADDRESS(0x0001, MappedAddressAttributeImpl.class),
    _RESPONSE_ADDRESS(0x0002, MappedAddressAttributeImpl.class),
    _CHANGE_ADDRESS(0x0003, MappedAddressAttributeImpl.class),
    _SOURCE_ADDRESS(0x0004, MappedAddressAttributeImpl.class),
    _CHANGED_ADDRESS(0x0005, MappedAddressAttributeImpl.class),
    USERNAME(0x0006, StringAttribute.class),
    _PASSWORD(0x0007, StringAttribute.class),
    MESSAGE_INTEGRITY(0x0008, IntegrityAttributeImpl.class),
    ERROR_CODE(0x0009, ErrorCodeAttributeImpl.class),
    UNKNOWN_ATTRIBUTES(0x000A, UnknownAttributesAttributeImpl.class),
    _REFLECTED_FROM(0x000B, MappedAddressAttributeImpl.class),
    CHANNEL_NUMBER(0x000C,GenericAttribute.class),
    LIFETIME(0x000D,GenericAttribute.class),
    BANDWIDTH(0x0010,GenericAttribute.class),
    XOR_PEER_ADDRESS(0x0012,XORMappedAddressAttributeImpl.class),
    DATA(0x0013,GenericAttribute.class),
    REALM(0x0014, StringAttribute.class),
    NONCE(0x0015, NonceAttribute.class),
    XOR_RELAYED_ADDRESS(0x0016,GenericAttribute.class),
    REQUESTED_ADDRESS_TYPE(0x0017,GenericAttribute.class),
    EVEN_PORT(0x0018,GenericAttribute.class),
    REQUESTED_TRANSPORT(0x0019,GenericAttribute.class),
    DONT_FRAGMENT(0x001a,GenericAttribute.class),
    XOR_MAPPED_ADDRESS(0x0020, XORMappedAddressAttributeImpl.class),
    RESERVATION_TOKEN(0x0022,GenericAttribute.class),
    PRIORITY(0x0024, IntegerAttribute.class),
    USE_CANDIDATE(0x0025, NullAttribute.class),
    PADDING(0x0026,GenericAttribute.class),
    XOR_RESPONSE_TARGET(0x0027,XORMappedAddressAttributeImpl.class),
    XOR_REFLECTED_FROM(0x0028,XORMappedAddressAttributeImpl.class),
    ICMP(0x0029,GenericAttribute.class),
    // Optional Attributes
    UNKNOWN(0x8020, MappedAddressAttributeImpl.class),
    SOFTWARE(0x8022, StringAttribute.class),
    ALTERNATE_SERVER(0x8023, MappedAddressAttributeImpl.class),
    CACHE_TIMEOUT(0x8027,GenericAttribute.class),
    FINGERPRINT(0x8028, FingerprintAttributeImpl.class),
    ICE_CONTROLLED(0x8029, LongAttribute.class),
    ICE_CONTROLLING(0x802a, LongAttribute.class),
    RESPONSE_ORIGIN(0x802b, GenericAttribute.class),
    OTHER_ADDRESS(0x802c, GenericAttribute.class);
    private static final Map<Integer, AttributeType> revlookup =
            new HashMap<Integer, AttributeType>();

    static {
        for (AttributeType at : EnumSet.allOf(AttributeType.class)) {
            revlookup.put(at.getTypeVal(), at);
        }
    }

    /**
     * Lookup an attribute type based on the attribute number
     * 
     * @param lookup
     * @return 
     */
    public static AttributeType getAttributeType(int lookup) {
        int lookupVal = 0x0000ffff & lookup;
        if (revlookup.containsKey(lookupVal)) {
            return revlookup.get(lookupVal);
        } else {
            return AttributeType.__UNKNOWN_ATTRIBUTE;
        }
    }
    int typeVal;
    Class implementationClass;

    AttributeType(int val, Class<? extends Attribute> implementationClass) {
        this.typeVal = val;
        this.implementationClass = implementationClass;
    }

    /**
     * Gets the implementation class of this attribute type
     * @return 
     */
    Class getImplementationClass() {
        return implementationClass;
    }

    /**
     * Get the attribute value used on the wire
     * 
     * @return 
     */
    public int getTypeVal() {
        return typeVal;
    }
}
