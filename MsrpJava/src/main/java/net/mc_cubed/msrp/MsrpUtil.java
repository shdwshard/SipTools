package net.mc_cubed.msrp;

import org.apache.commons.codec.binary.Base64;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

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
public class MsrpUtil {

    /**
     * RFC 4975 Section 15.4
     * <p>
     * MSRP uses TCP port 2855, from the "registered" port range. Usage of this
     * value is described in section 6.
     */
    public static final int MSRP_DEFAULT_PORT = 2855;
    /**
     * RFC 4975 Section 15.5
     * <p>
     * This document requests permanent registration of the URI schemes of
     * "msrp" and "msrps".
     */
    public static final String MSRP_URI_SCHEMA = "msrp";
    /**
     * RFC 4975 Section 15.5
     * <p>
     * This document requests permanent registration of the URI schemes of
     * "msrp" and "msrps".
     */
    public static final String MSRP_SSL_URI_SCHEMA = "msrps";

    /**
     * Number of bytes to use when generating a session-id
     * <p>
     * RFC 4975 Session 14.1
     * <p>
     * when an MSRP device generates an MSRP URI to be used in the initiation of
     * an MSRP session, the session-id component MUST contain at least 80 bits
     * of randomness
     */
    public static final int MSRP_URI_SESSION_LENGTH = 16;

    public static final String MSRP_FRAGMENT = ";tcp";

    /**
     * Generate a RFC 4975 Section 14.1 compliant SessionId for use in an MSRP
     * URI
     * @return
     */
    public static String generateMsrpUriSessionId() {
        SecureRandom secure = new SecureRandom();
        byte[] bytes = new byte[MSRP_URI_SESSION_LENGTH];
        secure.nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }
    /**
     * Number of bytes to use when generating a transaction identifier
     * <p>
     * To form a new request, the sender creates a transaction identifier and
     * uses this and the method name to create an MSRP request start line. The
     * transaction identifier MUST NOT collide with that of other transactions
     * that exist at the same time. Therefore, it MUST contain at least 64 bits
     * of randomness.
     */
    public static final int MSRP_TX_ID_LENGTH = 12;

    /**
     * Generate a RFC 4975 Section 7.1 compliant transaction identifier
     * <p>
     * To form a new request, the sender creates a transaction identifier and
     * uses this and the method name to create an MSRP request start line. The
     * transaction identifier MUST NOT collide with that of other transactions
     * that exist at the same time. Therefore, it MUST contain at least 64 bits
     * of randomness.
     */
    public static String generateMsrpTransactionId() {
        SecureRandom secure = new SecureRandom();
        byte[] bytes = new byte[MSRP_TX_ID_LENGTH];
        secure.nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }

    public static final String MSRP_SDP_MEDIA_TYPE = "message";

    public static final String MSRP_SDP_ACCEPT_TYPES_ATTRIBUTE = "accept-types";

    public static final String MSRP_SDP_PATH_ATTRIBUTE = "path";

    public static final List<String> MSRP_SDP_PROTOCOLS;

    public static final String[] IMPL_SUPPORTED_TYPES = new String[] { "text/plain" };

    static {
        MSRP_SDP_PROTOCOLS = new LinkedList<String>();
        MSRP_SDP_PROTOCOLS.add("TCP/MSRP");
        MSRP_SDP_PROTOCOLS.add("TCP/TLS/MSRP");
    }

    public static final String MSRP_MAGIC = "MSRP";

}
