/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.msrp;

import java.net.URI;

/**
 *
 * @author charles
 */
public class MsrpMessage implements MsrpRequest {

    public final static String MESSAGE_REQUEST_NAME = "SEND";

    public MsrpMessage(URI fromPath, URI toPath, String contentType, String content, long messageId ) {
        this.contentType = contentType;
        this.content = content;
        this.toPath = toPath;
        this.fromPath = fromPath;
        this.messageId = messageId;
        this.txid = MsrpUtil.generateMsrpTransactionId();
    }

    protected final long messageId;
    
    protected final String txid;

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public String getTransactionId() {
        return txid;
    }
    
    protected String contentType;

    /**
     * Get the value of contentType
     *
     * @return the value of contentType
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Set the value of contentType
     *
     * @param contentType new value of contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected String content;

    /**
     * Get the value of content
     *
     * @return the value of content
     */
    @Override
    public byte[] getContent() {
        return content.getBytes();
    }

    /**
     * Set the value of content
     *
     * @param content new value of content
     */
    public void setContent(String content) {
        this.content = content;
    }

    protected URI toPath;

    /**
     * Get the value of toPath
     *
     * @return the value of toPath
     */
    @Override
    public URI getToPath() {
        return toPath;
    }

    /**
     * Set the value of toPath
     *
     * @param toPath new value of toPath
     */
    public void setToPath(URI toPath) {
        this.toPath = toPath;
    }

    protected URI fromPath;

    /**
     * Get the value of fromPath
     *
     * @return the value of fromPath
     */
    @Override
    public URI getFromPath() {
        return fromPath;
    }

    /**
     * Set the value of fromPath
     *
     * @param fromPath new value of fromPath
     */
    public void setFromPath(URI fromPath) {
        this.fromPath = fromPath;
    }

    @Override
    public String getRequestName() {
        return MESSAGE_REQUEST_NAME;
    }

    public String getStringContent() {
        return content;
    }

}
