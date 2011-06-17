/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.stun;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import net.mc_cubed.icedjava.stun.annotation.StunServer;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.Transport.State;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOServerConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * StunUtil is the entry point into the IcedJava library STUN components.
 * 
 * @author Charles Chappell
 * @since 0.9
 */
@Named
public class StunUtil {

    protected static String[] serverList = {
        //        "stun.fwdnet.net",    // Getting timeouts with this server
        "stun.ideasip.com",
        //"stun01.sipphone.com", // Getting timeouts with this server
        "stun.softjoys.com",
        //"stun.voipbuster.com", // Getting timeouts with this server
        "stun.voxgratia.org",
        "stun.xten.com",
        "stun1.noc.ams-ix.net"
        //"numb.viagenie.ca", // Getting timeouts with this server
        //"stun.ipshka.com" // Getting timeouts with this server
    };
    public static Integer STUN_PORT = 3478;
    // This really should be adjusted to match some established standard
    public static Integer MAX_PACKET_SIZE = 4096;

    /**
     * Get a valid Stun Server name to use for STUN processing.
     * 
     * @return a name which can be used by getStunServerByName() to find the
     * address of a valid stun server.
     * @deprecated Use getStunServerSocket() instead, which gets and tests the
     * stun server, and returns a tested server socket.
     */
    @Produces
    @StunServer
    @Deprecated
    public static String getStunServer() {
        try {
            List<String> servers = new LinkedList<String>();

            servers.addAll(Arrays.asList(serverList));

            Collections.shuffle(servers);
            DatagramStunSocket testSocket = getStunSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0), StunSocketType.CLIENT);

            for (String server : servers) {
                try {
                    for (InetSocketAddress address : getStunServerByName(server)) {
                        StunReply reply = testSocket.doTest(address).get();
                        if (reply != null && reply.isSuccess()) {
                            return server;
                        }
                    }
                    // Don't interrupt the whole process if one server fails.
                } catch (Exception ex) {
                    Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Failed
        return null;
    }

    /**
     * Gets the InetSocketAddress of a valid STUN server picked from the built-in
     * list of stun servers.
     *
     * @return the address of a valid and tested STUN server, or NULL if none
     * could be tested successfully.
     */
    @Produces
    @StunServer
    public static InetSocketAddress getStunServerSocket() {
        try {
            List<String> servers = new LinkedList<String>();

            servers.addAll(Arrays.asList(serverList));

            Collections.shuffle(servers);
            DatagramStunSocket testSocket = getStunSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0), StunSocketType.CLIENT);

            for (String server : servers) {
                try {
                    for (InetSocketAddress address : getStunServerByName(server)) {
                        StunReply reply = testSocket.doTest(address).get();
                        if (reply != null && reply.isSuccess()) {
                            return address;
                        }
                    }
                    // Don't interrupt the whole process if one server fails.
                } catch (Exception ex) {
                    Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (Exception ex) {
            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Failed
        return null;
    }

    public static DatagramStunSocket getStunSocket(InetSocketAddress address, final StunSocketType stunType) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible for reading and writing 
        //  data to the connection
        filterChainBuilder.add(new TransportFilter());

        // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
        filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

        // Add the packet encoding/decoding filter which does the format
        //  translation for STUN packets
        filterChainBuilder.add(new StunPacketProtocolFilter());

        switch (stunType) {
            case SERVER:
            case BOTH:
                // If this socket should respond to STUN packets, add the
                //  default stun handler
                filterChainBuilder.add(new DefaultStunServerHandler());
                break;
            case CLIENT:
            default:
                break;
        }

        // Finally, add the stunSocket class to the top of the chain
        DatagramStunSocket socket = new DatagramStunSocket();
        filterChainBuilder.add(socket);

        // Get the underlying datagram transport
        UDPNIOTransport transport = getDatagramTransport();

        UDPNIOServerConnection connection = transport.bind(address);

        connection.setProcessor(filterChainBuilder.build());

        socket.setServerConnection(connection);

        return socket;
    }

    public static DatagramStunSocket getStunSocket(int port, StunSocketType stunType) throws IOException {
        return getStunSocket(new InetSocketAddress(port), stunType);
    }

    public static DatagramStunSocket getStunSocket(InetAddress address, int port, StunSocketType stunType) throws IOException {
        return getStunSocket(new InetSocketAddress(address, port), stunType);
    }

    public static DatagramDemultiplexerSocket getCustomStunPipeline(InetSocketAddress address, final Filter stunFilter) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible for reading and writing 
        //  data to the connection
        filterChainBuilder.add(new TransportFilter());

        // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
        filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

        // Add the packet encoding/decoding filter which does the format
        //  translation for STUN packets
        filterChainBuilder.add(new StunPacketProtocolFilter());

        // Add the custom stun filter.  This filter may also, optionally handle
        //  data packets
        filterChainBuilder.add(stunFilter);

        // Finally, add the stunSocket class to the top of the chain
        DatagramDemultiplexerSocket socket = new DatagramDemultiplexerSocket(null);
        filterChainBuilder.add(socket);

        // Get the underlying datagram transport
        UDPNIOTransport transport = getDatagramTransport();


        UDPNIOServerConnection connection = transport.bind(address);

        // Add the filter chain
        connection.setProcessor(filterChainBuilder.build());

        socket.setServerConnection(connection);

        return socket;
    }

    public static DatagramDemultiplexerSocket getCustomStunPipeline(int port, final Filter stunFilter) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(port), stunFilter);
    }

    public static DatagramDemultiplexerSocket getCustomStunPipeline(InetAddress address, int port, final Filter stunFilter) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(address, port), stunFilter);
    }

    public static DatagramDemultiplexerSocket getCustomStunPipeline(final Filter stunFilter) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(0), stunFilter);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket(InetAddress address, int port) throws IOException {
        return getDemultiplexerSocket(new InetSocketAddress(address, port), null);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket(int port) throws IOException {
        return getDemultiplexerSocket(new InetSocketAddress(port), null);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket() throws IOException {
        return getDemultiplexerSocket((InetSocketAddress) null, null);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket(InetSocketAddress inetSocketAddress, final StunEventListener stunEventListener) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible for reading and writing 
        //  data to the connection
        filterChainBuilder.add(new TransportFilter());

        // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
        filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

        // Add the packet encoding/decoding filter which does the format
        //  translation for STUN packets
        filterChainBuilder.add(new StunPacketProtocolFilter());

        // This socket should respond to STUN packets, so add the default stun
        //  handler
        filterChainBuilder.add(new DefaultStunServerHandler());

        // Finally, add the stunSocket class to the top of the chain
        DatagramDemultiplexerSocket socket = new DatagramDemultiplexerSocket(null);
        filterChainBuilder.add(socket);

        // Get the underlying datagram transport
        UDPNIOTransport transport = getDatagramTransport();

        UDPNIOServerConnection connection = transport.bind(inetSocketAddress);

        // Add the filter chain
        connection.setProcessor(filterChainBuilder.build());

        socket.setServerConnection(connection);

        return socket;
    }

    public static InetSocketAddress[] getStunServerByName(String address) {
        List<InetSocketAddress> retval = new LinkedList<InetSocketAddress>();

        try {
            Lookup lookup = new Lookup("_stun._udp." + address, Type.SRV);
            SRVRecord[] records = (SRVRecord[]) lookup.run();
            if (records != null) {
                for (SRVRecord record : records) {
                    try {
                        retval.add(new InetSocketAddress(InetAddress.getByName(record.rdataToString()), record.getPort()));
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (retval.size() > 0) {
                return retval.toArray(new InetSocketAddress[0]);
            }
        } catch (TextParseException ex) {
            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int lookupType : new int[]{Type.AAAA, Type.A}) {
            try {
                Lookup lookup = new Lookup(address, lookupType);
                Record[] records = (Record[]) lookup.run();
                if (records != null) {
                    for (Record record : records) {
                        if (record instanceof ARecord) {
                            ARecord aRecord = (ARecord) record;
                            retval.add(new InetSocketAddress(aRecord.getAddress(), STUN_PORT));
                        } else if (record instanceof AAAARecord) {
                            AAAARecord aaaaRecord = (AAAARecord) record;
                            retval.add(new InetSocketAddress(aaaaRecord.getAddress(), STUN_PORT));
                        }
                    }
                }
            } catch (TextParseException ex) {
                Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retval.toArray(new InetSocketAddress[0]);

    }
    protected static TCPNIOTransport streamTransport = null;
    protected static UDPNIOTransport datagramTransport = null;

    @Produces
    public static TCPNIOTransport getServerSocketChannelFactory() {
        //return TCPNIOTransportBuilder.newInstance().build();
        if (streamTransport == null) {
            streamTransport = TCPNIOTransportBuilder.newInstance().build();
            try {
                streamTransport.start();
            } catch (IOException ex) {
                Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new TransportShutdownRunnable(streamTransport)));

        }
        return streamTransport;
    }

    @Produces
    public static UDPNIOTransport getDatagramTransport() {
        //return UDPNIOTransportBuilder.newInstance().build();
        if (datagramTransport == null) {
            datagramTransport = UDPNIOTransportBuilder.newInstance().build();
            try {
                datagramTransport.start();
            } catch (IOException ex) {
                Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new TransportShutdownRunnable(datagramTransport)));
        }
        return datagramTransport;
    }
    protected static InetSocketAddress cachedStunServerSocket = null;

    public static InetSocketAddress getCachedStunServerSocket() {
        if (cachedStunServerSocket == null) {
            cachedStunServerSocket = getStunServerSocket();
        }

        return cachedStunServerSocket;
    }

    public static void clearCachedStunServer() {
        cachedStunServerSocket = null;
    }

    public static StunPacket createReplyPacket(StunPacket packet, MessageClass messageClass) {
        StunPacket replyPacket = new StunPacketImpl(messageClass, packet.getMethod(), packet.getTransactionId());
        return replyPacket;
    }

    public static StunPacket createStunRequest(MessageClass messageClass, MessageMethod messageMethod) {
        return new StunPacketImpl(messageClass, messageMethod);
    }

    static class TransportShutdownRunnable implements Runnable {

        Transport transport;

        TransportShutdownRunnable(Transport transport) {
            this.transport = transport;
        }

        @Override
        public void run() {
            try {
                if (transport.getState().getState() == State.START) {
                    transport.stop();
                }
            } catch (IOException ex) {
                Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
