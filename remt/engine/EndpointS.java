package engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class EndpointS extends Thread {
    //The socket that will be used to communicate with the client
    Socket socket;

    public EndpointS(Socket socket) {
        this.socket = socket;
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

    public static void send(){

    }

    public static void receive(){

    }

    public void run() {
        try {
            System.out.println("New connection: " + socket);
            //Client specicications
            String ipAddr = socket.getInetAddress().getHostAddress();
            int portNum = socket.getPort();

            // Stream for text to accord with the client
            PrintWriter outT = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inT = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Stream for binary data and file transfer
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            byte[] bytes = new byte[1024];

            //Variables for communication and the file transfer
            Document doc;
            String operation = "", fileName, filePath, number, status, code, req, res;
            boolean ack = false;

            //XML template
            String xmlTemplate = "<response><number>%s</number><ack>%s</ack><status>%s</status><code>%s</code></response>";

            //------------------------------------------------------------------
            //  Communication with the client 
            //------------------------------------------------------------------
            
            // While the client is connected, wait for it
            while(!operation.equals("STOP")){
                // Variable reset
                number = "";
                status = "";
                status = "";
                code = "";

                // Read the sent operation to perform
                req = inT.readLine();

                // Interpretation of the request
                doc = generateDOM(req);
                operation = ((org.w3c.dom.Document) doc).getElementsByTagName("operation").item(0).getTextContent();
                number = ((org.w3c.dom.Document) doc).getElementsByTagName("number").item(0).getTextContent();

                // Handle the request
                switch(operation){
                    case "STOP":
                        System.out.println("Client [" + ipAddr + ":" + portNum + "] has disconnected.");
                        break;
                    case "send": 
                        // Send the file he require
                        break;
                    case "receive":
                        // Receive the file he sends
                        break;
                    default:
                        // Thow error to the client
                        ack = false;
                        status = "denied";
                        code = "0003";
                        break;
                }

                // Generate the response
                res = String.format(xmlTemplate, number, ack, status, code);

                // Send the response to the client
                outT.println(res);
            }

            //------------------------------------------------------------------
            //  End of communication with the client
            //------------------------------------------------------------------


            //Close the socket
            outT.close();
            inT.close();
            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }// Run the thread of the server
}

//https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets