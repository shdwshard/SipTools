/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.stun;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import net.mc_cubed.icedjava.packet.StunPacket;
import net.mc_cubed.icedjava.packet.header.MessageClass;
import net.mc_cubed.icedjava.packet.header.MessageMethod;
import net.mc_cubed.icedjava.stun.annotation.StunServer;
import net.mc_cubed.icedjava.stun.event.StunEventListener;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 *
 * @author shadow
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
        "stun.xten.com", //        "numb.viagenie.ca", // Getting timeouts with this server
    //        "stun.ipshka.com"   // Getting timeouts with this server
    };
    public static Integer STUN_PORT = 3478;

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
            DatagramStunSocket testSocket = getStunSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0), StunListenerType.CLIENT);

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
            DatagramStunSocket testSocket = getStunSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0), StunListenerType.CLIENT);

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

    public static DatagramStunSocket getStunSocket(InetSocketAddress address, final StunListenerType stunType) {
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(getNioDatagramChannelFactory());
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("DatagramStunSocket", new DatagramStunSocket());
                switch (stunType) {
                    case SERVER:
                    case BOTH:
                        pipeline.addFirst("StunRequestHandler", new GenericStunListener());
                        break;
                    case CLIENT:
                    default:
                        break;
                }
                pipeline.addFirst("StunPacketDecoder", new StunPacketDecoder());
                pipeline.addFirst("StunPacketEncoder", new StunPacketEncoder());
                return pipeline;
            }
        });
        DatagramChannel channel = (DatagramChannel)bootstrap.bind(address);

        return (DatagramStunSocket) channel.getPipeline().get("DatagramStunSocket");
    }

    public static DatagramStunSocket getStunSocket(int port,StunListenerType stunType) {
        return getStunSocket(new InetSocketAddress(port),stunType);
    }

    public static DatagramStunSocket getStunSocket(InetAddress address, int port,StunListenerType stunType) {
        return getStunSocket(new InetSocketAddress(address, port),stunType);
    }

    public static DatagramStunSocket getStunSocket(InetSocketAddress address, final ChannelHandler stunHandler) {
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(getNioDatagramChannelFactory());
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("DatagramStunSocket", new DatagramStunSocket());
                pipeline.addFirst("StunRequestHandler", stunHandler);
                pipeline.addFirst("StunPacketDecoder", new StunPacketDecoder());
                pipeline.addFirst("StunPacketEncoder", new StunPacketEncoder());
                return pipeline;
            }
        });
        DatagramChannel channel = (DatagramChannel)bootstrap.bind(address);
        return (DatagramStunSocket) channel.getPipeline().get("DatagramStunSocket");
    }

    public static DatagramStunSocket getStunSocket(int port,final ChannelHandler stunHandler) {
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(getNioDatagramChannelFactory());
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("DatagramStunSocket", new DatagramStunSocket());
                pipeline.addFirst("StunRequestHandler", stunHandler);
                pipeline.addFirst("StunPacketDecoder", new StunPacketDecoder());
                pipeline.addFirst("StunPacketEncoder", new StunPacketEncoder());
                return pipeline;
            }
        });
        DatagramChannel channel = (DatagramChannel)bootstrap.bind(new InetSocketAddress(port));
        return (DatagramStunSocket) channel.getPipeline().get("DatagramStunSocket");
    }
    
    public static DatagramChannel getCustomStunDatagramChannel(SocketAddress address, final ChannelHandler stunHandler) {
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(getNioDatagramChannelFactory());
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addFirst("StunRequestHandler", stunHandler);
                pipeline.addFirst("StunPacketDecoder", new StunPacketDecoder());
                pipeline.addFirst("StunPacketEncoder", new StunPacketEncoder());
                return pipeline;
            }
        });
        DatagramChannel channel = (DatagramChannel)bootstrap.bind(address);
        return channel;
        
    }

    public static DatagramStunSocket getStunSocket(InetAddress address, int port,ChannelHandler stunHandler) {
        return getStunSocket(new InetSocketAddress(address, port),stunHandler);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket(int port) {
        return getDemultiplexerSocket(new InetSocketAddress(port),null);
    }

    public static DatagramDemultiplexerSocket getDemultiplexerSocket() {
        return getDemultiplexerSocket((InetSocketAddress)null,null);
    }
    public static DatagramDemultiplexerSocket getDemultiplexerSocket(InetSocketAddress inetSocketAddress,final StunEventListener stunEventListener) {
        ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(getNioDatagramChannelFactory());
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("DatagramStunSocket", new DatagramDemultiplexerSocket(stunEventListener));
                pipeline.addFirst("StunRequestHandler", new GenericStunListener());
                pipeline.addFirst("StunPacketDecoder", new StunPacketDecoder());
                pipeline.addFirst("StunPacketEncoder", new StunPacketEncoder());
                return pipeline;
            }
        });
        DatagramChannel channel = (DatagramChannel)bootstrap.bind(inetSocketAddress);
        return (DatagramDemultiplexerSocket) channel.getPipeline().get("DatagramStunSocket");
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
    protected static NioServerSocketChannelFactory socketChannelFactory = null;

    protected static NioDatagramChannelFactory datagramChannelFactory = null;

    @Produces
    public static NioServerSocketChannelFactory getNioServerSocketChannelFactory() {
        if (socketChannelFactory == null) {
            socketChannelFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());
        }
        return socketChannelFactory;
    }

    @Produces
    public static NioDatagramChannelFactory getNioDatagramChannelFactory() {
        if (datagramChannelFactory == null) {
            datagramChannelFactory = new NioDatagramChannelFactory(
                    Executors.newCachedThreadPool());
        }
        return datagramChannelFactory;
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
        StunPacket replyPacket = new StunPacketImpl(messageClass,packet.getMethod(),packet.getTransactionId());
        return replyPacket;
    }

    public static StunPacket createStunRequest(MessageClass messageClass, MessageMethod messageMethod) {
        return new StunPacketImpl(messageClass,messageMethod);
    }

}
