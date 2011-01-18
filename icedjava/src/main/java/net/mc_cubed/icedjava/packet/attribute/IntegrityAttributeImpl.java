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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.mc_cubed.icedjava.util.StringUtils;

/**
 *
 * @author Charles Chappell
 */
class IntegrityAttributeImpl extends GenericAttribute implements IntegrityAttribute {

    byte[] credentials;
    byte[] savedData;
    boolean valid = false;
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    static public byte[] computeHMAC_SHA1(byte[] credentials, byte[] data, int offset, int length) {
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(credentials, HMAC_SHA1_ALGORITHM);
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);

            mac.init(signingKey);

            mac.update(data, offset, length);

            return mac.doFinal();
        } catch (InvalidKeyException ex) {
            Logger.getLogger(IntegrityAttribute.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IntegrityAttribute.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void computeHash(byte[] data, int offset, int length) {
        this.data = computeHMAC_SHA1(credentials, data, offset, length);
    }

    @Override
    public boolean verifyHash(String username, String realm, String password) {
        byte[] verifyCredentials = computeMD5(username + ":" + realm + ":" + password);
        return verifyHash(verifyCredentials);
    }

    @Override
    public boolean verifyHash(byte[] verifyCredentials) {
        return verifyHash(verifyCredentials,savedData,0,savedData.length);
    }

    public boolean verifyHash(byte[] verifyCredentials, byte[] data, int offset, int length) {
        // Compute the signature
        byte[] signature = computeHMAC_SHA1(verifyCredentials, data, offset, length);
        Logger.getAnonymousLogger().log(Level.FINEST, "Verify Signature: {0} {1}",
                new Object[]{StringUtils.getHexString(signature),
                StringUtils.getHexString(this.data)});

        // Presume valid
        valid = true;
        // The signature length MUST be 20 bytes
        if (signature.length != this.data.length || this.data.length != 20) {
            valid = false;
        } else {
            // Check that each byte of the signatures match
            for (int i = 0; i < signature.length; i++) {
                if (this.data[i] != signature[i]) {
                    valid = false;
                }
            }
        }
        return valid;
    }

    public IntegrityAttributeImpl(AttributeType type, int length, byte[] value) {
        super(type, length, value);
    }

    public IntegrityAttributeImpl(byte[] credentials) {
        this.credentials = credentials;
        this.length = 20; // The length of the hash

    }

    /**
     * Used when forming an integrityAttribute to be SENT to a remote client
     * during an ICE exchange.  Order of the credential string is:
     * Peer UFrag, Sender uFrag, Realm, Peer Password
     */
    @Deprecated
    public IntegrityAttributeImpl(String localUFrag, String remoteUFrag,
            String realm, String remotePassword) {
        this.type = AttributeType.MESSAGE_INTEGRITY;
        Logger.getAnonymousLogger().log(Level.FINER, "Forming Credentials: {0}:{1}:{2}:{3}",
                new Object[]{remoteUFrag, localUFrag, realm, remotePassword});
        this.credentials = computeMD5(remoteUFrag + ":" + localUFrag + ":" + realm + ":" + remotePassword);
        this.length = 20;
    }

    public IntegrityAttributeImpl(String username, String realm, String password) {
        this.type = AttributeType.MESSAGE_INTEGRITY;
        Logger.getAnonymousLogger().log(Level.FINER, "Forming Credentials: {0}:{1}:{2}",
                new Object[]{username, realm, password});
        this.credentials = computeMD5(username + ":" + realm + ":" + password);
        this.length = 20;

    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean verifyHash(byte[] data, int offset, int length) {
        /**
         * Make a copy of the data we'll be verifying against, since we cannot
         * guarantee it won't be modified.
         */
        savedData = Arrays.copyOfRange(data, offset, length);
        /**
         * This attribute cannot be verified without credentials, so return false
         * for now since we're deferring verification
         */
        return false;
    }

    protected static byte[] computeMD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            return md.digest(string.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GenericAttribute.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
