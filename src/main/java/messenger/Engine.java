package messenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Engine {
    private final String clientFilePath = createFilePath();

    public ArrayList<String> getConnectedClients() {
        ArrayList<String> clients = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(clientFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String clientName = line.split(" ")[0];
                clients.add(clientName);
            }
        } catch (IOException e) {
            System.out.println("Error while reading from file");
        }
        return clients;
    }


    public boolean checkForRegistration(String line) {
        return line.split(" ")[0].equals("srv3");
    }

    public void registerClient(String name, int port) {
        System.out.println("register client:  " + name);
        try (FileWriter writer = new FileWriter(clientFilePath, true)) {
            writer.write(name + " " + port + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createFilePath() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM_HH-mm");
        LocalDateTime now = LocalDateTime.now();
        return "src/main/resources/clients_" + dtf.format(now) + ".txt";
    }
}
