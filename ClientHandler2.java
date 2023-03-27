import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatServer server;
    private final PrintWriter out;
    private String username;
    private HashSet<String> groups;

    public ClientHandler(ChatServer server, Socket socket) throws IOException {
        this.server = server;
        this.clientSocket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.groups = new HashSet<>();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out.println("Welcome to the chat room! Please enter your username:");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (username == null) {
                    // set the username for the client
                    setUsername(inputLine);
                    server.addUser(this);
                    out.println("Hi " + username + "! Type '@<username>' to chat with a specific person or '#<group>' to join a group chat.");
                    continue;
                }
                if (inputLine.startsWith("@")) {
                    // private message
                    String[] parts = inputLine.split(" ", 2);
                    if (parts.length != 2) {
                        out.println("Invalid private message format. Usage: '@<username> <message>'");
                        continue;
                    }
                    String recipient = parts[0].substring(1);
                    String message = parts[1];
                    server.sendPrivateMessage(username, recipient, message);
                } else if (inputLine.startsWith("#")) {
                    // group chat
                    String[] parts = inputLine.split(" ", 2);
                    if (parts.length != 2) {
                        out.println("Invalid group message format. Usage: '#<group> <message>'");
                        continue;
                    }
                    String groupName = parts[0].substring(1);
                    String message = parts[1];
                    server.sendGroupMessage(username, groupName, message);
                } else {
                    // public message
                    server.broadcastMessage(username, inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // remove the user from the server and close the socket
            server.removeUser(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username.isEmpty()) {
            this.username = UUID.randomUUID().toString().substring(0, 8); // generate a random username
        } else {
            this.username = username;
        }
    }

    public HashSet<String> getGroups() {
        return groups;
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public void removeGroup(String group) {
        groups.remove(group);
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
