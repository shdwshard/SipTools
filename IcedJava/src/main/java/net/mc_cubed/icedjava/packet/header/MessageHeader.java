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

import net.mc_cubed.icedjava.util.NumericUtils;
import net.mc_cubed.icedjava.util.StringUtils;
import java.net.DatagramPacket;
import java.security.SecureRandom;
import net.mc_cubed.icedjava.packet.attribute.Attribute;
import net.mc_cubed.icedjava.packet.attribute.AttributeFactory;
import net.mc_cubed.icedjava.packet.attribute.AttributeType;
import net.mc_cubed.icedjava.packet.attribute.FingerprintAttribute;

/**
 * Represents, encodes and decodes the STUN Message Header and checks for
 * RFC 5389 compliance.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public class MessageHeader {

    /**
     * Tests whether the given data is a valid RFC 5389 STUN packet, or a response
     * to an RFC 5389 STUN packet. Utilizes the FINGERPRINT attribute if present
     * to enhance detection capabilities.
     *
     * @param p the DatagramPacket to check for validity
     * @return true if the DatagramPacket represents a valid STUN packet, false
     * otherwise
     * @deprecated use isRFC5389StunPacket(byte[], int, int) instead.
     */
    @Deprecated
    public static boolean isRfc5389StunPacket(DatagramPacket p) {
        byte[] data = p.getData();
        int off = p.getOffset();
        int length = p.getLength();
        return isRFC5389StunPacket(data, off, length);
    }

    /**
     * Tests whether the given data is a valid RFC 5389 STUN packet, or a response
     * to an RFC 5389 STUN packet. Utilizes the FINGERPRINT attribute if present
     * to enhance detection capabilities.<br/>
     * <br/>
     * Uses the following mechanisms:<br/>
     * <strong>RFC 5389 Section 6:</strong><br/>
     * The most significant 2 bits of every STUN message MUST be zeros.<br/>
     * The magic cookie field MUST contain the fixed value 0x2112A442... it aids
     * in distinguishing STUN packets from packets of other protocols<br/>
     * The message length MUST contain the size, in bytes of the message not
     * including the 20-byte STUN header. Since all STUN attributes are padded
     * to a multiple of 4 bytes, the last 2 bits of this field are always zero.
     * This provides another way to distinguish STUN packets<br/>
     * <br/>
     * <strong>RFC 5389 Section 8:</strong><br/>
     * When the FINGERPRINT extension is used, an agent includes the FINGERPRINT
     * attribute in messages it sends to another agent. Section 15.5 describes
     * the placement and value of this attribute. When the agent receives what
     * it believes is a STUN message, then, in addition to other basic checks,
     * the agent also checks that the message contains a FINGERPRINT attribute
     * and that the attribute contains the correct value.<br/>
     * <br/>
     * <strong>RFC 5389 Section 15.5:</strong><br/>
     * When present, the FINGERPRINT attribute MUST be the last attribute in the
     * message, and thus will appear after MESSAGE-INTEGRITY. The FINGERPRINT
     * attribute can aid in distinguishing STUN packets from packets of other
     * protocols.
     *
     * @param data a byte buffer containing the packet data
     * @param off an offset into the buffer where the data starts
     * @param length the number of bytes to check for a STUN packet
     * @return true if the data represents a valid STUN packet, false otherwise
     */
    public static boolean isRFC5389StunPacket(byte[] data, int off, int length) {
        if (length >= 20) {
            // Check the first two bits are zero
            if ((0x00C0 & data[off]) != 0) {
                return false;
            }

            // Check that the last two bits of the length field are 0
            // (length is always a multiple of 4 bytes)
            if ((0x0003 & data[off + 3]) != 00) {
                return false;
            }

            // Check for the magic value
            if (!checkRfc5389magic(data, off + 4, length)) {
                return false;
            }
            /*
             * <strong>RFC 5389 Section 15.5:</strong><br/>
             * When present, the FINGERPRINT attribute MUST be the last
             * attribute in the message.
             */
            int fingerprintoffset = off + length - 8;

            // Attributes must be past the end of the header.
            if (fingerprintoffset >= off + 20) {

                // Read the Attribute Type and Length
                int attrVal = NumericUtils.toShort(data, fingerprintoffset);
                AttributeType attrType = AttributeType.getAttributeType(attrVal);
                int attrLength = NumericUtils.toShort(data, fingerprintoffset + 2);

                // Check for correctness - Only fail if fingerprint validation fails,
                // Fingerprint might not be present
                if (attrType == AttributeType.FINGERPRINT && attrLength == 4) {
                    // Read the attribute
                    Attribute attribute =
                            AttributeFactory.processOneAttribute(data,off,fingerprintoffset);

                    // Validate whether the fingerprint is valid
                    if (attribute instanceof FingerprintAttribute) {
                        FingerprintAttribute fingerprintAttribute = (FingerprintAttribute) attribute;
                        if (!fingerprintAttribute.isValid()) {
                            return false;
                        }
                    } else {
                        return false;
                    }

                }
            }

            return true;
        } else {
            return false;
        }

    }
    private MessageClass messageClass;
    private MessageMethod messageMethod = MessageMethod.BINDING;

    /*
     *          Stun Message header according to RFC 5389 Section 6
     *  0                   1                   2                   3
     *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |0 0|	STUN Message Type      |	Message Length         |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                         Magic Cookie                          |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                                                               |
     * |                     Transaction ID (96 bits)                  |
     * |                                                               |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    /**
     * RFC 5389 Section 6:
     * The magic cookie field MUST contain the fixed value 0x2112A442 in network
     * byte order.
     */
    public static final byte[] MAGIC_COOKIE = new byte[]{(byte) 0x21, (byte) 0x12, (byte) 0xA4, (byte) 0x42};
    /**
     * transactionId should be big enough to hold 128 bits
     * This includes the MAGIC_COOKIE above.
     */
    final byte[] transactionId;
    /**
     * Indicates whether this header conforms to rfc5389. Will always be true
     * for generated headers, but may be false if packet was received from a
     * non-compliant client.
     */
    boolean rfc5389header = true;
    /**
     * Holds a binary version of this class' data suitable for network transmission
     */
    byte[] byteRepresentation;
    /**
     * Used to check that the body size has not changed (and hence made the byte
     * representation of this header invalid)
     */
    int bodySize;

    /**
     * Construct a default MessageHeader type (A request binding)
     * <strong>Note:</note> Defers to the designated constructor
     */
    public MessageHeader() {
        // Message type defaults
        this(MessageClass.REQUEST,
                MessageMethod.BINDING);
    }

    /**
     * Construct a MessageHeader with a specified class and method
     * <strong>Note:</note> Defers to the designated constructor
     *
     * @param messageClass Class of the message header
     * @param method Method of the message header
     * 
     */
    public MessageHeader(MessageClass messageClass, MessageMethod method) {
        this(messageClass, method, null);
    }

    /**
     * The designated constructor of the MessageHeader class
     * Constructs a MessageHeader with the specified attributes.
     *
     * @param messageClass Class of the STUN message header
     * @param method Method of the message header
     * @param transactionId transactionId value or NULL<br/>
     * If this value is null, or has a zero length, a transactionID will be
     * generated internally using the SecureRandom generator.<br/>
     * If the length of this value is &lt;= 12 bytes (96 bits), this value will
     * form the least significant bits of the message, and the magic cookie will
     * form the upper 32 bits.<br/>
     * If the length is &gt; than 12 bytes but less than 16 bytes, the magic
     * cookie will NOT be copied, and this id will make up the least significant
     * bits of the transactionID.<br/>
     * If the length is equal to or greater than 16 bytes, only the first 16
     * bytes will be used.
     */
    public MessageHeader(MessageClass messageClass, MessageMethod method, byte[] transactionId) {
        this.messageClass = messageClass;
        this.messageMethod = method;
        if (transactionId == null || transactionId.length == 0) {
            this.transactionId = generateTransactionId();
        } else {
            this.transactionId = new byte[16];
            int txLength = transactionId.length;
            if (txLength <= 12) {
                // Need to copy the magic cookie in as well
                System.arraycopy(MAGIC_COOKIE, 0, this.transactionId, 0, 4);
                System.arraycopy(transactionId, 0, this.transactionId, 16 - txLength, txLength);
            } else if (txLength <= 16) {
                // 16 >= txLength > 12
                // Don't copy in the Magic Cookie
                System.arraycopy(transactionId, 0, this.transactionId, 16 - txLength, txLength);
            } else {
                // txLength > 16
                // Only use the first 16 bytes
                System.arraycopy(transactionId, 0, this.transactionId, 0, 16);
            }

            // Check for Rfc5389 compliance
            checkRfc5389();
        }
    }

    /**
     * Generate an RFC 5389 compliant STUN transactionId
     * 
     * @return the byte representation of the transactionId in network order
     */
    public static byte[] generateTransactionId() {
        byte[] transactionId = new byte[16];
        // Get a secure PRNG
        SecureRandom random = new SecureRandom();
        // Get a secure transactionId
        random.nextBytes(transactionId);
        // Write the magic cookie to the first 32 bits
        System.arraycopy(MAGIC_COOKIE, 0, transactionId, 0, 4);
        return transactionId;
    }

    /**
     * Read the message header from the supplied bytes
     * <strong>WARNING:</strong>Does not check for message validity aside from a
     * length check
     *
     * @param headerBytes
     * @param off
     * @param len
     */
    public MessageHeader(byte[] headerBytes, int off, int len) {
        if (len < 20) {
            throw new java.lang.IllegalArgumentException(
                    "Need 20 header bytes for a valid STUN packet");
        }

        // Extract the messageType from network byte order
        int messageType = headerBytes[off] * 0x0100 | headerBytes[off + 1];

        // Extract the Message Type
        messageClass = MessageClass.getMessageClass(messageType);
        messageMethod = MessageMethod.getMessageMethod(messageType);

        // Extract the body size from network byte order
        bodySize = headerBytes[off + 2] * 0x0100 | headerBytes[off + 3];

        // Copy the transaction ID
        this.transactionId = new byte[16];
        System.arraycopy(headerBytes, off + 4, transactionId, 0, 16);

        // Check for Rfc5389 compliance
        checkRfc5389();
    }

    /**
     * Create a byte representation of this header suitable for network
     * transmission
     * @return a byte representation suitable for network transmission
     */
    public byte[] toBytes() {
        return toBytes(bodySize);
    }

    public byte[] toBytes(int bodyLength) {
        if (byteRepresentation == null) {
            byteRepresentation = new byte[20];
        }
        // First two bytes
        encodeMessageType();
        // Third and fourth bytes
        encodeLength(bodyLength);
        // 5th - 20th bytes
        System.arraycopy(transactionId, 0, byteRepresentation, 4, 16);

        // Done
        return byteRepresentation;
    }

    /**
     * Encode the message type into the header byte representation
     */
    protected void encodeMessageType() {
        // Calculate the messageType integer
        int messageType = messageClass.getValue() & messageMethod.getValue();
        // Encode network order into the byte representation of the header
        byteRepresentation[0] = (byte) (messageType & 0xff00 / 0x0100);
        byteRepresentation[1] = (byte) (messageType & 0x00ff);

    }

    /**
     * Encode the length of the message into the header byte representation
     * @param length
     */
    protected void encodeLength(int length) {
        byteRepresentation[2] = (byte) (length & 0xff00 / 0x0100);
        byteRepresentation[3] = (byte) (length & 0xff);
    }

    public MessageClass getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(MessageClass messageClass) {
        this.messageClass = messageClass;
    }

    public MessageMethod getMessageMethod() {
        return messageMethod;
    }

    public void setMessageMethod(MessageMethod messageMethod) {
        this.messageMethod = messageMethod;
    }

    public boolean isRfc5389header() {
        return rfc5389header;
    }

    /**
     * Checks the transactionId of this header for the RFC 5389 magic cookie
     */
    private void checkRfc5389() {
        rfc5389header = checkRfc5389magic(transactionId, 0, transactionId.length);
    }

    /**
     * Checks a given transactionId to determine whether it represents a correct
     * ID in the RFC 5389 sense.
     * 
     * @param transactionId
     * @param off
     * @param len
     * @return true if the first 4 bytes match the RFC 5389 magic cookie, false
     * otherwise
     */
    private static boolean checkRfc5389magic(byte[] transactionId, int off, int len) {
        // Check for adequate bytes
        if (off + 4 > len) {
            return false;
        }

        // Assume true until we discover otherwise
        boolean rfc5389check = true;

        // Compare the magic cookie to see whether we're rfc5389 or not
        for (int i = 0; i < 4; i++) {
            if (transactionId[off + i] != MAGIC_COOKIE[i]) {
                rfc5389check = false;
            }
        }

        return rfc5389check;
    }

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    /**
     * Write the byte representation of this MessageHeader to an arbitrary byte
     * buffer, and return the number of bytes written
     *
     * @param buffer
     * @param offset
     * @param length
     * @return
     */
    public int write(byte[] buffer, int offset, int length) {
        int messageType = messageClass.getValue() | messageMethod.getValue();
        NumericUtils.toNetworkBytes((short) messageType, buffer, offset);
        NumericUtils.toNetworkBytes((short) length, buffer, offset + 2);
        System.arraycopy(transactionId, 0, buffer, 4, 16);
        return 20;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[class=" + messageClass + ":method=" + messageMethod + ":transactionId=" + StringUtils.getHexString(transactionId) + "]";
    }

    public byte[] getTransactionId() {
        return transactionId;
    }
}
