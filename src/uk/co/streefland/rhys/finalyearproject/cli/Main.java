package uk.co.streefland.rhys.finalyearproject.cli;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.storage.StorageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Created by Rhys on 09/09/2016.
 */
public class Main {

    private static Scanner sc = new Scanner(System.in);
    private static String localIp = "";
    private static String input = "";

    private static LocalNode localNode = null;

    public static void main(String[] args) {

        StorageHandler temp = new StorageHandler(new Configuration());

        if (!temp.doesSavedStateExist()) {
            System.out.println("Please enter your local IP address:");
            localIp = sc.nextLine();
        }

        try {
            localNode = new LocalNode(localIp);
            localIp = localNode.getNode().getSocketAddress().getHostName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!input.equals("q")) {
            System.out.println("Local IP: " + localIp + "\n");

            System.out.println("Please select an option:\n");

            System.out.println("(1) - Bootstrap to a node");
            System.out.println("(2) - Register");
            System.out.println("(3) - Log in");
            System.out.println("(4) - Send a broadcast message");
            System.out.println("(5) - Print routing table");
            System.out.println("(q) - Quit");

            input = sc.nextLine();

            switch (input) {
                case "1": bootstrap(); break;
                case "2": register(); break;
                case "3": login(); break;
                case "4": broadcast(); break;
                case "5": routingTable(); break;
                case "q": exit(); break;
                default : break;
            }
        }
    }

    private static void bootstrap() {

        boolean error = false;

        do {

            System.out.println("Please enter the IP of the node to bootstrap to:");
            input = sc.nextLine();

            /* Special case for first node in the network */
            if (input.equals("first")) {
                localNode.first();
                return;
            }

            try {
                error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(input), 12345));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (error) {
                System.out.println("Could not bootstrap to the specified IP - please try again\n");
            }
        } while (error);
    }

    private static void register() {

        boolean error = false;

        do {
            System.out.println("Please enter a username:");
            String username = sc.nextLine();

            System.out.println("Please enter a password:");
            String password = sc.nextLine();

            User user = new User(username, password);

            try {
                error = (localNode.getUsers().registerUser(user));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (error) {
                System.out.println("User already exists - please choose a different username\n");
            }
        } while (error);
    }

    private static void login() {

        boolean loggedIn = false;

        do {
            System.out.println("Please enter a username:");
            String username = sc.nextLine();

            System.out.println("Please enter a password:");
            String password = sc.nextLine();

            User user = new User(username, password);

            try {
                loggedIn = (localNode.getUsers().loginUser(user, password));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (loggedIn) {
                System.out.println("Logged in as " + username + " successfully!\n");
            } else {
                System.out.println("Invalid username/password - please try again\n");
            }
        } while (!loggedIn);
    }

    private static void broadcast() {

        System.out.println("Please enter a message to broadcast:");
        String message = sc.nextLine();

        try {
            localNode.message(message, localNode.getRoutingTable().getAllNodes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void routingTable() {
        System.out.println(localNode.getRoutingTable());
    }

    private static void exit() {
        localNode.shutdown();
    }
}
