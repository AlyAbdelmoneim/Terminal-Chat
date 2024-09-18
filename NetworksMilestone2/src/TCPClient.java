import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java TCPClient <serverName> <port> <name>");
            return;
        }

        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        String clientName = args[2]; // Get client's name from command line

        Scanner scanner = new Scanner(System.in);

        // Wait for user to type "CONNECT"
        System.out.println("Type 'CONNECT' to start the chat:");
        String userInput = scanner.nextLine();
        while (!"CONNECT".equalsIgnoreCase(userInput)) {
            System.out.println("Invalid input. Please type 'CONNECT' to start the chat:");
            userInput = scanner.nextLine();
        }

        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            // Send CONNECT message to server
            out.writeUTF("CONNECT");

            // Wait for confirmation from server
            String confirmation = in.readUTF();
            if (!"CONNECTED".equals(confirmation)) {
                System.out.println("Failed to connect to server.");
                client.close();
                return;
            }

            // Send client name to server
            out.writeUTF(clientName);

            // Read server name from server
            String serverIdentity = in.readUTF();

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String response = in.readUTF();
                        System.out.println(serverIdentity + " says: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed by server.");
                }
            });

            readThread.start();

            while (true) {
                System.out.print(clientName + " is typing: "); // Display client's name
                String message = scanner.nextLine();
                out.writeUTF(message);
                if (message.equalsIgnoreCase("CLOSE")) {
                    break;
                }
            }

            client.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}