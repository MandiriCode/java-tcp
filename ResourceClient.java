import java.io.*;
import java.net.*;
import java.util.*;

public class ResourceClient {
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        System.out.print("192.168.172.206: ");
        String serverIP = keyboard.nextLine();

        final int PORT = 1234;

        try {
            Socket socket = new Socket(serverIP, PORT);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Print initial server message
            String serverMsg;
            while ((serverMsg = input.readLine()) != null && !serverMsg.isEmpty()) {
                System.out.println(serverMsg);
            }

            String userInput;
            do {
                System.out.print("> ");
                userInput = keyboard.nextLine();
                output.println(userInput);

                String response = input.readLine();
                System.out.println("Server: " + response);
            } while (!userInput.equals("0"));

            socket.close();
        } catch (IOException e) {
            System.out.println("Unable to connect to server.");


        }
    }
}