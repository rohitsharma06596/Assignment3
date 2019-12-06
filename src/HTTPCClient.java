/**
 *
 * HTTPCClient.java
 *
 * This implementation class is an abstraction of a HTTPC client request. HTTPC is the
 * our new web request generation protocal modelled using standard HTTP with customized
 * selection of its features. It is made interactive by a Command Line application
 *
 * @author Rohit Sharma, Aayush Lamichhane
 * @since 09-11-2019
 */

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_READ;

public class HTTPCClient extends Thread{

    /**
     * Member variable to hold the parsed Data from the Command Line
     */
    private static HashMap<String, String> parsedData;
    private SocketAddress routerAddr;
    private InetSocketAddress serverAddr;
    private String serverKey;
    private long SeqNumber = 1L;
    HashMap<Long, Packet> sentMessageTracker;
    Date trackerFlushtimer = new Date();
    Long ackCounter = 0L;

    /**
     * Constructor to initialize the parsedData member variable
     *
     * @param parm
     */
    HTTPCClient(HashMap<String, String> parm, String[] args, String ipPort) throws IOException {
        sentMessageTracker = new HashMap<Long, Packet>();
        parsedData = parm;
        OptionParser parser = new OptionParser();
        parser.accepts("router-host", "Router hostname")
                .withOptionalArg()
                .defaultsTo("127.0.0.1");

        parser.accepts("router-port", "Router port number")
                .withOptionalArg()
                .defaultsTo("3000");
        System.out.println("The edge router of the network is 127.0.0.1 at port 3000");

        parser.accepts("server-host", "EchoServer hostname")
                .withOptionalArg()
                .defaultsTo(ipPort.substring(0, ipPort.indexOf(":")));

        parser.accepts("server-port", "EchoServer listening port")
                .withOptionalArg()
                .defaultsTo(ipPort.substring(ipPort.indexOf(":") + 1));
        System.out.println("The server you are trying to connect is " + ipPort.substring(0, ipPort.indexOf(":")) + " at port " +
                ipPort.substring(ipPort.indexOf(":") + 1));

        OptionSet opts = parser.parse(args);

        // Router address
        String routerHost = (String) opts.valueOf("router-host");
        int routerPort = Integer.parseInt((String) opts.valueOf("router-port"));

        // Server address
        String serverHost = (String) opts.valueOf("server-host");
        int serverPort = Integer.parseInt((String) opts.valueOf("server-port"));

        routerAddr = new InetSocketAddress(routerHost, routerPort);
        serverAddr = new InetSocketAddress(serverHost, serverPort);

        Thread t  = new Thread(this);
        t.start();

    }

    public void run(){
        System.out.println("The syncronizer is running.");
        Date timeNow = new Date();
        while(true){
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!sentMessageTracker.isEmpty()){
                for(long i = ackCounter+1; i <SeqNumber-1; i++){
                    try(DatagramChannel channel = DatagramChannel.open()) {
                        if(sentMessageTracker.containsKey(i)){
                            System.out.println("The ackCounter is "+ackCounter);
                            System.out.println("The packet with sequense number "+i+" was lost");
                            System.out.println("Resending the packet");
                            sendPacket(sentMessageTracker.get(i), channel);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
    public HashMap<String, String> getParsedData() {
        return parsedData;
    }

    public void setParsedData(HashMap<String, String> parsedData) {
        HTTPCClient.parsedData = parsedData;
    }

    public boolean createConnection() throws IOException {
        System.out.println("Attempting three way handshake...");
        boolean counter = handshakeCheck();
        while (!counter) {
            counter = handshakeCheck();
        }
        System.out.println("Three way handshake has been successfully completed");
        return true;
    }

    private Packet createPacket(long seqNumber, String msg) throws IOException {
        if (seqNumber != 0L) {
            if (SeqNumber >= 15L) {
                SeqNumber = 1L;
                sentMessageTracker.clear();
                seqNumber = SeqNumber;
            } else {
                SeqNumber += 1L;
                seqNumber = SeqNumber;
            }
        }
        Packet p;
        int pckType;
        if (msg.contains("SYN") || msg.contains("ACK") ||
                msg.contains("SYN-ACK")) {
            pckType = 0;
            seqNumber = 0L;
        } else {
            pckType = 1;
        }
        System.out.println("The length is:" + msg.length());
        p = new Packet.Builder()
                .setType(pckType)
                .setSequenceNumber(seqNumber)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload(msg.getBytes())
                .create();

        return p;
    }

    private Packet receivePacket(DatagramChannel channel) throws IOException {

        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        System.out.println("Waiting for the response");
        selector.select(1000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            //logger.error("No response after timeout");
            return null;
        }

        // We just want a single response.
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        SocketAddress router = channel.receive(buf);
        buf.flip();
        Packet resp = Packet.fromBuffer(buf);
        String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
        System.out.println(payload);
        if (resp.getType() == 0) {
            if (payload.contains("ACK")) {
                try {
                    long ackNo = Long.parseLong(payload.substring(payload.indexOf(":") + 1, payload.indexOf(" ")));
                    if (ackNo == SeqNumber) {
                        ackCounter = ackNo;
                    }
                    else{
                        ackCounter = ackCounter+1;
                    }
                } catch (Exception e) {
                    return resp;
                }
                //important in post write it then
            }
        }

        return resp;
    }

    private void sendPacket(Packet p, DatagramChannel channel) throws IOException {

        channel.send(p.toBuffer(), routerAddr);

    }

    private boolean handshakeCheck() throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            Packet p = createPacket(0L, "SYN");
            sendPacket(p, channel);
            System.out.println("Initiating the three way handshake from the client");
            Packet recev = receivePacket(channel);
            String payload;
            if (recev != null) {
                System.out.println("Received the acknowledgement response from the server");
                payload = new String(recev.getPayload(), StandardCharsets.UTF_8);
                System.out.println(payload.substring(0, payload.indexOf(":")));
                serverKey = payload.substring(payload.indexOf(":") + 1);
                System.out.println(serverKey);
            } else {
                while (recev == null) {
                    sendPacket(p, channel);
                    recev = receivePacket(channel);
                }
                payload = new String(recev.getPayload(), StandardCharsets.UTF_8);
            }
            if (payload.contains("ACK")) {
                System.out.println("Sending the final confirmation for the three way handshake");
                p = createPacket(0L, "SYN-ACK:" + serverKey);
                sendPacket(p, channel);
            } else {
                handshakeCheck();
            }
        }
        return true;
    }


    /**
     * This method uses the parsed data to create a HTTP get request to the
     * requested server implementing the appropriate headers and parameters
     *
     * @throws IOException
     */
    public static void GETRequest() throws IOException {

        /**Creating connection and request for httpc*/

        URL urlForGetRequest = new URL(parsedData.get("URL"));
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        boolean redirect = false;

        /**Setting request header*/
        if (parsedData.containsKey("Header")) {
            String head = parsedData.get("Header");
            while (!head.isEmpty()) {
                String parm1 = head.substring(1, head.indexOf(":"));
                head = head.substring(head.indexOf(":") + 1);
                String parm2 = head.substring(0, head.indexOf("\""));
                head = head.substring(head.indexOf(";"));
                connection.setRequestProperty("parm1", "parm2");
                if ((head.length() == 1) && head.startsWith(";")) {
                    head = "";
                } else {
                    head = head.substring(1);
                }
            }
        }

        /**Configuring time outs (Extra property)*/

        connection.setConnectTimeout(5000000);
        connection.setReadTimeout(5000000);

        /**Adding query parameters (included in default in new http client request) */
//        Map<String, String> parameters = new HashMap<String, String>();
//        parameters.put("q", "world");
//        connection.setDoOutput(true);
//        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//        out.writeBytes(main.ParameterStringBuilder.getParamsString(parameters));
//        out.flush();
//        out.close();

        /**Reading the response code*/
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            /**Redirecting the request*/
            if (redirect) {

                /** get redirect url from "location" header field*/
                String newUrl = connection.getHeaderField("Location");

                /** open the new connnection again*/
                System.out.println("Redirecting to URL : " + newUrl + " ....");
                parsedData.remove("URL");
                parsedData.put("URL", newUrl);
                GETRequest();
                return;
            }
            StringBuffer response = new StringBuffer();

            /** verbose option*/
            if (parsedData.containsKey("verbose")) {
                connection.getHeaderFields().entrySet().stream()
                        .filter(entry -> entry.getKey() != null)
                        .forEach(entry -> {
                            response.append(entry.getKey()).append(": ");
                            List headerValues = entry.getValue();
                            Iterator it = headerValues.iterator();
                            if (it.hasNext()) {
                                response.append(it.next());
                                while (it.hasNext()) {
                                    response.append(", ").append(it.next());
                                }
                            }
                            response.append("\n");
                        });

                response.append("\n");
            }

            /**connection output*/
            if (!parsedData.containsKey("outFile")) {
                System.out.println("GET Response Code :  " + responseCode);
                System.out.println("GET Response Message : " + connection.getResponseMessage());
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                    BufferedReader in = new BufferedReader(isr);
                    while ((readLine = in.readLine()) != null) {
                        response.append(readLine);
                        response.append("\n");

                    }
                    in.close();
                    // print result
                    System.out.println(response.toString());
                } else {
                    System.out.println("GET NOT WORKED");
                }
            }
            /** Publishing server response in a file*/
            else {
                PrintWriter printWriter = null;
                String filepath = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/" + parsedData.get("outFile");
                try {
                    File file = new File(filepath);
                    if (file.isFile()) {
                        file.delete();
                    }
                    printWriter = new PrintWriter(new File(filepath));
                    printWriter.println("GET Response Code :  " + responseCode);
                    printWriter.println("GET Response Message : " + connection.getResponseMessage());
                    if (responseCode == HttpURLConnection.HTTP_OK) { //success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                connection.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                            response.append("\n");
                        }
                        in.close();
                        // print result
                        printWriter.println(response.toString());
                    } else {
                        printWriter.println("GET NOT WORKED");
                    }
                    printWriter.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("CONNECTION TIMED OUT");
        }
        connection.disconnect();
    }

    /**
     * This method uses the parsed data to create a HTTP post request to the
     * requested server implementing the appropriate headers and parameters
     *
     * @throws IOException
     */
    public static void POSTRequest() throws IOException {

        final String POST_PARAMS;
        final String POST_PARAM_file;

        /**Creating connection and request for httpc*/
        URL obj = new URL(parsedData.get("URL"));
        HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
        postConnection.setRequestMethod("POST");
        postConnection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        boolean redirect = false;

        /** Setting the requested headers */
        if (parsedData.containsKey("Header")) {
            String head = parsedData.get("Header");
            while (!head.isEmpty()) {
                String parm1 = head.substring(1, head.indexOf(":"));
                head = head.substring(head.indexOf(":") + 1);
                String parm2 = head.substring(0, head.indexOf("\""));
                head = head.substring(head.indexOf(";"));
                postConnection.setRequestProperty(parm1, parm2);
                if ((head.length() == 1) && head.startsWith(";")) {
                    head = "";
                } else {
                    head = head.substring(1);
                }
            }
        }

        /**Configuring time outs (Extra property)*/

        postConnection.setConnectTimeout(5000000);
        postConnection.setReadTimeout(5000000);

        /** Adding the inline passed data to the body*/
        postConnection.setDoOutput(true);
        OutputStream os = postConnection.getOutputStream();
        if (parsedData.containsKey("Inline")) {
            String holder = parsedData.get("Inline");
//            POST_PARAMS = "{\"" + holder.substring(0, holder
//                    .indexOf(":")) + "\": " + holder.substring(holder.indexOf(":") + 1) + "}";
            POST_PARAMS = "{" + holder + "}";
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();
        }

        /** Adding the data passed to the body through file*/
        String line = null;
        String holder = "";
        if (parsedData.containsKey("Path")) {
            try {
                String path = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/" + parsedData.get("Path");
                FileReader fileReader =
                        new FileReader(path);
                BufferedReader bufferedReader =
                        new BufferedReader(fileReader);
                while ((line = bufferedReader.readLine()) != null) {
                    holder = holder + line;
                }
                bufferedReader.close();
                if (!holder.isEmpty()) {
                    holder = holder.replaceAll("\"", "\"");
                }
            } catch (FileNotFoundException ex) {
                System.out.println(
                        "Unable to open file '" +
                                parsedData.get("Path") + "'");
            } catch (IOException ex) {
                System.out.println(
                        "Error reading file '"
                                + parsedData.get("Path") + "'");
            } catch (NullPointerException ex) {
                System.out.println("There was a problem in reading file");
            }

            POST_PARAM_file = holder;
            os.write(POST_PARAM_file.getBytes());
            os.flush();
            os.close();
        }

        /**Checking for a possible redirect*/
        int responseCode = postConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }
        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = postConnection.getHeaderField("Location");
            // open the new connnection again
            System.out.println("Redirecting to URL : " + newUrl + " ....");
            parsedData.remove("URL");
            parsedData.put("URL", newUrl);
            POSTRequest();
            return;
        }

        /** verbose option check*/

        try {
            StringBuffer response = new StringBuffer();
            if (parsedData.containsKey("verbose")) {
                postConnection.getHeaderFields().entrySet().stream()
                        .filter(entry -> entry.getKey() != null)
                        .forEach(entry -> {
                            response.append(entry.getKey()).append(": ");
                            List headerValues = entry.getValue();
                            Iterator it = headerValues.iterator();
                            if (it.hasNext()) {
                                response.append(it.next());
                                while (it.hasNext()) {
                                    response.append(", ").append(it.next());
                                }
                            }
                            response.append("\n");
                        });
            }
            if (!parsedData.containsKey("outFile")) {
                System.out.println("POST Response Code :  " + responseCode);
                System.out.println("POST Response Message : " + postConnection.getResponseMessage());
                if (responseCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            postConnection.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                        response.append("\n");
                    }
                    in.close();
                    // print result
                    System.out.println(response.toString());
                } else {
                    System.out.println("POST NOT WORKED");
                }
            }
            /**Publishing the response in a file*/
            else {
                PrintWriter printWriter = null;
                String filepath = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/" + parsedData.get("outFile");
                try {
                    File file = new File(filepath);
                    if (file.isFile()) {
                        file.delete();
                    }
                    printWriter = new PrintWriter(new File(filepath));
                    printWriter.println("POST Response Code :  " + responseCode);
                    printWriter.println("POST Response Message : " + postConnection.getResponseMessage());
                    if (responseCode == HttpURLConnection.HTTP_OK) { //success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                postConnection.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                            response.append("\n");
                        }
                        in.close();
                        // print result
                        printWriter.println(response.toString());
                    } else {
                        printWriter.println("POST NOT WORKED");
                    }
                    printWriter.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("SERVER TIMED OUT");
        }
    }

    public void FILEGETRequest() throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {

            if (!parsedData.containsKey("file")) {

                //Send the message to the server

                String number = "GETDirectory";

                String sendMessage = number;
                sendMessage = "$$" + serverKey + ":" + sendMessage;

                Packet p = createPacket(SeqNumber, sendMessage);
                sendPacket(p, channel);
                System.out.println("Message sent to the server : " + sendMessage);
                String finalMessage;
                sentMessageTracker.put(SeqNumber, p);
                Packet recev = receivePacket(channel);

                if (recev != null) {
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    sentMessageTracker.put(SeqNumber, p);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);

                } else {
                    while (recev == null) {
                        sendPacket(p, channel);
                        recev = receivePacket(channel);
                        //wait(1000);
                    }
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    sentMessageTracker.put(SeqNumber, p);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);


                }
            } else {

                String sendMessage = "GET" + parsedData.get("file") + ":" + "FILE";
                sendMessage = "$$" + serverKey + ":" + sendMessage;
                Packet p = createPacket(SeqNumber, sendMessage);
                sendPacket(p, channel);

                System.out.println("Message sent to the server : " + sendMessage);
                Packet recev = receivePacket(channel);
                String finalMessage;
//                if(p.getType() == 0){
//                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
//                    if(finalMessage.startsWith("NON-MEM")){
//                        System.out.println("You are not connected to the server");
//                        System.out.println("Attempting three way handshake...");
//                        boolean counter = handshakeCheck();
//                        while(!counter){
//                            counter = handshakeCheck();
//                        }
//                        System.out.println("Three way handshake has been successfully completed");
//                        FILEGETRequest();
//                    }
//                }
                if (recev != null) {
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    sentMessageTracker.put(SeqNumber, p);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);

                } else {
                    while (recev == null) {
                        sendPacket(p, channel);
                        recev = receivePacket(channel);
                        //wait(1000);
                    }
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);
                    sentMessageTracker.put(SeqNumber, p);

                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void FILEPOSTRequest() throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            if (!parsedData.containsKey("file")) {

                //Send the message to the server

                String number = "POST cannot be used to make listing requests";

                String sendMessage = number;
                sendMessage = "$$" + serverKey + ":" + sendMessage;
                Packet p = createPacket(SeqNumber, sendMessage);
                sendPacket(p, channel);
                sentMessageTracker.put(SeqNumber, p);
                Packet recev = receivePacket(channel);
                System.out.println("Message sent to the server : " + sendMessage);
                String finalMessage;
                if (recev != null) {
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    sentMessageTracker.put(SeqNumber, p);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);

                } else {
                    while (recev == null) {
                        sendPacket(p, channel);
                        recev = receivePacket(channel);
                        //wait(1000);
                    }
                    finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                    System.out.println("Your directory is below : " + finalMessage);
                    Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                    sendPacket(p1, channel);
                    sentMessageTracker.put(SeqNumber, p);

                }
                System.out.println("Your directory is below : " + finalMessage);
            } else {
                if (!parsedData.containsKey("message")) {
                    System.out.println("Please enter appropriate content to write into the file");
                } else {
                    String number = "POST" + parsedData.get("file") + ":" + "FILE" + ";" + parsedData.get("message") +
                            ":" + "MESSAGE";
                    number = "$$" + serverKey + ":" + number;
                    Packet p = createPacket(SeqNumber, number);
                    sendPacket(p, channel);
                    Packet recev = receivePacket(channel);
                    System.out.println("Message sent to the server : " + number);
                    String finalMessage;
                    if (recev != null) {
                        finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("Final response message : " + finalMessage);
                        sentMessageTracker.put(SeqNumber, p);
                        Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                        sendPacket(p1, channel);

                    } else {
                        while (recev == null) {
                            sendPacket(p, channel);
                            recev = receivePacket(channel);
                            //wait(1000);
                        }
                        finalMessage = new String(p.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("Final response message : " + finalMessage);
                        Packet p1 = createPacket(0L, "$$" + serverKey + ":" + "ACK:" + SeqNumber);
                        sendPacket(p1, channel);
                        sentMessageTracker.put(SeqNumber, p);

                    }
                }


            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}
