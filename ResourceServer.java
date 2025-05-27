import java.io.*;
import java.net.*;

public class ResourceServer {
    private static ServerSocket servSocket;
    private static final int PORT = 1234;

    public static void main(String[] args) throws IOException {
        try {
            servSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);
        } catch (IOException e) {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }

        Resource item = new Resource(1); // starting level = 1
        Producer producer = new Producer(item);
        producer.start();

        while (true) {
            Socket client = servSocket.accept();
            System.out.println("New client connected.");
            ClientThread handler = new ClientThread(client, item);
            handler.start();
        }
    }
}

class Resource {
    private int numResources;
    private final int MAX = 5;

    public Resource(int startLevel) {
        numResources = startLevel;
    }

    public synchronized int getLevel() {
        return numResources;
    }

    public synchronized int takeOne() {
        try {
            while (numResources == 0)
                wait();
            numResources--;
            notifyAll();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return numResources;
    }

    public synchronized int addOne() {
        try {
            while (numResources >= MAX)
                wait();
            numResources++;
            notifyAll();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return numResources;
    }
}

class Producer extends Thread {
    private Resource item;

    public Producer(Resource resource) {
        item = resource;
    }

    public void run() {
        while (true) {
            try {
                int level = item.addOne();
                System.out.println("Producer added one. New level: " + level);
                Thread.sleep((int)(Math.random() * 5000));
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}

class ClientThread extends Thread {
    private Socket client;
    private Resource item;
    private BufferedReader input;
    private PrintWriter output;

    public ClientThread(Socket socket, Resource resource) {
        client = socket;
        item = resource;

        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
            output.println("Connected to Resource Server.");
            output.println("Commands:\n1 = Take resource\n2 = Show level\n0 = Exit");
        } catch (IOException e) {
            System.out.println("Error setting up I/O streams.");
        }
    }

    public void run() {
        String request = "";

        try {
            while ((request = input.readLine()) != null && !request.equals("0")) {
                switch (request) {
                    case "1":
                        int level = item.takeOne();
                        output.println("Resource taken. New level: " + level);
                        break;
                    case "2":
                        output.println("Current resource level: " + item.getLevel());
                        break;
                    default:
                        output.println("Invalid command.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection lost.");
        } finally {
            try {
                client.close();
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.out.println("Error closing client socket.");
            }
        }
    }
}