/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.util.Date;
import net.mc_cubed.icedjava.ice.event.IceStatusChangeEvent;

/**
 *
 * @author shdwshard
 */
public class IceStatusChangeEventImpl implements IceStatusChangeEvent {

    final private IceStatus lastStatus;
    final private IceStatus currentStatus;
    final private Date updatedTimestamp;
    final private IcePeer icePeer;

    public IceStatusChangeEventImpl(IceStatus lastStatus, IceStatus currentStatus, Date modifiedDate, IcePeer icePeer) {
        this.lastStatus = lastStatus;
        this.currentStatus = currentStatus;
        this.updatedTimestamp = modifiedDate;
        this.icePeer = icePeer;
    }
    
    @Override
    public IceStatus getLastStatus() {
        return this.lastStatus;
    }

    @Override
    public IceStatus getCurrentStatus() {
        return this.currentStatus;
    }

    @Override
    public Date getUpdatedTimestamp() {
        return this.updatedTimestamp;
    }

    @Override
    public IcePeer getIcePeer() {
        return this.icePeer;
    }
    
}
