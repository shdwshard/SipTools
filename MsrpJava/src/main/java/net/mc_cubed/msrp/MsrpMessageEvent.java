/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.msrp;

/**
 *
 * @author charles
 */
public class MsrpMessageEvent extends MsrpEvent {

    private MsrpMessage message;

    public MsrpMessageEvent(Object source,MsrpMessage message) {
        super(source);
        this.message = message;
    }

    public MsrpMessage getMessage() {
        return message;
    }



}
