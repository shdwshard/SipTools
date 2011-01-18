/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.packet.attribute;

/**
 *
 * @author charles
 */
public interface IntegrityAttribute extends FingerprintAttribute {
    public boolean verifyHash(String username, String realm, String password);
    public boolean verifyHash(byte[] verifyCredentials);
}
