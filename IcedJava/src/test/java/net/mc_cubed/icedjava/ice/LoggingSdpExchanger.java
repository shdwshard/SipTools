/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpParseException;

/**
 *
 * @author shadow
 */
public class LoggingSdpExchanger {

    final Logger log = Logger.getLogger(LoggingSdpExchanger.class.getName());

    public LoggingSdpExchanger(final IcePeer source1, final IcePeer source2) {
        source1.setSdpListener(new SDPListener() {

            @Override
            public void updateMedia(Origin origin, Connection conn, Vector iceAttributes, Vector iceMedias) throws SdpParseException {
                updateMedia(origin, conn, (List) iceAttributes, (List) iceMedias);
            }

            @Override
            public void updateMedia(final Origin origin, final Connection conn, final List<Attribute> iceAttributes, final List<MediaDescription> iceMedias) throws SdpParseException {
                Runnable updateRunner = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            log.log(Level.INFO, "{0}\n{1}\n{2}\n{3}", new Object[]{origin, conn, iceAttributes, iceMedias});
                            source2.updateMedia(origin, conn, iceAttributes, iceMedias);
                        } catch (SdpParseException ex) {
                            Logger.getLogger(LoggingSdpExchanger.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                updateRunner.run();
            }
        });
        source2.setSdpListener(new SDPListener() {

            @Override
            public void updateMedia(Origin origin, Connection conn, Vector iceAttributes, Vector iceMedias) throws SdpParseException {
                updateMedia(origin, conn, (List) iceAttributes, (List) iceMedias);
            }

            @Override
            public void updateMedia(final Origin origin, final Connection conn, final List<Attribute> iceAttributes, final List<MediaDescription> iceMedias) throws SdpParseException {
                Runnable updateRunner = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            log.log(Level.INFO, "{0}\n{1}\n{2}\n{3}", new Object[]{origin, conn, iceAttributes, iceMedias});
                            source1.updateMedia(origin, conn, iceAttributes, iceMedias);
                        } catch (SdpParseException ex) {
                            Logger.getLogger(LoggingSdpExchanger.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(LoggingSdpExchanger.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                
                new Thread(updateRunner).start();
            }
        });
    }
}
