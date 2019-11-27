import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;


public class UDPClient {

    //private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);

    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            String msg = "Hello World";
            //find the partition number and distribute the packet in a seprate method
            //int partiNo = getThePartiNo(msg);
            //for(int i =1; i <=partiNo; i++){
            System.out.println("The length is:" +msg.length());
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(1L)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);

            //logger.info("Sending \"{}\" to router at {}", msg, routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            //logger.info("Waiting for the response");
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
                //logger.error("No response after timeout");
                return;
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            //logger.info("Packet: {}", resp);
            //logger.info("Router: {}", router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            System.out.println(payload);
            //logger.info("Payload: {}",  payload);

            keys.clear();
        }
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("router-host", "Router hostname")
                .withOptionalArg()
                .defaultsTo("127.0.0.1");

        parser.accepts("router-port", "Router port number")
                .withOptionalArg()
                .defaultsTo("3000");

        parser.accepts("server-host", "EchoServer hostname")
                .withOptionalArg()
                .defaultsTo("127.0.0.1");

        parser.accepts("server-port", "EchoServer listening port")
                .withOptionalArg()
                .defaultsTo("8007");

        OptionSet opts = parser.parse(args);

        // Router address
        String routerHost = (String) opts.valueOf("router-host");
        int routerPort = Integer.parseInt((String) opts.valueOf("router-port"));

        // Server address
        String serverHost = (String) opts.valueOf("server-host");
        int serverPort = Integer.parseInt((String) opts.valueOf("server-port"));

        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
        handshakeCheck(routerAddress, serverAddress);
        runClient(routerAddress, serverAddress);
    }

    private static void handshakeCheck(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            String msg = "Hello World";
            //find the partition number and distribute the packet in a separate method
            //int partiNo = getThePartiNo(msg);
            //for(int i =1; i <=partiNo; i++){
            System.out.println("The length is:" +msg.length());
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(1L)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);

            //logger.info("Sending \"{}\" to router at {}", msg, routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            //logger.info("Waiting for the response");
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
                //logger.error("No response after timeout");
                return;
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            //logger.info("Packet: {}", resp);
            //logger.info("Router: {}", router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            System.out.println(payload);
            //logger.info("Payload: {}",  payload);

            keys.clear();
        }
    }
    public static int getThePartiNo(String message){
        int tempValHolder;
        int remainder;
        if(message.length() <= 1024){
            return 1;
        }
        else{
            tempValHolder = message.length()/1024;
            remainder = message.length()%1024;
            if(remainder != 0){
                tempValHolder += 1;
            }
            return tempValHolder;
        }

    }
}

