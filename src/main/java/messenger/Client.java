package messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final int PORT = 15000;
    private static final String ADDRESS = "localhost";
    private static Socket socket = null;
    private static BufferedReader in = null;
    private static PrintWriter out = null;

    public static void main(String[] args) {

        createConnection(ADDRESS, PORT);
        System.out.println("Waiting for registration");

        new Thread(new MessageListener()).start();

        String message;
        while (!socket.isClosed()) {
            message = SCANNER.nextLine();
            out.println(message);
        }
    }


    private static class MessageListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                // serverMessage = in.readLine();
                while ((serverMessage = in.readLine()) != null &&
                        !serverMessage.equals("Connection closed") &&
                        !serverMessage.equals("Registration failed")) {
                    System.out.println(serverMessage);
                }
//                do {
//                    System.out.println(serverMessage);
//                } while ((serverMessage = in.readLine()) != null &&
//                        !serverMessage.equals("Connection closed") &&
//                        !serverMessage.equals("Registration failed")
//                );
                System.out.println(serverMessage);
                closeConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void createConnection(String address, int port) {
        System.out.println("Creating connection");
        try {
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Connection created");
    }

    private static void closeConnection() {
        System.out.println("Closing connection");
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Connection closed");
    }
}
