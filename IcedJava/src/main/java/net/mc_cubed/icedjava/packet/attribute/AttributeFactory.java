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

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Collection;
import net.mc_cubed.icedjava.util.NumericUtils;
import java.util.LinkedList;
import java.util.List;

/**
 * The factory class which creates and processes STUN attributes
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class AttributeFactory {

    /**
     * Given a buffer "packetBytes" which starts at "start" and is currently at
     * offset "off" with a length of "len", create a list of attributes.
     * 
     * @param packetBytes raw, network ordered bytes of the packet containing
     * STUN attributes
     * @param start Indicates the start of the STUN packet for Hash/Signature
     * use
     * @param off offset into the byte array where the STUN attributes start
     * @param len length of the packet buffer from off until the attributes end
     * @return 
     */
    public static List<Attribute> processIntoList(byte[] packetBytes, int start, int off, int len) {
        List<Attribute> attrList = new LinkedList<Attribute>();
        while (len > 0) {
            Attribute attr = GenericAttribute.process(packetBytes, start, off);
            if (attr != null) {
                // Add to the attribute list
                attrList.add(attr);
                // Move to the next attribute
                off += NumericUtils.makeMultipleOf(4 + attr.getLength(), 4);
                len -= NumericUtils.makeMultipleOf(4 + attr.getLength(), 4);
            } else {
                throw new RuntimeException("Encountered an error processing the attribute list");
            }
        }

        return attrList;
    }
    
    public static ErrorCodeAttribute createErrorCodeAttribute(int error, String reason) {
        return new ErrorCodeAttributeImpl(error,reason);
    }

    /**
     * Creates a mapped address attribute with the specified address and port
     * values. This method supports both IPv4 and IPv6 addresses
     * 
     * @param address an IPv4 or IPv6 address to encode
     * @param port a port number to encode
     * @return a MappedAddressAttribute with the given input parameters
     */
    public static MappedAddressAttribute createMappedAddressAttribute(InetAddress address, int port) {
        return new MappedAddressAttributeImpl(address, port);
    }

    /**
     * Creates an XORed mapped address attribute with the specified address and
     * port values. This method supports both IPv4 and IPv6 addresses.
     * XORMappedAddressAttributes are an attempt to bypass the aggressive IP
     * rewriting performed by some NAT devices.
     * 
     * @param address an IPv4 or IPv6 address to encode
     * @param port a port number to encode
     * @return a MappedAddressAttribute with the given input parameters
     */
    public static XORMappedAddressAttribute createXORMappedAddressAttribute(InetAddress address, int port,byte[] transactionId) {
        return new XORMappedAddressAttributeImpl(address, port, transactionId);
    }

    /**
     * Create a username attribute for STUN authentication purposes
     * 
     * @param username Username to encode
     * @return a Username Attribute with the given username
     */
    public static UsernameAttribute createUsernameAttribute(String username) {
        return new StringAttribute(AttributeType.USERNAME,username);
    }

    /**
     * Create a realm attribute for STUN authentication purposes
     * 
     * @param realm Realm name to encode
     * @return a Realm Attribute with the given realm name
     */
    public static RealmAttribute createRealmAttribute(String realm) {
        return new StringAttribute(AttributeType.REALM,realm);
    }

    /**
     * Create an integrity attribute with the given username, realm and password
     * used as the basis of the HMAC hash of the message.
     * 
     * @param username
     * @param realm
     * @param password
     * @return An integrity attribute with the given parameters encoded
     * actual processing is deferred until the packet is encoded to the network
     */
    public static IntegrityAttribute createIntegrityAttribute(String username, String realm, String password) {
        return new IntegrityAttributeImpl(username,realm,password);
    }

    /**
     * Create a fingerprint attribute which can be used to verify the integrity
     * of the STUN packet, and more accurately identify the existence of STUN
     * packets in a multiplexed stream
     * 
     * @return A fingerprint attribute. Actual fingerprinting is delayed until
     * the packet is encoded to the network
     */
    public static FingerprintAttribute createFingerprintAttribute() {
        return new FingerprintAttributeImpl();
    }

    /**
     * Create a software attribute for informational purposes, which identifies
     * the software being used to send/receive STUN packets.
     * 
     * @param softwareIdentifier an arbitrary software identifier string
     * @return A Software attribute with the given identifier encoded into it
     */
    public static SoftwareAttribute createSoftwareAttribute(String softwareIdentifier) {
        return new StringAttribute(AttributeType.SOFTWARE,softwareIdentifier);
    }

    /**
     * Create a NONCE attribute for use in STUN authentication.
     * @param nonce Use this string as the NONCE value
     * @return A Nonce Attribute with the given nonce value encoded into it.
     */
    public static NonceAttribute createNonceAttribute(String nonce) {
        if (nonce == null || nonce.length() == 0) {
            return createNonceAttribute();
        } else {
            return new StringAttribute(AttributeType.NONCE,nonce);
        }
    }

    /**
     * Create a NONCE attribute for use in STUN authentication.
     * Generates a long NONCE attribute using the SecureRandom generator
     * @return A Nonce Attribute with a long, nonce value generated using
     * SecureRandom.
     */
    public static NonceAttribute createNonceAttribute() {
        String nonce = "";
        SecureRandom sr = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            Long nextLong = sr.nextLong();
            nonce += Long.toHexString(nextLong);
        }
        return new StringAttribute(AttributeType.NONCE,nonce);
    }

    /**
     * Create a Priority attribute for use in STUN processing
     * @param priority assigns a priority to the STUN packet being sent.
     * @return A Priority attribute with the given priority value encoded
     */
    public static PriorityAttribute createPriorityAttribute(int priority) {
        return new IntegerAttribute(AttributeType.PRIORITY,priority);
    }

    /**
     * Create an IceControlling attribute with a specified tie breaker value
     * @param tieBreaker tieBreaker should be a randomly generated value that 
     * would vary greatly from computer to computer. Time should typically NOT
     * be used as a component of this value.
     * @return An IceControlling Attribute with an encoded tieBreaker value
     */
    public static Attribute createIceControllingAttribute(long tieBreaker) {
        return new LongAttribute(AttributeType.ICE_CONTROLLING,tieBreaker);
    }

    /**
     * Create an IceControlled attribute with a specified tie breaker value
     * @param tieBreaker tieBreaker should be a randomly generated value that 
     * would vary greatly from computer to computer. Time should typically NOT
     * be used as a component of this value.
     * @return An IceControlled Attribute with an encoded tieBreaker value
     */
    public static IceControlledAttribute createIceControlledAttribute(long tieBreaker) {
        return new LongAttribute(AttributeType.ICE_CONTROLLED,tieBreaker);
    }

    /**
     * Extract a single attribute from the given byte buffer, starting at 
     * attributeOffset
     * @param data raw bytes of the STUN packet
     * @param offset offset where the STUN packet begins.
     * @param attributeOffset offset where the attribute to decode begins
     * @return A fully processed Attribute
     */
    public static Attribute processOneAttribute(byte[] data, int offset, int attributeOffset) {
        return GenericAttribute.process(data, offset, attributeOffset);
    }

    /**
     * Creates an UnknownAttributesAttribute which specifies that the STUN
     * server did not understand the given list of attributes, and so is not
     * responding to them
     * @param attrs The attributes the server did not understand
     * @return An UnknownAttributes Attribute with the list of unknown attributes
     * encoded into it
     */
    public static UnknownAttributesAttribute createUnknownAttributesAttribute(Collection<AttributeType> attrs) {
        return new UnknownAttributesAttributeImpl(attrs);
    }

    /**
     * There's never a need to instantiate this class directly since all of its
     * methods are static.
     */
    private AttributeFactory() {

    }  
}
