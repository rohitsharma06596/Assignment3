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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HTTPCClient {
    /**
     * Member variable to hold the parsed Data from the Command Line
     */
    private static HashMap<String, String> parsedData;

    /**
     * Constructor to initialize the parsedData member variable
     * @param parm
     */
    HTTPCClient(HashMap<String, String> parm){
        parsedData = parm;
    }

    /**
     * This method uses the parsed data to create a HTTP get request to the
     * requested server implementing the appropriate headers and parameters
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
        if(parsedData.containsKey("Header")) {
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
            if(parsedData.containsKey("verbose")) {
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
            if(!parsedData.containsKey("outFile")) {
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
            else{
                PrintWriter printWriter = null;
                String filepath = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/"+parsedData.get("outFile");
                try {
                    File file = new File(filepath);
                    if(file.isFile()){
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
                    printWriter.close ();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            System.out.println("CONNECTION TIMED OUT");
        }
        connection.disconnect();
    }

    /**
     * This method uses the parsed data to create a HTTP post request to the
     * requested server implementing the appropriate headers and parameters
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
        if(parsedData.containsKey("Path")){
            try {
                String path = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/"+parsedData.get("Path");
                FileReader fileReader =
                        new FileReader(path);
                BufferedReader bufferedReader =
                        new BufferedReader(fileReader);
                while ((line = bufferedReader.readLine()) != null) {
                    holder = holder + line;
                }
                bufferedReader.close();
                if(!holder.isEmpty()) {
                    holder = holder.replaceAll("\"", "\"");
                }
            }
            catch(FileNotFoundException ex) {
                System.out.println(
                        "Unable to open file '" +
                                parsedData.get("Path") + "'");
            }
            catch(IOException ex) {
                System.out.println(
                        "Error reading file '"
                                + parsedData.get("Path") + "'");
            }
            catch (NullPointerException ex){
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
                else{
                    PrintWriter printWriter = null;
                    String filepath = "/Users/rohitsharma/IdeaProjects/Assignment1Network/src/"+parsedData.get("outFile");
                    try {
                        File file = new File(filepath);
                        if(file.isFile()){
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
                        printWriter.close ();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch(Exception e){
                System.out.println("SERVER TIMED OUT");
            }
        }
    public static void FILEGETRequest() throws IOException {

        /**Creating connection and request for httpc*/

//        String readLine = null;
//        URL urlForGetRequest = new URL(parsedData.get("URL"));
//        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setInstanceFollowRedirects(true);
        String host = "http://localhost";
        int port = 8080;
        InetAddress address = InetAddress.getByName(host);
        Socket socket = new Socket(address, port);
        try {

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            if (!parsedData.containsKey("file")) {

                //Send the message to the server

                String number = "GETDirectory";

                String sendMessage = number + "\n";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : " + sendMessage);
                String finalMessage = " ";
                String message;
                while((message = br.readLine()) != null){
                    finalMessage += message;
                    finalMessage += "\n";
                }
                System.out.println("Your directory is below : " + finalMessage);
            } else {

                String number = "GET"+parsedData.get("file") + ":" + "FILE";

                String sendMessage = number + "\n";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : " + sendMessage);
                String message = br.readLine();
                System.out.println("Final response message : " + message);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void FILEPOSTRequest() throws IOException {

        String host = "http://localhost";
        int port = 8080;
        InetAddress address = InetAddress.getByName(host);
        Socket socket = new Socket(address, port);
        try {

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            if (!parsedData.containsKey("file")) {

                //Send the message to the server

                String number = "POST cannot be used to make listing requests";

                String sendMessage = number + "\n";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : " + sendMessage);
                String finalMessage = " ";
                String message;
                while((message = br.readLine()) != null){
                    finalMessage += message;
                    finalMessage += "\n";
                }
                System.out.println("Your directory is below : " + finalMessage);
            } else {
                if (!parsedData.containsKey("message")){
                    System.out.println("Please enter an appropriate contents to write into the file");
                }
                else{
                    String number = "POST"+parsedData.get("file") + ":" + "FILE"+";"+ parsedData.get("message") +
                            ":"+"MESSAGE";


                    String sendMessage = number + "\n";
                    bw.write(sendMessage);
                    bw.flush();
                    System.out.println("Message sent to the server : " + sendMessage);
                    String message = br.readLine();
                    System.out.println("Final response message : " + message);
                }


            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }



}
