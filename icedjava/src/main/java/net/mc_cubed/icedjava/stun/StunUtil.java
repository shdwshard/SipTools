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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import net.mc_cubed.icedjava.stun.StreamDemultiplexerSocket.ConnectionFactory;
import net.mc_cubed.icedjava.stun.annotation.StunServer;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.Transport.State;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
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
            StunSocket testSocket = getStunSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0), StunSocketType.CLIENT);

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

    public static StunSocket getStunSocket(InetSocketAddress address, final StunSocketType stunType) throws IOException {
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

    public static StunSocket getStunSocket(int port, StunSocketType stunType) throws IOException {
        return getStunSocket(new InetSocketAddress(port), stunType);
    }

    public static StunSocket getStunSocket(InetAddress address, int port, StunSocketType stunType) throws IOException {
        return getStunSocket(new InetSocketAddress(address, port), stunType);
    }

    public static DemultiplexerSocket getCustomStunPipeline(InetSocketAddress address, TransportType transportType, boolean active, final Filter... stunFilters) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible for reading and writing 
        //  data to the connection
        filterChainBuilder.add(new TransportFilter());

        // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
        filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

        if (transportType == transportType.TCP) {
            // If we're a TCP socket, we MUST support RFC 4571 framing!
            filterChainBuilder.add(new RFC4571FramingFilter());
        }

        // Add the STUN packet decoder
        filterChainBuilder.add(new StunPacketProtocolFilter());

        // Add the custom filters
        for (Filter stunFilter : stunFilters) {
            filterChainBuilder.add(stunFilter);
        }

        DemultiplexerSocket socket;

        if (transportType == transportType.UDP) {
            // Finally, add the stunSocket class to the top of the chain
            socket = new DatagramDemultiplexerSocket(null);
            filterChainBuilder.add((Filter) socket);

            // Get the underlying datagram transport
            UDPNIOTransport transport = getDatagramTransport();

            // Bing the socket to the supplied address
            UDPNIOServerConnection connection = transport.bind(address);

            // Add the filter chain
            connection.setProcessor(filterChainBuilder.build());

            // Set the server connection
            ((DatagramStunSocket) socket).setServerConnection(connection);
        } else {
            if (active) {
                ConnectionFactory factory = new ConnectionFactory() {

                    @Override
                    public Connection connect(InetSocketAddress address, Filter socket) {
                        try {
                            // Create a FilterChain using FilterChainBuilder
                            FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

                            // Add TransportFilter, which is responsible for reading and writing 
                            //  data to the connection
                            filterChainBuilder.add(new TransportFilter());

                            // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
                            filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

                            // If we're a TCP socket, we MUST support RFC 4571 framing!
                            filterChainBuilder.add(new RFC4571FramingFilter());

                            // Add the STUN packet decoder
                            filterChainBuilder.add(new StunPacketProtocolFilter());

                            // Add the custom filters
                            for (Filter stunFilter : stunFilters) {
                                filterChainBuilder.add(stunFilter);
                            }
                            filterChainBuilder.add((Filter) socket);

                            // Get the underlying stream transport
                            TCPNIOTransport transport = getServerSocketChannelFactory();

                            // Bind the socket to the supplied address
                            Connection connection = transport.connect(address).get();

                            // Add the filter chain
                            connection.setProcessor(filterChainBuilder.build());

                            return connection;
                        } catch (InterruptedException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return null;
                    }
                };

                socket = new StreamDemultiplexerSocket(null, factory);

            } else {
                socket = new StreamDemultiplexerServerSocket(null);

                filterChainBuilder.add((Filter) socket);

                // Get the underlying stream transport
                TCPNIOTransport transport = getServerSocketChannelFactory();

                // Bind the socket to the supplied address
                TCPNIOServerConnection connection = transport.bind(address);

                // Add the filter chain
                connection.setProcessor(filterChainBuilder.build());

                // Set the server connection
                ((StreamDemultiplexerSocket) socket).setServerConnection(connection);
            }

        }

        return socket;
    }

    public static DemultiplexerSocket getCustomStunPipeline(int port, final Filter... stunFilters) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(port), TransportType.UDP, false, stunFilters);
    }

    public static DemultiplexerSocket getCustomStunPipeline(InetAddress address, int port, final Filter... stunFilters) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(address, port), TransportType.UDP, false, stunFilters);
    }

    public static DemultiplexerSocket getCustomStunPipeline(final Filter... stunFilters) throws IOException {
        return getCustomStunPipeline(new InetSocketAddress(0), TransportType.UDP, false, stunFilters);
    }

    public static DemultiplexerSocket getDemultiplexerSocket(InetAddress address, int port) throws IOException {
        return getDemultiplexerSocket(new InetSocketAddress(address, port), TransportType.UDP, false, null);
    }

    public static DemultiplexerSocket getDemultiplexerSocket(int port) throws IOException {
        return getDemultiplexerSocket(new InetSocketAddress(port), TransportType.UDP, false, null);
    }

    public static DemultiplexerSocket getDemultiplexerSocket() throws IOException {
        return getDemultiplexerSocket((InetSocketAddress) null, TransportType.UDP, false, null);
    }

    public static DemultiplexerSocket getDemultiplexerSocket(InetSocketAddress inetSocketAddress, TransportType transportType, boolean active, final StunEventListener stunEventListener) throws IOException {
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible for reading and writing 
        //  data to the connection
        filterChainBuilder.add(new TransportFilter());

        // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
        filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

        if (transportType == transportType.TCP) {
            // If we're a TCP socket, we MUST support RFC 4571 framing!
            filterChainBuilder.add(new RFC4571FramingFilter());
        }

        // Add the packet encoding/decoding filter which does the format
        //  translation for STUN packets
        filterChainBuilder.add(new StunPacketProtocolFilter());

        // This socket should respond to STUN packets, so add the default stun
        //  handler
        filterChainBuilder.add(new DefaultStunServerHandler());

        DemultiplexerSocket socket;

        if (transportType == transportType.UDP) {
            // Finally, add the stunSocket class to the top of the chain
            socket = new DatagramDemultiplexerSocket(null);
            filterChainBuilder.add((Filter) socket);

            // Get the underlying datagram transport
            UDPNIOTransport transport = getDatagramTransport();

            // Bing the socket to the supplied address
            UDPNIOServerConnection connection = transport.bind(inetSocketAddress);

            // Add the filter chain
            connection.setProcessor(filterChainBuilder.build());

            // Set the server connection
            ((DatagramStunSocket) socket).setServerConnection(connection);
        } else {
            if (active) {
                ConnectionFactory factory = new ConnectionFactory() {

                    @Override
                    public Connection connect(InetSocketAddress address, Filter socket) {
                        try {
                            // Create a FilterChain using FilterChainBuilder
                            FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

                            // Add TransportFilter, which is responsible for reading and writing 
                            //  data to the connection
                            filterChainBuilder.add(new TransportFilter());

                            // Add the transcoding filter to go from Grizzly Buffers to NIO buffers
                            filterChainBuilder.add(new ByteBufferGrizzlyProtocolFilter());

                            // If we're a TCP socket, we MUST support RFC 4571 framing!
                            filterChainBuilder.add(new RFC4571FramingFilter());

                            // Add the STUN packet decoder
                            filterChainBuilder.add(new StunPacketProtocolFilter());

                            // This socket should respond to STUN packets, so add the default stun
                            //  handler
                            filterChainBuilder.add(new DefaultStunServerHandler());

                            filterChainBuilder.add((Filter) socket);

                            // Get the underlying stream transport
                            TCPNIOTransport transport = getServerSocketChannelFactory();

                            // Bind the socket to the supplied address
                            Connection connection = transport.connect(address).get();

                            // Add the filter chain
                            connection.setProcessor(filterChainBuilder.build());

                            return connection;
                        } catch (InterruptedException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return null;
                    }
                };

                socket = new StreamDemultiplexerSocket(null, factory);
            } else {
                socket = new StreamDemultiplexerServerSocket(null);

                filterChainBuilder.add((Filter) socket);

                // Get the underlying stream transport
                TCPNIOTransport transport = getServerSocketChannelFactory();

                // Bind the socket to the supplied address
                TCPNIOServerConnection connection = transport.bind(inetSocketAddress);

                // Add the filter chain
                connection.setProcessor(filterChainBuilder.build());

                // Set the server connection
                ((StreamDemultiplexerSocket) socket).setServerConnection(connection);
            }
        }
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
                if (!transport.isStopped()) {
                    transport.stop();
                }
            } catch (IOException ex) {
                Logger.getLogger(StunUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
