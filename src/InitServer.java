import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class InitServer {

    public static void main(String args[]) throws IOException {
        System.out.println("The syntax to initialize the server is ");
        System.out.println("httpfs is a simple file server.");
        System.out.println("usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        System.out.println("-v Prints debugging messages.");
        System.out.println("-p Specifies the port number that the server will listen and serve at.");
        System.out.println("Default is 8080.");
        System.out.println("-d Specifies the directory that the server will use to read/write");
        System.out.println("requested files. Default is the current directory when launching the");
        System.out.println("application.");
        System.out.println("You can set the scope of the server to one of the directories listed below");
        System.out.println("By default it will take the scope of the entire server directory");
        returnDirectory();
        System.out.println();
        System.out.println();
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        HTTPServer server =  new HTTPServer(command);

    }
    public static void returnDirectory() throws IOException {
        String path  = new java.io.File( "." ).getCanonicalPath() + "/src/" + "/ServerFileDir/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String str = "\n";
        assert listOfFiles != null;
        int j =1;
        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println(" Directory" + j + ": " + listOfFiles[i].getName());
                j = j + 1;
            }
        }
    }

}
