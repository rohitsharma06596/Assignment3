/**
 *
 * CommandLine.java
 *
 * This implementation class is an abstraction of a CommandLine application for client request
 * using our protocol HTTPC. It is the our new web request generation protocol modelled using
 * standard HTTP with customized selection of its features. It is made interactive by this
 * Command Line application
 *
 * @author Rohit Sharma, Ayush Lamichane
 * @since 03-10-2019
 */

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CommandLine {

    /**
     * Member variable to hold the parsed Data from the Command Line
     */
    private static HashMap<String, String> parsedData;

    /** Method to steer the parsing procedure*/
    public static int parse(String buffer){
        if(!buffer.contains(" ")){
            System.out.println("Incomplete command");
            return 0;
        }
        String holder = buffer.substring(0, buffer.indexOf(" "));
        if((holder.equalsIgnoreCase("httpc"))) {
            parsedData.put("protocol", holder);
            buffer = buffer.substring(buffer.indexOf(" "));
            buffer = buffer.trim();
            /**help options*/
            if(buffer.startsWith("help") && !buffer.isEmpty()){
                holder = buffer.substring(0,4);
                buffer = buffer.substring(4);
                buffer = buffer.trim();
                if(buffer.isEmpty()){
                    System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
                    System.out.println("Usage:");
                    System.out.println("    httpc and httpfs command [arguments]");
                    System.out.println("The commands are:");
                    System.out.println("    get     executes a HTTP GET request and prints the response.");
                    System.out.println("    post    executes a HTTP POST request and prints the response.");
                    System.out.println("    help    help prints this screen.");
                    System.out.println("Use \"httpc help [command]\" for more information about a command.");
                    return 2;
                }

                if(buffer.startsWith("get")){
                    System.out.println("usage: httpc get [-v] [-h key:value] URL");
                    System.out.println("Get executes a HTTP GET request for a given URL.");
                    System.out.println("-v  Prints the detail of the response such as protocol, status, and headers.");
                    System.out.println("-h \"key:value\" Associates headers to HTTP Request with the format \"key:value\". ");
                    return 2;
                }
                else if(buffer.startsWith("post")){
                    System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n");
                    System.out.println("Get executes a HTTP POST request for a given URL.");
                    System.out.println("-v  Prints the detail of the response such as protocol, status, and headers.");
                    System.out.println("-h \"key:value\" Associates headers to HTTP Request with the format \"key:value\".");
                    System.out.println("-d string Associates an inline data to the body HTTP POST request.");
                    System.out.println("-f file Associates the content of a file to the body HTTP POST request");
                    return 2;
                }
                else{
                    System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
                    System.out.println("Usage:");
                    System.out.println("    httpc command [arguments]");
                    System.out.println("The commands are:");
                    System.out.println("    get     executes a HTTP GET request and prints the response.");
                    System.out.println("    post    executes a HTTP POST request and prints the response.");
                    System.out.println("    help    help prints this screen.");
                    System.out.println("Use \"httpc help [command]\" for more information about a command.");
                    return 2;
                }
            }
            if(!buffer.contains(" ")){
                System.out.println("Incomplete command");
                return 0;
            }
            holder = buffer.substring(0, buffer.indexOf(" "));
            if((holder.equalsIgnoreCase("get") || holder.equalsIgnoreCase("post"))){
                parsedData.put("request", holder);
                buffer = buffer.substring(buffer.indexOf(" "));
                buffer = buffer.trim();
                if(!buffer.contains(" ")){
                    try{
                        if(!buffer.startsWith("http://")){

                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                url = "http://"+url;
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                buffer = "http://"+buffer;
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }
                        else {
                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }

                    } catch (MalformedURLException e) {
                        System.out.println("Incomplete command or Invalid URL");
                        return 0;
                    }

                }
                if(holder.equals("get")){
                    return prepareGetRequest(buffer);
                }
                else if(holder.equals("post")){
                    return preparePostRequest(buffer);
                }

            }
            else{
                System.out.println("Invalid request method");
                return 0;
            }
        }
        else if((holder.equalsIgnoreCase("httpfs"))) {
            parsedData.put("protocol", holder);
            buffer = buffer.substring(buffer.indexOf(" ") + 1);
            buffer.trim();
            holder = buffer.substring(0, buffer.indexOf(" "));
            if ((holder.equalsIgnoreCase("get") || holder.equalsIgnoreCase("post"))) {
                parsedData.put("request", "file" + holder);
                buffer = buffer.substring(buffer.indexOf(" "));
                buffer = buffer.trim();
            }
            if (holder.equalsIgnoreCase("get")){
                if (buffer.startsWith("/")) {
                    if (buffer.equals("/")) {
                        parsedData.put("URL", "http://localhost:8080");
                        return 1;
                    } else {
                        parsedData.put("file", buffer.substring(1));
                        parsedData.put("URL", "http://localhost:8080");
                        return 1;
                    }

                } else {
                    System.out.println("Invalid syntax");
                }
            }
            else if(holder.equalsIgnoreCase("post")){
                if (buffer.startsWith("/")) {
                    if (buffer.equals("/")) {
                        parsedData.put("URL", "http://localhost:8080");
                        return 1;
                    } else {
                        buffer = buffer.substring(1);
                        if(buffer.contains(" ")){
                            parsedData.put("file", buffer.substring(0, buffer.indexOf(" ")));
                            parsedData.put("URL", "http://localhost:8080");
                            parsedData.put("message", buffer.substring(buffer.indexOf(" ")+1));
                            return 1;
                        }
                        else{
                            System.out.println("You are missing either the file name or the message in the syntax");
                            return 0;
                        }

                    }

                } else {
                    System.out.println("Invalid syntax");
                }
            }
            if(!parsedData.keySet().contains("URL")) {
                parsedData.put("URL", buffer.substring(0, buffer.indexOf(" ")));
                return 1;
            }
            return 0;
        }
        else{
            System.out.println("Invalid protocol");
            return 0;
        }
        return 0;
    }

    /**
     * This method reads the request from from the command prompt and prepares a parsedData
     * hashMap which can be used to prepared the client request
     * @param buffer
     * @return
     */
    public static int prepareGetRequest(String buffer){
        String holder = "";
        if(buffer.startsWith("-") && buffer.contains(" ")){
            holder = buffer.substring(0, buffer.indexOf(" "));
            if((holder.equals("-v"))||(holder.equals("-h"))){
                holder = buffer.substring(0, buffer.indexOf(" "));
                if(holder.equals("-v")){
                    parsedData.put("verbose", "true");
                    buffer = buffer.substring(buffer.indexOf(" "));
                    buffer = buffer.trim();
                    if(!buffer.contains(" ")){
                        try {
                            java.net.URL finalStepURL = new java.net.URL(buffer);
                        } catch (MalformedURLException e) {
                            System.out.println("Incomplete command or invalid URL");
                            return 0;
                        }
                    }
                }
                if (buffer.startsWith("-h")) {
                    holder = buffer.substring(0, buffer.indexOf(" "));
                    buffer = buffer.substring(buffer.indexOf(" "));
                    buffer = buffer.trim();
                    String temp = "";
                    String header = "";
                    if(!buffer.contains(" ")){
                        System.out.println("Incomplete command");
                        return 0;
                    }
                    while(buffer.contains("\"")){
                        if(!buffer.contains(" ")){
                            System.out.println("Incomplete Command");
                            return 0;
                        }
                        if(buffer.contains("-f")||buffer.contains("-d")||holder.contains("-f")||holder.contains("-d")){
                            System.out.println("This option is not available for get method");
                            return 0;
                        }
                        holder = buffer.substring(0, buffer.indexOf(" "));
                        buffer = buffer.substring(buffer.indexOf(" "));
                        buffer = buffer.trim();
                        if(!holder.contains(":")){
                            System.out.println("Header parameters invalid");
                            return 0;
                        }
                        temp = temp + holder + ";";
                    }
                    temp = temp.trim();
                    if(temp.isEmpty()){
                        System.out.println("No Header parameters provided");
                        return 0;
                    }
                    else {
                        parsedData.put("Header", temp);
                    }
                    if(buffer.isEmpty()){
                        System.out.println("Missing URL");
                        return 0;
                    }

                }
                if(!buffer.startsWith("-")){
                    try{
                        if(!buffer.startsWith("http://")){

                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                url = "http://"+url;
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                buffer = "http://"+buffer;
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }
                        else {
                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }

                    } catch (MalformedURLException e) {
                        System.out.println("Invalid URL");
                        return 0;
                    }
                }
                else{
                    System.out.println("Invalid request option for get");
                    return 0;
                }
            }
            else{
                System.out.println("Invalid request option for get");
                return 0;
            }
            try{
                if(!buffer.startsWith("http://")){

                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        url = "http://"+url;
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        buffer = "http://"+buffer;
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }
                else {
                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }

            } catch (MalformedURLException e) {
                System.out.println("Invalid URL");
                return 0;
            }
        }
        else{
            try{
                if(!buffer.startsWith("http://")){

                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        url = "http://"+url;
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        buffer = "http://"+buffer;
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }
                else {
                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }

            } catch (MalformedURLException e) {
                System.out.println("Invalid URL");
                return 0;
            }
        }
        System.out.println("GET Command parsing Successful...");
        System.out.println(parsedData);
        return 1;
    }

    /**
     * This method reads the request from from the command prompt and prepares a parsedData
     * hashMap which can be used to prepared the client request
     * @param buffer
     * @return
     */
    public static int preparePostRequest(String buffer){
        String holder = "";
        if(buffer.startsWith("-") && buffer.contains(" ")){
            holder = buffer.substring(0, buffer.indexOf(" "));
            if((holder.equals("-v"))||(holder.equals("-h"))){
                holder = buffer.substring(0, buffer.indexOf(" "));
                if(holder.equals("-v")){
                    parsedData.put("verbose", "true");
                    buffer = buffer.substring(buffer.indexOf(" "));
                    buffer = buffer.trim();
                    if(!buffer.contains(" ")){
                        try {
                            java.net.URL finalStepURL = new java.net.URL(buffer);
                        } catch (MalformedURLException e) {
                            System.out.println("Incomplete command or invalid URL");
                            return 0;
                        }
                    }
                }
                if (buffer.startsWith("-h")) {
                    holder = buffer.substring(0, buffer.indexOf(" "));
                    buffer = buffer.substring(buffer.indexOf(" "));
                    buffer = buffer.trim();
                    String temp = "";
                    String header = "";
                    if(!buffer.contains(" ")){
                        System.out.println("Incomplete command");
                        return 0;
                    }
                    do{
                        if(buffer.startsWith("-d")||buffer.startsWith("-f")){
                            break;
                        }
                        if(buffer.startsWith("http")){
                            break;
                        }
                        if(buffer.startsWith("www")){
                            break;
                        }
                        if(!buffer.contains(" ")){
                            System.out.println("Incomplete Command");
                            return 0;
                        }
                        holder = buffer.substring(0, buffer.indexOf(" "));

                        buffer = buffer.substring(buffer.indexOf(" "));
                        buffer = buffer.trim();
                        if(!holder.contains(":")){
                            System.out.println("Header parameters invalid");
                            return 0;
                        }
                        temp = temp + holder + ";";
                    }while(!buffer.startsWith("-")|| !buffer.startsWith("http")|| !buffer.startsWith("www"));
                    temp = temp.trim();
                    if(temp.isEmpty()){
                        System.out.println("No Header parameters provided");
                        return 0;
                    }
                    else{
                        parsedData.put("Header", temp);
                    }
                    if(buffer.startsWith("-")){
                        holder = buffer.substring(0, buffer.indexOf(" "));
                        if((holder.equals("-d"))) {
                            buffer = buffer.replaceAll("\"","\"");
                            temp = "";
//                            buffer = buffer.substring(buffer.indexOf("\"")+1);
//                            buffer = buffer.trim();
//                            holder = buffer.substring(0, buffer.indexOf("\""));
//                            buffer = buffer.substring(buffer.indexOf("\"")+1);
//                            temp = holder +":"+ buffer.substring(1, buffer.indexOf("}")).trim();
//                            buffer = buffer.substring( buffer.indexOf("}"));
//                            buffer = buffer.trim();
                            temp = buffer.substring(buffer.indexOf("{")+1, buffer.indexOf("}"));
                            buffer = buffer.substring(buffer.indexOf("}")+1);
                            buffer = buffer.trim();
                            parsedData.put("Inline", temp);
                            if(buffer.startsWith("}")){
                                if(buffer.length() == 1){
                                    System.out.println("Missing URL parameter");
                                    return 0;
                                }
                                else{
                                    buffer = buffer.substring(1);
                                    buffer = buffer.trim();
                                }
                            }
                        }
                        else if((holder.equals("-f")&& buffer.contains(" "))) {
                            buffer = buffer.substring(buffer.indexOf(" "));
                            buffer = buffer.trim();
                            String path = buffer.substring(0, buffer.indexOf(" "));
                            buffer = buffer.substring(buffer.indexOf(" "));
                            buffer = buffer.trim();
                            parsedData.put("Path", path);
                        }
                        else{
                            System.out.println("file or inline parameters invalid");
                            return 0;
                        }
                    }
                    if(buffer.startsWith("-")){ // for letting either file or inline option to be parsed
                        System.out.println("Cannot use file and inline parameters together");
                        return 0;
                    }
                    if(buffer.isEmpty()){
                        System.out.println("Missing URL");
                        return 0;
                    }
                    try {
                        if(!buffer.startsWith("http://")){

                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                url = "http://"+url;
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                buffer = "http://"+buffer;
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }
                        else {
                            if(buffer.contains("-o")){
                                String url = buffer.substring(0, buffer.indexOf(" "));
                                url = url.trim();
                                String outFile = buffer.substring(buffer.indexOf("-")+2);
                                outFile = outFile.trim();
                                parsedData.put("outFile", outFile);
                                java.net.URL finalStepURL = new java.net.URL(url);
                                parsedData.put("URL", url);
                            }
                            else{
                                buffer = buffer.trim();
                                java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                                parsedData.put("URL", buffer.trim());
                            }

                        }

                    } catch (MalformedURLException e) {
                        System.out.println("Invalid URL");
                        return 0;
                    }
                }
                else{
                    if(buffer.startsWith("-")){
                        String temp;
                        holder = buffer.substring(0, buffer.indexOf(" "));
//                        if((holder.equals("-d") && buffer.contains("\"") && buffer.contains(":"))) {
                        if((holder.equals("-d"))){
                            buffer = buffer.replaceAll("\"","\"");
                            temp = "";
//                            buffer = buffer.substring(buffer.indexOf("\"")+1);
//                            buffer = buffer.trim();
//                            holder = buffer.substring(0, buffer.indexOf("\""));
//                            buffer = buffer.substring(buffer.indexOf("\"")+1);
//                            temp = holder +":"+ buffer.substring(1, buffer.indexOf("}")).trim();
//                            buffer = buffer.substring( buffer.indexOf("}"));
//                            buffer = buffer.trim();
                            temp = buffer.substring(buffer.indexOf("{")+1, buffer.indexOf("}"));
                            buffer = buffer.substring(buffer.indexOf("}")+1);
                            buffer = buffer.trim();
                            parsedData.put("Inline", temp);
                            if(buffer.startsWith("}")){
                                if(buffer.length() == 1){
                                    System.out.println("Missing URL parameter");
                                }
                                else{
                                    buffer = buffer.substring(1);
                                    buffer = buffer.trim();
                                }
                            }
                        }
                        else if((holder.equals("-f")&& buffer.contains(" "))) {
                            buffer = buffer.substring(buffer.indexOf(" "));
                            buffer = buffer.trim();
                            String path = buffer.substring(0, buffer.indexOf(" "));
                            buffer = buffer.substring(buffer.indexOf(" "));
                            buffer = buffer.trim();
                            parsedData.put("Path", path);
                        }
                        else{
                            System.out.println("file or inline parameters invalid");
                            return 0;
                        }
                    }
                    else {
                        System.out.println("Invalid request option for get");
                        return 0;
                    }
                }
            }
            else{
                System.out.println("Invalid request option for get");
                return 0;
            }
        }
        else{
            try{
                if(!buffer.startsWith("http://")){

                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        url = "http://"+url;
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        buffer = "http://"+buffer;
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }
                else {
                    if(buffer.contains("-o")){
                        String url = buffer.substring(0, buffer.indexOf(" "));
                        url = url.trim();
                        String outFile = buffer.substring(buffer.indexOf("-")+2);
                        outFile = outFile.trim();
                        parsedData.put("outFile", outFile);
                        java.net.URL finalStepURL = new java.net.URL(url);
                        parsedData.put("URL", url);
                    }
                    else{
                        buffer = buffer.trim();
                        java.net.URL finalStepURL = new java.net.URL(buffer.trim());
                        parsedData.put("URL", buffer.trim());
                    }

                }

            } catch (MalformedURLException e) {
                System.out.println("Invalid URL");
                return 0;
            }
        }
        System.out.println("POST Command parsing Successful...");
        System.out.println(parsedData);
        return 1;
    }

    /**
     * the main driver method for the complete Command line application
     * @param args
     */
    public static void main(String args[]){
        parsedData = new HashMap<String, String>();
            try {
                DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                Date dateObj = new Date();
                byte[] b = new byte[1024];
                parsedData.clear();
                System.out.print(df.format(dateObj)+" LocalComp-HTTPCPrompt:~ ");
                for (int r; (r = System.in.read(b)) != -1; ) {
                    String buffer = new String(b, 0, r);
                    int val = parse(buffer);
                    DateFormat dfnew = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                    Date dateObjnew = new Date();
                    System.out.println("\n");
                    if(val == 1) {
                        HTTPCClient client = new HTTPCClient(parsedData);
                        if (parsedData.get("request").equalsIgnoreCase("get")) {
                            HTTPCClient.GETRequest();
                        } else if (parsedData.get("request").equalsIgnoreCase("post")) {
                            HTTPCClient.POSTRequest();
                        } else if (parsedData.get("request").equalsIgnoreCase("fileget")) {
                            HTTPCClient.FILEGETRequest();
                        } else if (parsedData.get("request").equalsIgnoreCase("filepost")) {
                            HTTPCClient.FILEPOSTRequest();
                        }
                    }

                    parsedData.clear();
                    System.out.print(dfnew.format(dateObjnew)+" LocalComp-HTTPCPrompt:~ ");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}
