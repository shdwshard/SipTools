/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.packet.attribute;

/**
 * IntegrityAttribute is used as an authentication mechanism to verify both that
 * the STUN message was received unmodified, and to authenticate a STUN peer.
 * Credentials must be supplied to verify the hash, and so verification is
 * deferred until the verifyHash method is called.
 *
 * @author Charles Chappell
 * @since 0.9
 */
public interface IntegrityAttribute extends FingerprintAttribute {
    /**
     * Verify the integrity of the STUN packet using the supplied credentials
     * 
     * @param username Username credential
     * @param realm Realm credential
     * @param password Password credential
     * @return true if the credentials match and the STUN packet is unmodified,
     * false otherwise. To differentiate between mismatching credentials and a 
     * modified STUN packet, the Fingerprint Attribute may be checked if this
     * method returns false
     * 
     * @see FingerprintAttribute
     */
    public boolean verifyHash(String username, String realm, String password);
    /**
     * Verify the integrity of the STUN packet using the supplied credentials
     * 
     * @param verifyCredentials raw bytes of the credentials to be verified
     * @return true if the credentials match and the STUN packet is unmodified,
     * false otherwise. To differentiate between mismatching credentials and a 
     * modified STUN packet, the Fingerprint Attribute may be checked if this
     * method returns false
     * 
     * @see FingerprintAttribute
     */
    public boolean verifyHash(byte[] verifyCredentials);
}
