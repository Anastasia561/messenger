package messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class ServerThread extends Thread {
    private Socket socket;

    private static Engine engine = new Engine();

    private static BufferedReader in = null;
    private static PrintWriter out = null;

    public ServerThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        String thread_ID = Long.toString(currentThread().getId()); // DEBUG
        System.out.println("THREAD " + thread_ID + " starting"); // DEBUG


        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("To connect: serverName clientName");

            String line = getLine();
            if (engine.checkForRegistration(line)) {
                String clientName = line.split(" ")[1];
                System.out.println("Registering client: " + clientName);
                engine.registerClient(clientName, socket.getLocalPort());

                out.print("Registration succeed ");
                out.print("List of connected clients: " + engine.getConnectedClients() + " ");
                out.println("Instruction");
                //System.out.println("Connected clients " + engine.getConnectedClients());
                //add instruction
                while (!socket.isClosed()) {
                    String command = getLine();
                    if (command.equals("exit")) {
                        System.out.println(clientName + " left");
                        closeSocket("Connection closed", thread_ID);
                    } else {
                        processCommand(command, clientName);
                    }
                }

            } else {
                System.out.println("Registration failed");
                closeSocket("Registration failed", thread_ID);
            }

        } catch (IOException e) {
            System.out.println("Connection error");
        }
    }

    private void closeSocket(String message, String thread_ID) {
        out.println(message);
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("THREAD " + thread_ID + " exiting");
    }

    private void processCommand(String command, String clientName) {
        switch (command) {
            case "names": {
                out.println("Specify names to which you want message to be send");
                String[] names = getLine().split(" ");
                System.out.println("Sending message of " + clientName + " to " + Arrays.toString(names));
                out.println("Message was send to " + Arrays.toString(names));
                break;
            }
            case "exclude": {
                out.println("Specify excluded people (your message will not be send to)");
                String[] names = getLine().split(" ");
                //process names
                System.out.println("Sending message of " + clientName + " to ...");
                out.println("Message was send to ...");
                break;
            }
            case "all": {
                //process names
                System.out.println("Sending message of " + clientName + " to ...");
                out.println("Message was send to ...");
                break;
            }
            case "banned": {
                System.out.println("Sending banned phrases to " + clientName);
                out.println("...");
            }
        }
    }

    private String getLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
