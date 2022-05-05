import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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


public class RemtClient {

    static final String basePath = "C:\\remtClient\\";

    private static Document generateDOM(String xml) throws Exception {
        // Creazione del DOM
        InputSource is = new InputSource(new StringReader(xml));
        DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = (Document) dBuilder.parse(is);
        ((Node) doc.getDocumentElement()).normalize();
        return doc;
    } // generateDOM

    public static void send(Socket socket, String token, String fileName, String filePath, File file) throws IOException{
        // Length of the file
        int length = (int) file.length();

        // Byte buffer
        byte[] buffer = new byte[length];

        // Setting up channels to transfer data
        InputStream in = null;
        OutputStream out = socket.getOutputStream();

        try{
            in = new FileInputStream(file);
            
            // Variables 
            int bytesRead;

            //-------------------------
            // Send file 
            //------------------------- 

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            //-------------------------
            // End of file transfer
            //-------------------------
            in.close();
        }catch(IOException e){
            System.out.println("Error: " + e);
        }
        
        
    } // send
    
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
        File file = null;
        int length = 0;
        String operation = "", fileName="", filePath="", newFileName= "", newFilePath="", token="", status, code, req, res;

        //XML template
        String xmlTemplate = "<request><token>%s</token><operation>%s</operation><fileName>%s</fileName><filePath>%s</filePath><length>%s</length></request>";

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
                filePath = filePath != null && !filePath.isEmpty() ? filePath + "\\" : "";

                // Getting the file to send from the file system
                file = new File(basePath + filePath + fileName);

                // Getting length of the file and creating the buffer
                length = (int) file.length();

                //Ask for file name
                System.out.println("Insert new file name (if you want to change it)");
                newFileName = stdIn.readLine();

                //Ask for file path
                System.out.println("Insert new file path (if you want to change it)");
                newFilePath = stdIn.readLine();

                // Checking if they are empty
                newFileName = newFileName != null && !newFileName.isEmpty() ? newFileName : fileName;
                newFilePath = newFilePath != null && !newFilePath.isEmpty() ? newFilePath : "";
            }

            //Create the XML request, and send it to the server
            req = String.format(xmlTemplate, token, operation, newFileName, newFilePath, ""+length);
            outT.println(req);

            //Get the response from the server
            if(!operation.equals("STOP")){
                res = inT.readLine();

                // Interprete the response
                doc = generateDOM(res);
                status = doc.getElementsByTagName("status").item(0).getTextContent();

                //If the operation is successful
                if(status.equals("allowed")){
                    switch(operation){
                        case "send":
                            //Send the file to the server
                            send(socket, token, fileName, filePath, file);
                            break;
                        case "receive":
                            //Receive the file from the server
                            //receive(socket, token, fileName, filePath);
                            break;
                    } // switch
                }else{
                    code = doc.getElementsByTagName("code").item(0).getTextContent();
                    System.out.println("Error code: " + code);
                } // if
            }
        }

        //------------------------------------------------------
        //End of comunication with the server
        //------------------------------------------------------

        //Close the socket
        in.close();
        out.close();
        socket.close();
    } // Run the client
     
}
