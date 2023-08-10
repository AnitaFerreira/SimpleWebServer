import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Webserver {

    public static void main(String[] args) throws IOException {

        int portNumber = Integer.parseInt("8000");

        //Create a server socket to listen for incoming connections
        ServerSocket serverSocket = new ServerSocket(portNumber);
        //Accept a client connection
        Socket clientSocket = serverSocket.accept();

        //Set up input stream to read client request
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String[] request = in.readLine().split(" ");
        File imageFile = new File("Resources"+ request[1]);

        //Prepare the HTTP response header for the image
        String imageHeader = "HTTP/1.0 200 Document Follows\r\n" +
                "Content-Type: image/png \r\n" +
                "Content-Length: " + imageFile.length() + " \r\n" +
                "Link: rel=icon type=image/png href=/Users/carloscasaleiro/AdC/shared/simplewebserver/SimpleWebServer/Resources/favicon.ico\r\n" +
                "\r\n";
        //If the requested image file exists
        if (imageFile.exists()) {

            //Set up output stream to send the HTTP response header
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeBytes(imageHeader);

            //Read and send the image file content in chunks
            byte[] buffer = new byte[1024];
            BufferedInputStream bin = new BufferedInputStream(Files.newInputStream(imageFile.toPath()));
            int bytesRead = 0;
            while ((bytesRead = bin.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            //Close streams and sockets
            in.close();
            clientSocket.close();
            serverSocket.close();
        }
    }
}
