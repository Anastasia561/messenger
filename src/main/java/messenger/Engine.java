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

    public void deleteRegisteredClient(String clientName) {
        ArrayList<String> oldRecords = readFromFile(clientFilePath);
        writeToFile(clientFilePath, "", false);
        for (String record : oldRecords) {
            if (!(record.split(" ")[0].equals(clientName))) {
                writeToFile(clientFilePath, record + "\n", true);
            }
        }
    }

    public ArrayList<String> getConnectedClients() {
        ArrayList<String> records = readFromFile(clientFilePath);
        ArrayList<String> names = new ArrayList<>();
        for (String record : records) {
            names.add(record.split(" ")[0]);
        }
        return names;
    }

    public boolean checkForRegistration(String line) {
        return line.split(" ")[0].equals("srv3");
    }

    public void registerClient(String name, int port) {
        System.out.println("register client:  " + name);
        writeToFile(clientFilePath, name + " " + port + "\n", true);
    }

    private String createFilePath() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM_HH-mm");
        LocalDateTime now = LocalDateTime.now();
        return "src/main/resources/clients_" + dtf.format(now) + ".txt";
    }

    private ArrayList<String> readFromFile(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private void writeToFile(String filePath, String line, boolean isAppending) {
        try (FileWriter writer = new FileWriter(filePath, isAppending)) {
            writer.write(line);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
