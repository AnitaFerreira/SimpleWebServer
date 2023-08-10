import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleWebServer {
    public static void main(String[] args) {
        //Port Configuration: indicating the port number on which the server will listen for incoming connections
        int port = 8000; // Specify the port number you want to use

        try {
            //This socket will be responsible for accepting incoming connections from clients
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                //Is created to handle communication with the connected client
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                //This HttpRequestHandler is presumably responsible for processing and responding to HTTP requests from the client
                HttpRequestHandler requestHandler = new HttpRequestHandler(clientSocket);
                requestHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//This class is responsible for handling incoming HTTP requests
//It has a constructor that takes a Socket object (representing a client connection) as a parameter
class HttpRequestHandler extends Thread {
    private Socket clientSocket;

    public HttpRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            //Set up input and output streams to communicate with the client
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            //Read the first line of the client's request
            String requestLine = reader.readLine();
            String[] requestParts = requestLine.split(" ");

            if (requestParts.length >= 2) {
                //Extract the HTTP method and the requested file name
                String method = requestParts[0];
                String requestedFile = requestParts[1].substring(1); // Remove leading slash
                //The code calls the serveFile method to send the requested
                if (method.equals("GET")) {
                    serveFile(requestedFile, writer);
                } else {
                    sendResponse(405, "Method Not Allowed", "Only GET requests are supported.", writer);
                }
            }

            //Flush and close streams and socket
            writer.flush();
            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace(); // Print any exception details for debugging
        }
    }

    private void serveFile(String fileName, PrintWriter writer) throws IOException {
        File file = new File(fileName);

        // Check if the file exists and is a regular file
        if (file.exists() && file.isFile()) {
            //Read the file and send it in the response
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            //It returns the appropriate MIME type for the response headers.
            sendResponse(200, "OK", getContentType(fileName), writer);

            //Read and send each line of the file's content
            while ((line = fileReader.readLine()) != null) {
                writer.println(line);
            }

            fileReader.close();
        } else {
            //If the file doesn't exist, send a "404 Not Found" response
            sendResponse(404, "Not Found", "File not found.", writer);
        }
    }

    //Method to send an HTTP response to the client
    private void sendResponse(int statusCode, String statusText, String content, PrintWriter writer) throws IOException {
        //Write the HTTP status line, including status code and status text
        writer.println("HTTP/1.1 " + statusCode + " " + statusText);
        writer.println("Content-Type: " + getContentType(content));
        writer.println();
        //Write the actual content of the response
        writer.println(content);
    }

    //Method to determine the Content-Type header based on the file's extension
    private String getContentType(String fileName) {
        //Check if the file has an ".html" extension
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
       //If the file has an unrecognized extension, default to "application/octet-stream"
        } else {
            return "application/octet-stream";
        }
    }
}