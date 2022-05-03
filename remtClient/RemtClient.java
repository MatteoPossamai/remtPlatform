import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public class RemtClient {

    private static Document generateDOM(String xml) throws Exception {
        // Creazione del DOM
        InputSource is = new InputSource(new StringReader(xml));
        DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(is);
        ((Node) doc.getDocumentElement()).normalize();
        return doc;
    } // generateDOM
    
    public static void main(String[] args) throws Exception {
        //Inizialize socket variables
        String host = "localhost";
        int port = 8080;

        // Create a socket to connect to the server
        Socket socket = new Socket(host, port);

        //Stream for data from keyboard and send information to server
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter outT = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader inT = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Stream for binary data and file transfer
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        //Variables for communication and the file transfer
        Document doc;
        String operation = "", fileName="", filePath="", number="", error, status, code, req, res;
        boolean ack = false;

        //XML template
        String xmlTemplate = "<request><number>%s</number><operation>%s</operation><file-name>%s</file-name><file-path>%s</file-path></request>";

        //------------------------------------------------------
        // Send what you want to do to the server
        //------------------------------------------------------
        
        //While I am connected to the server
        while(!operation.equals("STOP")){
            //Ask for operation 
            System.out.println("What do you want to do? (STOP, send, receive)");
            operation = stdIn.readLine();

            if(!operation.equals("STOP")){
                //Ask for file name
                System.out.println("Insert file name");
                fileName = stdIn.readLine();

                //Ask for file path
                System.out.println("Insert file path");
                filePath = stdIn.readLine();
            }

            //Create the XML request, and send it to the server
            req = String.format(xmlTemplate, number, operation, fileName, filePath);
            outT.println(req);

            //Get the response from the server
            if(!operation.equals("STOP")){
                res = inT.readLine();

                //Interpret the response
                doc = generateDOM(res);
                System.out.println(((org.w3c.dom.Document) doc).getElementsByTagName("code").item(0).getTextContent());
            }
        }

        //------------------------------------------------------
        //End of comunication with the server
        //------------------------------------------------------

        //Close the socket
        in.close();
        out.close();
        socket.close();
    }
    
}
