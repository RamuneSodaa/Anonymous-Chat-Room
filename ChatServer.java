import java.awt.Frame;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class ChatServer extends Frame {

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    /**
     * Constructor to initialize the server UI.
     */
    public ChatServer() {
        add(new Label("Server is running"));
        setVisible(true);
        setSize(200, 100);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * Starts the chat server and listens for incoming client connections.
     */
    public void startServer() {
        boolean isRunning = false;
        try {
            // Bind the server to port 8888
            serverSocket = new ServerSocket(8888);
            isRunning = true;
            System.out.println("Server started");

            // Continuously listen for incoming client connections
            while (isRunning) {
                socket = serverSocket.accept(); // Accept a connection from a client
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start(); // Handle client in a separate thread
                clients.add(clientHandler); // Add client to the list of active clients
            }
        } catch (BindException e) {
            System.err.println("Port 8888 is already in use.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles communication with a connected client.
     */
    class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private boolean isConnected = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                isConnected = true;
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Sends a message to the client.
         * 
         * @param message The message to send
         */
        public void sendMessage(String message) {
            try {
                outputStream.writeUTF(message);
                outputStream.flush();
            } catch (SocketException e) {
                // Remove the client if a socket exception occurs
                clients.remove(this);
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (isConnected) {
                    // Read message from the client
                    String receivedMessage = inputStream.readUTF();

                    // Broadcast the message to all connected clients
                    for (ClientHandler client : clients) {
                        client.sendMessage(receivedMessage);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close all resources upon client disconnection
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
