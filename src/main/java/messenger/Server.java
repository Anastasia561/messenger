package messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Server {
    private static final int PORT = 15000;
    private ServerSocket server;
    private static final HashSet<ClientHandler> clients = new HashSet<>();

    public void listenSocket() {
        Socket client = null;

        try {
            server = new ServerSocket(PORT);
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
            ClientHandler clientHandler = new ClientHandler(client);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    private static class ClientHandler implements Runnable {
        private static final Engine engine = new Engine();
        private final Socket socket;
        private BufferedReader in = null;
        private PrintWriter out = null;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("To connect: [serverName clientName]");

                String line = getLine();
                if (engine.checkForRegistration(line)) {
                    clientName = line.split(" ")[1];
                    System.out.println("Registering client: " + clientName);
                    engine.registerClient(clientName, socket.getLocalPort());

                    out.println("Registration succeed ");

                    sendMessageToSelected(clientName + " joined",
                            getClientsByName(getNamesWithoutExcluded(new String[]{clientName})));


                    out.println("List of connected clients: " + engine.getConnectedClients() + " ");
                    out.println("Instruction");
                    out.println("[all] -> to send message to all connected users");
                    out.println("[names] -> to send message to specified users");
                    out.println("[exclude] -> to send message to all users except specified ones");
                    out.println("[banned] -> get list of banned phrases");
                    out.println("[exit] -> to quit");

                    while (!socket.isClosed()) {
                        String command = getLine();
                        if (command.equals("exit")) {
                            System.out.println(clientName + " left");

                            sendMessageToSelected(clientName + " left",
                                    getClientsByName(getNamesWithoutExcluded(new String[]{clientName})));

                            clients.remove(this);
                            out.println("Connection closed");
                            socket.close();
                            engine.deleteRegisteredClient(clientName);
                        } else {
                            processCommand(command, clientName);
                        }
                    }

                } else {
                    System.out.println("Registration failed");
                    out.println("Registration failed");
                    socket.close();
                }

            } catch (IOException e) {
                System.out.println("Connection error");
            }
        }

        private void processCommand(String command, String clientName) {
            switch (command) {
                case "names": {
                    out.println("Specify names to which you want message to be send");
                    String[] names = getLine().split(" ");
                    //send to selected
                    out.println("Enter message: ");
                    String message = clientName + ": " + getLine();
                    sendMessageToSelected(message, getClientsByName(names));

                    System.out.println("Sending message of " + clientName + " to " + Arrays.toString(names));
                    out.println("Message was send to " + Arrays.toString(names));
                    break;
                }
                case "exclude": {
                    out.println("Specify excluded people (your message will not be send to)");
                    String[] excludedNames = (getLine() + " " + clientName).split(" ");

                    //send without excluded
                    out.println("Enter message: ");
                    String message = clientName + ": " + getLine();
                    String[] names = getNamesWithoutExcluded(excludedNames);

                    sendMessageToSelected(message, getClientsByName(names));

                    System.out.println("Sending message of " + clientName + " to " + Arrays.toString(names));
                    out.println("Message was send to " + Arrays.toString(names));
                    break;
                }
                case "all": {
                    //process names
                    out.println("Enter message: ");
                    String message = clientName + ": " + getLine();
                    String[] names = getNamesWithoutExcluded(new String[]{clientName});
                    sendMessageToSelected(message, getClientsByName(names));
                    System.out.println("Sending message of " + clientName + " to " + Arrays.toString(names));
                    out.println("Message was send to " + Arrays.toString(names));
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

        private void sendMessageToSelected(String message, HashSet<ClientHandler> selected) {
            for (ClientHandler clientHandler : selected) {
                clientHandler.out.println(message);
            }
        }


        private HashSet<ClientHandler> getClientsByName(String[] names) {
            HashSet<ClientHandler> selected = new HashSet<>();
            for (ClientHandler clientHandler : clients) {
                for (String name : names) {
                    if (clientHandler.clientName.equals(name)) {
                        selected.add(clientHandler);
                    }
                }
            }
            return selected;
        }

        private String[] getNamesWithoutExcluded(String[] excludedNames) {
            ArrayList<String> names = new ArrayList<>();

            for (ClientHandler clientHandler : clients) {
                if (!(Arrays.stream(excludedNames).toList().contains(clientHandler.clientName))) {
                    names.add(clientHandler.clientName);
                }
            }
            return names.toArray(new String[]{});
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.listenSocket();
    }
}
