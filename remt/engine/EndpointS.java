package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class EndpointS extends Thread {
    // The socket that will be used to communicate with the client
    static Socket socket;

    //  XML template
    String xmlTemplate = "<response><number>%s</number><status>%s</status><code>%s</code></response>";

    // Base Path for the server
    static String basePath = "C:\\remtServer\\";

    public EndpointS(Socket socket) {
        EndpointS.socket = socket;
    } // Return the socket itself, via constructor

    private static Document generateDOM(String xml) throws Exception {
        // Creazione del DOM
        InputSource is = new InputSource(new StringReader(xml));
        DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(is);
        ((Node) doc.getDocumentElement()).normalize();
        return doc;
    } // generateDOM

    public static String[] send(String token, String fileName, String filePath) throws IOException{
        String[] result = new String[3];
        result[0] = token;

        //Stream for binary data and file transfer
        OutputStream out = null;
        try{
            out = new FileOutputStream(basePath + filePath + fileName);
            System.out.println(basePath + filePath + fileName);
        }catch(IOException ex){
            result[1] = "insuccess";
            result[2] = "0004";
        }
        InputStream in = socket.getInputStream();

        // Bytes for store info to be sent
        byte[] buffer = new byte[1024 * 16];

        //-------------------------
        // Send file 
        //------------------------- 

        System.out.println("Sending...");

        //-------------------------
        // End of file transfer
        //-------------------------

        out.close();

        return result;

    } // send

    public static void receive(String token, String fileName, String filePath, int length) throws IOException{
        if(length > 0){
            new File(basePath + filePath).mkdirs();
            
            // Stream for binary data and file transfer
            OutputStream out = new FileOutputStream(basePath + filePath + fileName);
            InputStream in = socket.getInputStream();

            // Variables 
            int bytesRead;

            // Bytes for store info to be sent
            byte[] buffer = new byte[length];

            //-------------------------
            // Send file 
            //------------------------- 

            while(length > 0 &&(bytesRead = in.read(buffer)) > 0 ){
                length -= bytesRead;
                out.write(buffer, 0, bytesRead);

                if (bytesRead < 1024) {
                    break;
                }

            } // while

            //-------------------------
            // End of file transfer
            //-------------------------
            

            out.close();
        } // if
    } // receive

    public void run() {
        try {
            //Client specicications
            String ipAddr = socket.getInetAddress().getHostAddress();
            int portNum = socket.getPort();

            // Get the connection socket information 
            System.out.println("New connection: [" + ipAddr + ":" + portNum + "]");

            // Stream for text to accord with the client
            PrintWriter outT = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inT = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Variables for communication and the file transfer
            Document doc;
            String operation = "", fileName="", filePath="", token, status, code, req, res;
            int length = 0;

            //------------------------------------------------------------------
            //  Communication with the client 
            //------------------------------------------------------------------
            
            // While the client is connected, wait for it
            while(!operation.equals("STOP")){
                // Variable reset
                token = "";
                status = "";
                status = "";
                code = "";

                // Read the sent operation to perform
                req = inT.readLine();

                // Interpretation of the request
                doc = generateDOM(req);
                operation = ((org.w3c.dom.Document) doc).getElementsByTagName("operation").item(0).getTextContent();
                token = ((org.w3c.dom.Document) doc).getElementsByTagName("token").item(0).getTextContent();
                fileName = ((org.w3c.dom.Document) doc).getElementsByTagName("fileName").item(0).getTextContent();
                filePath = ((org.w3c.dom.Document) doc).getElementsByTagName("filePath").item(0).getTextContent();
                length = Integer.parseInt(((org.w3c.dom.Document) doc).getElementsByTagName("length").item(0).getTextContent());

                filePath = filePath != null && !filePath.isEmpty() ? filePath + "\\" : "";

                // Handle the request
                switch(operation){
                    case "STOP":
                        System.out.println("Client [" + ipAddr + ":" + portNum + "] has disconnected.");

                        // Generate the response, and send it to the client
                        res = String.format(xmlTemplate, token, status, code);
                        outT.println(res);
                        break;
                    case "send": 
                        // Receive the file he sends

                        // Generate the response, and send it to the client
                        res = String.format(xmlTemplate, token, "allowed", "0000");
                        outT.println(res);

                        try{
                            // All the logic for the file transfer 
                            receive(token, fileName, filePath, length);

                            if(length > 0)System.out.println("File received successfully from [" + ipAddr + ":" + portNum + "]");
                        }catch(Exception ex){
                            System.out.println("Error: " + ex.getMessage());
                        }
                        break;
                    case "receive":
                        // Send the file he require

                        // Generate the response, and send it to the client
                        res = String.format(xmlTemplate, token, "allowed", "0000");
                        outT.println(res);

                        // All the logic for the file transfer

                        break;

                    default:
                        // Thow error to the client
                        status = "denied";
                        code = "0003";

                        // Generate the response, and send it to the client
                        res = String.format(xmlTemplate, token, status, code);
                        outT.println(res);

                        break;
                }// switch

            } // while

            //------------------------------------------------------------------
            //  End of communication with the client
            //------------------------------------------------------------------


            //Close the socket
            outT.close();
            inT.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }// Run the thread of the server

}

//https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets