import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        String hostName = "localhost"; // server hostname
        int portNumber = 80085; // server port number
        
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine(); // get user input for username

        try (
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(username + ": " + userInput); // send user input with username to server
                System.out.println(in.readLine()); // print response from server
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Could not connect to " + hostName + " on port " + portNumber);
            System.exit(1);
        }
    }
}
