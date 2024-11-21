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
    private static final String CONFIG_PATH = "C:\\Users\\User\\JavaProjects\\Messenger\\src\\main\\resources\\config.txt";
    private static final FileProcessor PROCESSOR = new FileProcessor();
    private static int port;
    private static String serverName;
    private ServerSocket server;
    private static ArrayList<String> bannedWords;
    private static final HashSet<ClientHandler> clients = new HashSet<>();

    public void listenSocket() {
        Socket client = null;
        loadConfig(CONFIG_PATH);

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("Could not create ServerSocket");
        }
        System.out.println("Server ready on port: " + server.getLocalPort());

        while (true) {
            try {
                client = server.accept();
            } catch (IOException e) {
                throw new RuntimeException("Accept failed");
            }
            ClientHandler clientHandler = new ClientHandler(client);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    private void loadConfig(String path) {
        ArrayList<String> configs = PROCESSOR.readFromFile(path);
        port = Integer.parseInt(configs.get(0));
        serverName = configs.get(1);
        configs.remove(0);
        configs.remove(0);
        bannedWords = configs;
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in = null;
        private PrintWriter out = null;
        private String clientName = "default";

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
                if (checkForRegistration(line)) {
                    String name = line.split(" ")[1];
                    while (checkNamesIfExist(new String[]{name}, false)) {
                        out.println("Client with name " + name + " already exists");
                        out.println("Try another name");
                        name = getLine();
                    }
                    clientName = name;
                    registerClient();
                    sendInstruction();
                    processCommands();

                } else {
                    out.println("Registration failed");
                    clients.remove(this);
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Connection exception");
            }
        }

        private void registerClient() {
            PROCESSOR.registerClient(clientName, socket.getLocalPort());

            out.println("Registration succeed");
            out.println("Welcome, " + clientName + " !");

            sendMessageToSelected(clientName + " joined",
                    getClientsByName(getNamesWithoutExcluded(new String[]{clientName})));
        }

        private void sendInstruction() {
            out.println("List of connected clients: " + PROCESSOR.getConnectedClients() + " ");
            out.println("-".repeat(30));
            out.println("Instruction");
            out.println("[all] -> to send message to all connected users (default)");
            out.println("[names] -> to send message to specified users");
            out.println("[exclude] -> to send message to all users except specified ones");
            out.println("[banned] -> to check whether phrase is banned");
            out.println("[exit] -> to quit");
            out.println("-".repeat(30));
        }

        private void processCommands() {
            while (!socket.isClosed()) {
                out.println("Waiting for command");
                String command = getLine();
                if (command.equals("exit")) {

                    sendMessageToSelected(clientName + " left",
                            getClientsByName(getNamesWithoutExcluded(new String[]{clientName})));

                    clients.remove(this);
                    out.println("Connection closed");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    PROCESSOR.deleteRegisteredClient(clientName);
                } else {
                    processCommand(command, clientName);
                }
            }
        }

        private boolean checkForRegistration(String line) {
            String[] data = line.split(" ");
            if (data.length == 2) {
                return line.split(" ")[0].equals(serverName);
            }
            return false;
        }

        private boolean checkForBannedPhrase(String message) {
            for (String bannedWord : bannedWords) {
                if (message.contains(bannedWord)) {
                    return true;
                }
            }
            return false;
        }

        private void processCommand(String command, String clientName) {
            switch (command) {
                case "exclude": {
                    out.println("Specify excluded people (your message will not be send to)");
                    String[] excludedNames = (getLine() + " " + clientName).split(" ");
                    if (checkNamesIfExist(excludedNames, true)) {
                        String[] names = getNamesWithoutExcluded(excludedNames);
                        if (names.length != 0) {
                            sentTo(names, "private from");
                        } else {
                            out.println("There are no other users to whom message could be send");
                        }
                    }
                    break;
                }
                case "all": {
                    String[] names = getNamesWithoutExcluded(new String[]{clientName});
                    if (checkNamesIfExist(names, true)) {
                        sentTo(names, "to all from");
                    }
                    break;
                }
                case "banned": {
                    out.println("Enter phrase you wan to check: ");
                    String phrase = getLine();
                    if (checkForBannedPhrase(phrase)) {
                        out.println("This phrase is banned");
                    } else {
                        out.println("This phrase is permitted to use");
                    }
                    break;
                }
                case "names": {
                    out.println("Specify names to which you want message to be send");
                    String[] names = getLine().split(" ");
                    if (checkNamesIfExist(names, true)) {
                        sentTo(names, "private from");
                    }
                    break;
                }
                default: {
                    out.println("Command was incorrect, using default one [all]");
                    String[] names = getNamesWithoutExcluded(new String[]{clientName});
                    if (checkNamesIfExist(names, true)) {
                        sentTo(names, "to all from");
                    }
                }
            }
        }

        private String getMessage(String prefix) {
            out.println("Enter message: ");
            return prefix + " " + clientName + ": " + getLine();
        }

        private void sentTo(String[] names, String prefix) {
            String message = getMessage(prefix);
            if (checkForBannedPhrase(message)) {
                out.println("Message will not be send, it contains banned phrase");
            } else {
                sendMessageToSelected(message, getClientsByName(names));

                out.println("Message was send to " + Arrays.toString(names));
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

        private boolean checkNamesIfExist(String[] names, boolean printMessage) {
            ArrayList<String> notMatched = new ArrayList<>();
            if (clients.size() == 1) {
                if (printMessage) {
                    out.println("There are no other users to whom message could be send");
                }
                return false;
            } else {
                for (String name : names) {
                    if (!clients.stream().map(e -> e.clientName).toList().contains(name) && !name.equals("default")) {
                        notMatched.add(name);
                    }
                }
                if (!notMatched.isEmpty()) {
                    if (printMessage) {
                        out.println(notMatched + " does not refer to any of the currently connected clients");
                    }
                    return false;
                }
                return true;
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listenSocket();
    }
}
