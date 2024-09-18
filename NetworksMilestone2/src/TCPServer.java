import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TCPServer <port> <serverName>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String serverName = args[1]; // Get server's name from command line

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");

            Socket server = serverSocket.accept();
            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            OutputStream outToClient = server.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToClient);
            InputStream inFromClient = server.getInputStream();
            DataInputStream in = new DataInputStream(inFromClient);
            Scanner scanner = new Scanner(System.in);

            // Read CONNECT message
            String connectMessage = in.readUTF();
            if (!"CONNECT".equalsIgnoreCase(connectMessage)) {
                System.out.println("Invalid connect message. Closing connection.");
                server.close();
                return;
            }

            // Send confirmation to client
            out.writeUTF("CONNECTED");

            // Read client name from client
            String clientName = in.readUTF();
            // Send server name to client
            out.writeUTF(serverName);

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String response = in.readUTF();
                        System.out.println(clientName + " says: " + response);
                        if ("CLOSE".equalsIgnoreCase(response)) {
                            System.out.println("Client requested to close the connection.");
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed by client.");
                }
            });

            readThread.start();

            while (true) {
                System.out.print("\n" + serverName + " is typing: "); // Display server's name
                String message = scanner.nextLine();
                out.writeUTF(message);
                if (message.equalsIgnoreCase("CLOSE")) {
                    break;
                }
            }

            server.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}