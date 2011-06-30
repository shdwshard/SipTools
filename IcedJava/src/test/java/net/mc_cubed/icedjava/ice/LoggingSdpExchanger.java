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
            public void updateMedia(Connection conn, Vector iceAttributes, Vector iceMedias) throws SdpParseException {
                log.log(Level.INFO, "{0}\n{1}\n{2}", new Object[]{conn, iceAttributes, iceMedias});
                source2.updateMedia(conn, (List) iceAttributes, (List) iceMedias);
            }

            @Override
            public void updateMedia(Connection conn, List<Attribute> iceAttributes, List<MediaDescription> iceMedias) throws SdpParseException {
                log.log(Level.INFO, "{0}\n{1}\n{2}", new Object[]{conn, iceAttributes, iceMedias});
                source2.updateMedia(conn, iceAttributes, iceMedias);
            }
        });
        source2.setSdpListener(new SDPListener() {

            @Override
            public void updateMedia(Connection conn, Vector iceAttributes, Vector iceMedias) throws SdpParseException {
                log.log(Level.INFO, "{0}\n{1}\n{2}", new Object[]{conn, iceAttributes, iceMedias});
                source1.updateMedia(conn, (List) iceAttributes, (List) iceMedias);
            }

            @Override
            public void updateMedia(Connection conn, List<Attribute> iceAttributes, List<MediaDescription> iceMedias) throws SdpParseException {
                log.log(Level.INFO, "{0}\n{1}\n{2}", new Object[]{conn, iceAttributes, iceMedias});
                source1.updateMedia(conn, iceAttributes, iceMedias);
            }
        });
    }
}
