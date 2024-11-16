package messenger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket server = null;

    public void listenSocket() {
        Socket client = null;

        try {
            server = new ServerSocket(15000);
        } catch (IOException e) {
            System.out.println("Could not create ServerSocket");
            System.exit(-1);
        }
        System.out.println("Server ready on port: " + server.getLocalPort());

        while (true) {
            try {
                client = server.accept();
            } catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }
            (new ServerThread(client)).start();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listenSocket();
    }
}
