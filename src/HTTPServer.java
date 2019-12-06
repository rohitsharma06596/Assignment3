import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HTTPServer extends Thread{

    ArrayList<String> filesOnHold = new ArrayList<String>();
    private ArrayList<String> listFiles =  new ArrayList<>();
    private ArrayList<String> allfiles  = new ArrayList<>();
    private boolean verbose;
    ArrayList<String> connectedUsers = new ArrayList<>();
    ArrayList<String> standbyUsers = new ArrayList<>();
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    long expectedSequenceNumber = 2L;

    HashMap<Long, Packet> sentMessageTracker = new HashMap<>();
    Date trackerFlushtimer = new Date();
    Long ackCounter = 0L;

    private int port;
    private String path;
    private SocketAddress router;

    HTTPServer(String command) throws IOException {
        if(command.contains("-v")){
            this.setVerbose(true);
        }
        else{
            this.setVerbose(false);
        }
        if(command.contains("-d")){
            String path  = new java.io.File( "." ).getCanonicalPath() + "/src/" + "/ServerFileDir/";
            path += command.substring(command.indexOf("-d")+3);
            path += "/";
            this.setPath(path);
        }
        else{
            String path  = new java.io.File( "." ).getCanonicalPath() + "/src/" + "/ServerFileDir/";
            this.setPath(path);
        }
        if(command.contains("-p")){
            String val = command.substring(command.indexOf("-p")+3);
            String port = val.substring(0, val.indexOf(" "));
            this.setPort(Integer.parseInt(port));
        }
        else{
            this.setPort(8007);
        }

        System.out.println("Sever started and listening to the port "+this.getPort());

        //Reading the message from the client
        Thread t = new Thread(this);
        this.start();
        this.listenAndServe(this.port);




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
            timeNow = new Date();
            if(!sentMessageTracker.isEmpty()){
                for(long i = ackCounter+1; i <expectedSequenceNumber; i++){
                    try(DatagramChannel channel = DatagramChannel.open()) {
                        if(sentMessageTracker.keySet().contains(i)){
                            System.out.println("The ackCounter is "+ackCounter);
                            System.out.println("The packet with sequense number "+i+" was lost");
                            System.out.println("Resending the packet");
                            String payload = new String(sentMessageTracker.get(i).getPayload(), UTF_8);
                            respondPacket(payload,sentMessageTracker.get(i), channel, router);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void ServeGETRequest(String number, StringBuilder response) throws IOException, InterruptedException {
        System.out.println("Message received from client is " + number);
        System.out.println(" -------------------------------------------");
        System.out.println();
        number.trim();

        if(number.contains("Directory")){
            String holder  = returnDirectory();
            System.out.println(holder);
            response.append(holder);

//            OutputStream os = socket.getOutputStream();
//            OutputStreamWriter osw = new OutputStreamWriter(os);
//            BufferedWriter bw = new BufferedWriter(osw);
//            bw.write(holder);
            if(isVerbose()){
                String temp = verboseContent();
                holder = temp + holder;
            }
            System.out.println("Message sent to the client is 1 " + holder);
//            bw.flush();
//            os.close();
//            osw.close();
        }
        else if(number.endsWith(":FILE")) {
//            OutputStream os = socket.getOutputStream();
//            OutputStreamWriter osw = new OutputStreamWriter(os);
//            BufferedWriter bw = new BufferedWriter(osw);
            System.out.println("Type 2");
            File holder = returnFile(number.substring(0, number.indexOf(":")));
            String name;
            if(holder != null) {
                name = holder.getName();
            }
            else{
                name = "testname";
            }
            while(true){
                if(!filesOnHold.contains(name)) {
                    filesOnHold.add(name);
                    if (holder == null) {
                        if (entireDirectory("/Users/rohitsharma/IdeaProjects/Assignment3/src/ServerFileDir/"
                                , number.substring(0, number.indexOf(":")))) {

                            String hope = "File is present in a directory which is out of your scope of access";
                            if (isVerbose()) {
                                String temp = verboseContent();
                                hope = temp + hope;
                            }
                            //bw.write(hope);
                            response.append(hope);
                            System.out.println("Message sent to the client is 2 " + hope);
                        } else {
                            String hope = "HTTPFS: ERROR 404 The file was not found";
                            if (isVerbose()) {
                                String temp = verboseContent();
                                hope = temp + hope;
                            }
                            response.append(hope);
                            System.out.println("Message sent to the client is 2 " + hope);
                        }
                    } else {
                        if (holder.isFile()) {
                            String hope = fileToString(holder);
                            if (hope.isEmpty()) {
                                hope = "HTTPFS :ERROR 0701 the file is empty";
                                if (isVerbose()) {
                                    String temp = verboseContent();
                                    hope = temp + hope;
                                }
                                //bw.write(hope);
                                response.append(hope);
                                System.out.println("Message sent to the client is 2 " + hope);
                            } else {
                                if (isVerbose()) {
                                    String temp = verboseContent();
                                    hope = temp + hope;
                                }
                                //bw.write(hope);
                                response.append(hope);
                                System.out.println("Message sent to the client is 2 " + hope);
                            }
                        } else {
                            //bw.write("The selected file is a directory you are not authorized access to it");
                            response.append("The selected file is a directory you are not authorized access to it");
                            System.out.println("Message sent to the client is 2 " + "The selected file is a directory you " +
                                    "are not authorized access to it");
                        }
                    }
                    filesOnHold.remove(name);
                    break;
                }
                else{
                    wait(1000);
                }
            }

            //bw.flush();
//            os.close();
            //osw.close();
        }
        else{
//            OutputStream os = socket.getOutputStream();
//            OutputStreamWriter osw = new OutputStreamWriter(os);
//            BufferedWriter bw = new BufferedWriter(osw);
            System.out.println("This is a general HTTPC request");
            String temp = "HTTP/1.1 200 OK ";
            temp += "Server: localhost; ";
            temp += "Content-Type: text; ";
            temp += "This is the constant test output for the server;";
            System.out.println("Message has been sent to the client");
            //bw.write(temp);
            response.append(temp);
           // bw.flush();
//            os.close();
           // osw.close();



        }
        filesOnHold.clear();
    }

    public void ServePOSTRequest(String number, StringBuilder response) throws IOException, InterruptedException {
        System.out.println("Message received from client is " + number);
        System.out.println(" -------------------------------------------");
        System.out.println();
//        OutputStream os = socket.getOutputStream();
//        OutputStreamWriter osw = new OutputStreamWriter(os);
//        BufferedWriter bw = new BufferedWriter(osw);

        if(number.contains("Directory")){
            String holder  = "POST cannot be used to list directory";
            System.out.println(holder);
            response.append(holder);
            if(isVerbose()){
                String temp = verboseContent();
                response.append(temp);
            }

            System.out.println("Message sent to the client is 1 " + holder);

//            os.close();
        }
        else if(number.endsWith(":MESSAGE")) {
            String filename = number.substring(0, number.indexOf(":"));
            number = number.substring(number.indexOf(":") + 1);
            String messa = number.substring(number.indexOf(";") + 1, number.indexOf(":"));
            File holder = returnFile(filename);
            String name;
            if(holder == null) {
                name = "testfilename";
            }
            else{
                name = holder.getName();
            }
            while (true){
                if(!filesOnHold.contains(name)) {
                    filesOnHold.add(name);
                    if (holder == null) {
                        createFileandWrite(filename, messa);
                        String hope = "File has been created at the server in your accessible directory with the contents" +
                                " supplied by you";
                        if (isVerbose()) {
                            String temp = verboseContent();
                            hope = temp + hope;
                        }
                        response.append(hope);
                        System.out.println("Message sent to the client is 2 " + hope);
                        filesOnHold.remove(name);
                        break;
                    } else {
                        if (holder.isFile()) {
                            openFileandWrite(holder, messa);
                            String hope = "File was found at the server in your accessible directory and has been overwritten" +
                                    " by contents supplied by you";
                            if (isVerbose()) {
                                String temp = verboseContent();
                                hope = temp + hope;
                            }
                            response.append(hope);
                            System.out.println("Message sent to the client is 2 " + hope);

                        } else {
                            response.append("The selected file is a directory you are not authorized access to it");
                            System.out.println("Message sent to the client is 2 " + "The selected file is a directory you " +
                                    "are not authorized access to it");
                        }
                        filesOnHold.remove(name);
                        break;
                    }
                }
                else{
                    wait(1000);
                }
        }
//            os.close();
        }
        else {
            System.out.println("This is a general HTTPC request");
            System.out.println("The message received by the client is: " + number );
            response.append("HTTP/1.1 200 OK This is the basic response to the the post request by the client, " +
                    "the post has been notified");
//            os.close();
        }
        filesOnHold.clear();
    }

    public void openFileandWrite(File file, String messa) throws IOException {
        String filename = file.getName();
        if(file.delete()){
            createFileandWrite(filename, messa);
        }

    }

    public void createFileandWrite(String filename, String messa) throws IOException {
        String path  = this.getPath();
        File file = new File(path + filename);
        FileWriter writer;
        try {
            writer = new FileWriter(file, true);
            PrintWriter printer = new PrintWriter(writer);
            printer.append(messa);
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String returnDirectory(){

        File folder = new File(this.getPath());
        File[] listOfFiles = folder.listFiles();
        String str = "\n";
        assert listOfFiles != null;
        for (int i = 0; i < listOfFiles.length; i++) {
            int j = i +1;
            if (listOfFiles[i].isFile()) {
                str += (" " + j + " File        ");
                str += (listOfFiles[i].getName());
                listFiles.add(listOfFiles[i].getName());
                str += ("\n");
                //System.out.println("File      " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                str += (" "+ j +" Directory   ");
                str += (listOfFiles[i].getName());
                listFiles.add(listOfFiles[i].getName());
                str += ("\n");
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return str;
    }

    public File returnFile( String fileName) throws IOException {

        String path  = this.getPath();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String str = "\n";
        assert listOfFiles != null;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (fileName.equalsIgnoreCase(listOfFiles[i].getName().trim())) {
                    return listOfFiles[i];
                }
                //System.out.println("File      " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                    if (fileName.equalsIgnoreCase(listOfFiles[i].getName().trim())) {
                        return listOfFiles[i];
                }
            }
        }
        return null;
    }

    public String fileToString(File file) throws IOException {
        String contents = "";
        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        while ((st = br.readLine()) != null)
            contents += st;
        return contents;
    }
    public boolean entireDirectory(String path, String filename){
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String str = "\n";
        assert listOfFiles != null;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                str += (" File        ");
                str += (listOfFiles[i].getName());
                allfiles.add(listOfFiles[i].getName());
                str += ("\n");
            } else if (listOfFiles[i].isDirectory()) {
                entireDirectory(path+"/"+listOfFiles[i].getName()+"/", filename);
            }
        }
        System.out.println(allfiles);
        if(allfiles.contains(filename)){
            return true;
        }
        else{
            return false;
        }
    }

    public String verboseContent(){
        Date today = new Date();
        String str = "";
        str += "HTTP/1.1 200 OK\n";
        str += "Server: httpfs\n";
        str += "Date: " + today.getTime()+"\n";
        str += "Connection: close\n";
        str += "Access-Control-Allow-Origin: *\n";
        str += "Access-Control-Allow-Credentials: true\n";
        return str;
    }

    public void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
//            Thread t = new Thread(this);
//            t.start();
//            listFiles.clear();
            channel.bind(new InetSocketAddress(port));
            //logger.info("EchoServer is listening at {}", channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);
                if(this.router == null){
                    this.router = router;
                }
                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                String clientKey = null;
                String payload = new String(packet.getPayload(),UTF_8);
                long clientSqno = 0L;
                if(packet.getType()!=0){
                    clientKey = payload.substring(2, payload.indexOf(":"));
                    clientSqno = packet.getSequenceNumber();
                }
                System.out.println(new String(packet.getPayload(),UTF_8));
                buf.flip();
                String msg = processPacket(packet);
                if(msg != null){
                    respondPacket(msg,packet,channel,router);
                    String ackMsg = "$$" + clientKey + ":" + "ACK:"+ clientSqno;
                    respondPacket(ackMsg,packet,channel,router);
                    System.out.println("Ack to the packet has been responded");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String processPacket(Packet p) throws IOException, InterruptedException {
        System.out.print("A packet has been received from ");
        String payload1 = new String(p.getPayload(), UTF_8);
        System.out.println(payload1);
        if(p.getType() == 0){
            String payload = new String(p.getPayload(), UTF_8);
            if(payload.equalsIgnoreCase("SYN")){
                Date curTime = new Date();
                long key = curTime.getTime();
                standbyUsers.add(Long.toString(key));
                connectedUsers.add(Long.toString(key));
                System.out.println(key);
                return "ACK:"+ key;

            }
            else{
                if(payload.startsWith("SYN-ACK")){
                    String key = payload.substring(payload.indexOf(":")+1);
                    if(standbyUsers.contains(key)){
                        ConnectedUsers holder = new ConnectedUsers(key);
                        standbyUsers.remove(key);
                        System.out.println(key);
                        Thread t = new Thread();
                        t.start();
                    }

                }
                else if(payload.contains("ACK")){
                    String usr = payload.substring(2, payload.indexOf(":"));
                    payload = payload.substring(payload.indexOf(":")+1);
                    if(connectedUsers.contains(usr)){
                        long seqNo = Long.parseLong(payload.substring(payload.indexOf(":")+1));
                        if(expectedSequenceNumber==seqNo) {
                            expectedSequenceNumber = seqNo + 1;
                            System.out.println("The expected sequence number is" + expectedSequenceNumber);
                            ackCounter = seqNo;
                            System.out.println("The updated ack number is "+ackCounter);
                        }
                        else{
                            if(expectedSequenceNumber == 16L){
                                expectedSequenceNumber = seqNo + 1;
                                ackCounter = seqNo;
                                System.out.println("The updated sequence number is "+ackCounter);
                                System.out.println("The expected sequence number is" + expectedSequenceNumber);
                            }
                            else {
                                expectedSequenceNumber = seqNo + 1;
                                ackCounter = seqNo;
                                System.out.println("The updated sequence number is "+ackCounter);
                                System.out.println("The expected sequence alternate is" + expectedSequenceNumber);
                                //some work
                            }
                        }
                    }
                }
                return null;
            }
        }
        else{
            String payload = new String(p.getPayload(), UTF_8);
            String key = payload.substring(payload.indexOf("$$")+2, payload.indexOf(":"));
            payload = payload.substring(payload.indexOf(":")+1);
            if(!authenticateUser(key)){
                return "NON-MEM";
            }
            else{
                StringBuilder response = new StringBuilder();
                if(payload.startsWith("GET")){
                    payload = payload.substring(3);
                    ServeGETRequest(payload, response);
                    return response.toString();
                }
                if(payload.startsWith("POST")) {
                    payload = payload.substring(4);
                    ServePOSTRequest(payload, response);
                    return response.toString();
                }
            }
            return null ;
        }

    }
    public void respondPacket(String msg, Packet p, DatagramChannel channel, SocketAddress router) throws IOException {
        String payload = new String(p.getPayload(), UTF_8);
        //logger.info("Packet: {}", packet);
        //logger.info("Payload: {}", payload);
        //logger.info("Router: {}", router);

        // Send the response to the router not the client.
        // The peer address of the packet is the address of the client already.
        // We can use toBuilder to copy properties of the current packet.
        // This demonstrate how to create a new packet from an existing packet.
        Packet resp = p.toBuilder()
                .setPayload(msg.getBytes())
                .create();
        channel.send(resp.toBuffer(), router);
        if(resp.getSequenceNumber() != 0L) {
            sentMessageTracker.put(resp.getSequenceNumber(), resp);
        }
        payload = new String(p.getPayload(), UTF_8);
        System.out.println("The packet has been responded");

    }

    public boolean authenticateUser(String key){

        if(connectedUsers.contains(key)){
            return true;
        }
        else{
            return false;
        }
    }
}
