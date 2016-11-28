package uk.co.streefland.rhys.finalyearproject.cli;

import uk.co.streefland.rhys.finalyearproject.core.IPTools;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command line interface for the framework
 */
class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static int localPort = 0;
    private static String localIp = "";
    private static String input = "";

    private static final String ipPattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):?(\\d{1,5})?";
    private static final Pattern p = Pattern.compile(ipPattern);

    private static LocalNode localNode = null;

    public static void main(String[] args) {

        IPTools ipTools = null;
        try {
            ipTools = new IPTools();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageHandler temp = new StorageHandler();

        if (!temp.doesSavedStateExist()) {
            System.out.println("Please enter your local IP address (and optionally port):");

            Matcher m = p.matcher(sc.nextLine());

            if (m.matches()) {
                if (m.group(1) != null) {
                    localIp = m.group(1);
                    if (m.group(2) != null) {
                        localPort = Integer.parseInt(m.group(2));
                    }
                }
            } else {
                System.err.println("Invalid IP address format");
                return;
            }
        }

        try {
            localNode = new LocalNode(ipTools, InetAddress.getByName(localIp), InetAddress.getByName(localIp), localPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!input.equals("q")) {
            System.out.println("Please select an option:\n");

            System.out.println("(1) - Bootstrap to a node");
            System.out.println("(2) - Register");
            System.out.println("(3) - Log in");
            System.out.println("(4) - Send a message");
            System.out.println("(5) - Print routing table");
            System.out.println("(q) - Quit");

            input = sc.nextLine();

            switch (input) {
                case "1":
                    bootstrap();
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    login();
                    break;
                case "4":
                    message();
                    break;
                case "5":
                    routingTable();
                    break;
                case "q":
                    exit();
                    break;
                default:
                    break;
            }
        }
    }

    private static void bootstrap() {

        String input = "";
        int port = 0;
        boolean error = false;

        System.out.println("Please enter the IP of the node to bootstrap to:");

        String nextLine = sc.nextLine();
        Matcher m = p.matcher(nextLine);

        if (m.matches()) {
            if (m.group(1) != null) {
                input = m.group(1);
                if (m.group(2) != null) {
                    port = Integer.parseInt(m.group(2));
                }
            }
        } else {
            /* Special case for first node in the network */
            if (nextLine.equals("first")) {
                localNode.first();
                return;
            }
            System.err.println("Invalid IP address");
            return;
        }

        try {
            if (port != 0) {
                error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(input), InetAddress.getByName(input), port, port));
            } else {
                error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(input), InetAddress.getByName(input), 12345, 12345));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (error) {
            System.err.println("Could not bootstrap to the specified IP - please try again\n");
        }
    }

    private static void register() {

        boolean success = false;

        System.out.println("Please enter a username:");
        String username = sc.nextLine();

        System.out.println("Please enter a password:");
        String password = sc.nextLine();


        try {
            success = (localNode.getUsers().registerUser(username, password));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (success) {
            System.out.println("User registered successfully!");
            System.out.println("Logged in as " + username + " successfully!\n");
        } else {
            System.err.println("User already exists - please choose a different username\n");
        }
    }

    private static void login() {

        boolean loggedIn = false;

        System.out.println("Please enter a username:");
        String username = sc.nextLine();

        System.out.println("Please enter a password:");
        String password = sc.nextLine();

        try {
            loggedIn = (localNode.getUsers().loginUser(username, password));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loggedIn) {
            System.out.println("Logged in as " + username + " successfully!");
        } else {
            System.err.println("Invalid username/password - please try again\n");
        }
    }

    private static void message() {

        System.out.println("Please specify a username to message:");
        String username = sc.nextLine();

        User userToMessage = new User(username, "");

        System.out.println("Please enter a message to send:");
        String message = sc.nextLine();

        try {
            localNode.message(message, userToMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void routingTable() {
        System.out.println(localNode.getRoutingTable());
    }

    private static void exit() {
        localNode.shutdown(true);
    }
}