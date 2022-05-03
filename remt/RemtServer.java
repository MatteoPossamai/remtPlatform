//Project packages
import engine.EndpointS;

//Public packages
import java.io.IOException;
import java.net.ServerSocket;

public class RemtServer {
    public static void main(String[] args) throws IOException {
        //Select the port that the server will listen to
        int port = 8080;

        //Create a new server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            //When socket is created, the server is ready to listen
            System.out.println("Waiting for connections...");

            //When a client connects, create a new thread
            while (true){
                new EndpointS(serverSocket.accept()).start();
            }

        }catch (Exception e){
            //If an error occurs, print it
            System.out.println("Error: " + e);
        }
    }
}