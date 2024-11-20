package messenger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientWindow extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 15000;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private final JTextArea textArea;
    private final JTextField textField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientWindow().start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ClientWindow() {
        setTitle("Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        textArea = new JTextArea(20, 30);
        textArea.setEditable(false);
        textArea.setBackground(new Color(235, 236, 240));
        textArea.setFont(new Font("Arial", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        textField = new JTextField(50);
        textField.setPreferredSize(new Dimension(100, 30));
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setBackground(new Color(230, 230, 230));

        JButton sendButton = new JButton(">");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        sendButton.addActionListener(e -> sendMessage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(textField, LEFT_ALIGNMENT);
        buttonPanel.add(sendButton, RIGHT_ALIGNMENT);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void start() throws IOException {
        createConnection(SERVER_ADDRESS, SERVER_PORT);

        new Thread(new IncomingMessageListener()).start();
    }

    private void sendMessage() {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            out.println(message);
            textField.setText("");
        }
    }

    private class IncomingMessageListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null &&
                        !serverMessage.equals("Connection closed") &&
                        !serverMessage.equals("Registration failed")) {
                    textArea.append(serverMessage + "\n");
                }
                textArea.append(serverMessage + "\n");
                System.out.println(serverMessage);
                closeConnection();
            } catch (IOException e) {
                throw new RuntimeException("Connection exception");
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
