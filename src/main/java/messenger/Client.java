package messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static Socket socket = null;
    private static BufferedReader in = null;
    private static PrintWriter out = null;

    //private static String name;

    public static void main(String[] args) {
        String address = "localhost";
        int port = 15000;

        createConnection(address, port);
        System.out.println("Waiting for registration");

        while (!socket.isClosed()) {
            String line = receiveLines(1).get(0);
            System.out.println("Message from server received");
            System.out.println(line);
            if (line.equals("Registration failed") || line.equals("Connection closed")) {
                closeConnection();
            } else {
                String answer = SCANNER.nextLine();
                out.println(answer);
                System.out.println("Answer to server send");
            }
        }
    }

    private static ArrayList<String> receiveLines(int linesNumber) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < linesNumber; i++) {
            try {
                String line = in.readLine();
                lines.add(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return lines;
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
