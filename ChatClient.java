import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * A simple chat client that connects to a server and sends/receives messages.
 */
public class ChatClient extends Frame {
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private DataInputStream inputStream = null;
    private TextArea chatDisplay = new TextArea();
    private TextField messageInput = new TextField();

    public static void main(String[] args) {
        new ChatClient().launchFrame();
    }

    /**
     * Initializes the chat client window and its components.
     */
    public void launchFrame() {
        // Set up the GUI layout
        add(chatDisplay, BorderLayout.CENTER);
        add(messageInput, BorderLayout.SOUTH);
        setSize(400, 400);  // Larger size for better visibility
        setVisible(true);
        setLocationRelativeTo(null);
        setTitle("Anonymous Chat Room");
        pack();
        
        // Ensure the connection is closed when the window is closed
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
        
        // Establish connection to the server
        connect();

        // Listen for the enter key in the message input field to send a message
        messageInput.addActionListener(new SendMessageListener());
    }

    /**
     * Connects to the chat server.
     */
    public void connect() {
        try {
            // Connect to the server at localhost (127.0.0.1) on port 8888
            socket = new Socket("127.0.0.1", 8888);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            
            // Start a thread to listen for incoming messages
            new Thread(new MessageReceiver()).start();
        } catch (SocketException e) {
            chatDisplay.setText("Server is not running!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A thread that listens for incoming messages from the server and displays them.
     */
    class MessageReceiver implements Runnable {
        private boolean isRunning = true;

        @Override
        public void run() {
            try {
                while (isRunning) {
                    String message = inputStream.readUTF(); // Read the incoming message
                    chatDisplay.append(message + "\n");     // Display it in the chat area
                }
            } catch (SocketException e) {
                System.out.println("Client disconnected.");
            } catch (EOFException e) {
                System.out.println("Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the connection to the server.
     */
    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listens for the enter key in the message input field to send a message to the server.
     */
    class SendMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageInput.getText(); // Get the text from the input field
            messageInput.setText(""); // Clear the input field after sending

            try {
                outputStream.writeUTF(message); // Send the message to the server
                outputStream.flush();
            } catch (NullPointerException e1) {
                chatDisplay.setText("Server is not running!");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
