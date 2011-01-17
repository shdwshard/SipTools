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
public interface MsrpRequest {

    public String getRequestName();

    public URI getToPath();

    public URI getFromPath();

    public long getMessageId();

    public String getTransactionId();

    public String getContentType();

    public byte[] getContent();

    public static final String HEADER_TO = "To-Path:";

    public static final String HEADER_FROM = "From-Path:";

    public static final String HEADER_MESSAGE_ID = "Message-ID:";

    public static final String HEADER_BYTE_RANGE = "Byte-Range:";

    public static final String HEADER_CONTENT_TYPE = "Content-Type:";

}
