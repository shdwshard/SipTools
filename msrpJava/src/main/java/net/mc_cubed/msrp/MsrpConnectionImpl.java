package net.mc_cubed.msrp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2010 Charles Chappell.
 *
 * This file is part of MsrpJava.
 *
 * MsrpJava is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * MsrpJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MsrpJava.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Charles Chappell <shdwshard@me.com>
 * @version 2010.1112
 * @since 1.0
 */
class MsrpConnectionImpl implements MsrpConnection {

    final URI connectionUri;
    final String sessionId;

    final Socket sock;

    Date lastTouch;

    static final Logger log = Logger.getLogger(MsrpConnectionImpl.class.getName());

    public MsrpConnectionImpl(URI connectionUri,String sessionId) throws IOException {
        this.connectionUri = connectionUri;
        this.sessionId = sessionId;
        log.log(Level.INFO,"Connecting to {0}:{1}", new Object[] {connectionUri.getHost(),connectionUri.getPort()});
        sock = new Socket(connectionUri.getHost(),connectionUri.getPort());
    }

    @Override
    public URI getConnectionUri() {
        return connectionUri;
    }

    @Override
    public Date getLastTouch() {
        return lastTouch;
    }

    @Override
    public Socket getSock() {
        return sock;
    }

    @Override
    public void setLastTouch(Date lastTouch) {
        this.lastTouch = lastTouch;
    }

    @Override
    public boolean sendMessage(MsrpRequest message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);

        // Write the first line of the MSRP message
        dataOut.writeBytes(MsrpUtil.MSRP_MAGIC);
        dataOut.writeByte(0x20);

        dataOut.writeBytes(message.getTransactionId());
        dataOut.writeByte(0x20);

        dataOut.writeBytes(message.getRequestName());
        dataOut.writeBytes("\n");

        /**
         *  Write out the headers
         */
        dataOut.writeBytes(MsrpRequest.HEADER_TO);
        dataOut.writeByte(0x20);
        dataOut.writeBytes(message.getToPath().toString());
        dataOut.writeBytes("\n");
        
        dataOut.writeBytes(MsrpRequest.HEADER_FROM);
        dataOut.writeByte(0x20);
        dataOut.writeBytes(message.getFromPath().toString());
        dataOut.writeBytes("\n");

        dataOut.writeBytes(MsrpRequest.HEADER_MESSAGE_ID);
        dataOut.writeByte(0x20);
        dataOut.writeBytes(Long.toString(message.getMessageId()));
        dataOut.writeBytes("\n");

        // TODO: Chunking headers here

        dataOut.writeBytes(MsrpRequest.HEADER_CONTENT_TYPE);
        dataOut.writeByte(0x20);
        dataOut.writeBytes(message.getContentType());
        dataOut.writeBytes("\n");

        dataOut.writeBytes("\n");
        dataOut.write(message.getContent());
        dataOut.writeBytes("------".concat(message.getTransactionId()).concat("$"));
        dataOut.writeBytes("\n");
        dataOut.close();

        baos.writeTo(sock.getOutputStream());

        return true;
    }

    protected boolean secure = false;

    /**
     * Get the value of secure
     *
     * @return the value of secure
     */
    public boolean isSecure() {
        return secure;
    }

    public String getSessionId() {
        return sessionId;
    }

    protected URI getLocalURI() {
        try {
            return new URI((isSecure()) ? MsrpUtil.MSRP_SSL_URI_SCHEMA : MsrpUtil.MSRP_URI_SCHEMA, null, sock.getLocalAddress().getCanonicalHostName(), sock.getPort(), "/" + getSessionId(), null, MsrpUtil.MSRP_FRAGMENT);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MsrpConnectionImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
