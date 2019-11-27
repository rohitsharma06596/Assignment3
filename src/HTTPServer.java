import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class HTTPServer extends Thread{

    ArrayList<String> filesOnHold = new ArrayList<String>();
    private ArrayList<String> listFiles =  new ArrayList<>();
    private ArrayList<String> allfiles  = new ArrayList<>();
    private boolean verbose;

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

    private int port;
    private String path;
    private Socket socket;

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
            this.setPort(8080);
        }

        try
        {

            ServerSocket serverSocket = new ServerSocket(this.getPort());
            System.out.println("Sever started and listening to the port "+this.getPort());

            while(true)
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                Thread t = new Thread(this);
                t.start();
                listFiles.clear();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
    public void run() {
        try {
            StringBuilder response = new StringBuilder();
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String number = br.readLine();
            System.out.println(number);

            if(number.startsWith("GET")){
                number = number.substring(3);
                ServeGETRequest(number, response);
            }
           if(number.startsWith("POST")) {
                number = number.substring(4);
                ServePOSTRequest(number, response);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void ServeGETRequest(String number, StringBuilder response) throws IOException, InterruptedException {
        System.out.println("Message received from client is " + number);
        System.out.println(" -------------------------------------------");
        System.out.println();

        if(number.contains("Directory")){
            String holder  = returnDirectory();
            System.out.println(holder);
            response.append(holder);

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(holder);
            if(isVerbose()){
                String temp = verboseContent();
                holder = temp + holder;
            }
            System.out.println("Message sent to the client is 1 " + holder);
            bw.flush();
//            os.close();
            osw.close();
        }
        else if(number.endsWith(":FILE")) {
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
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
                        if (entireDirectory("/Users/rohitsharma/IdeaProjects/Assignment1Network/src/ServerFileDir/"
                                , number.substring(0, number.indexOf(":")))) {

                            String hope = "File is present in a directory which is out of your scope of access";
                            if (isVerbose()) {
                                String temp = verboseContent();
                                hope = temp + hope;
                            }
                            bw.write(hope);
                            System.out.println("Message sent to the client is 2 " + hope);
                        } else {
                            String hope = "HTTPFS: ERROR 404 The file was not found";
                            if (isVerbose()) {
                                String temp = verboseContent();
                                hope = temp + hope;
                            }
                            bw.write(hope);
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
                                bw.write(hope);
                                System.out.println("Message sent to the client is 2 " + hope);
                            } else {
                                if (isVerbose()) {
                                    String temp = verboseContent();
                                    hope = temp + hope;
                                }
                                bw.write(hope);
                                System.out.println("Message sent to the client is 2 " + hope);
                            }
                        } else {
                            bw.write("The selected file is a directory you are not authorized access to it");
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

            bw.flush();
//            os.close();
            osw.close();
        }
        else{
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            System.out.println("This is a general HTTPC request");
            String temp = "HTTP/1.1 200 OK ";
            temp += "Server: localhost; ";
            temp += "Content-Type: text; ";
            temp += "This is the constant test output for the server;";
            System.out.println("Message has been sent to the client");
            bw.write(temp);
            bw.flush();
//            os.close();
            osw.close();



        }
        filesOnHold.clear();
    }

    public void ServePOSTRequest(String number, StringBuilder response) throws IOException, InterruptedException {
        System.out.println("Message received from client is " + number);
        System.out.println(" -------------------------------------------");
        System.out.println();
        OutputStream os = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);

        if(number.contains("Directory")){
            String holder  = "POST cannot be used to list directory";
            System.out.println(holder);
            response.append(holder);
            if(isVerbose()){
                String temp = verboseContent();
                holder = temp + holder;
            }
            bw.write(holder);
            System.out.println("Message sent to the client is 1 " + holder);
            bw.flush();
//            os.close();
            osw.close();
        }
        else if(number.endsWith(":MESSAGE")) {
            String filename = number.substring(0, number.indexOf(":"));
            number = number.substring(number.indexOf(":") + 1);
            String messa = number.substring(number.indexOf(";") + 1, number.indexOf(":"));
            System.out.println("Type 2");
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
                        bw.write(hope);
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
                            bw.write(hope);
                            System.out.println("Message sent to the client is 2 " + hope);

                        } else {
                            bw.write("The selected file is a directory you are not authorized access to it");
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

            bw.flush();
//            os.close();
            osw.close();
        }
        else {
            System.out.println("This is a general HTTPC request");
            System.out.println("The message received by the client is: " + number );
            bw.write("HTTP/1.1 200 OK This is the basic response to the the post request by the client, the post has been notified");
            bw.flush();
//            os.close();
            osw.close();
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
}
