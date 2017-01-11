package uk.co.streefland.rhys.finalyearproject.cli;

import uk.co.streefland.rhys.finalyearproject.core.*;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Command line interface for the framework
 */
class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static IPTools ipTools;
    private static String input = ""; // a line of input from scanner
    private static String[] inputWords = new String[2]; // scanner input separated into words
    private static LocalNode localNode = null;
    private static final ArrayList<String> availableCommands = new ArrayList<>(); // stores a list of available commands

    public static void main(String[] args) {
        System.out.println("Shortcut Messenger CLI");

        availableCommands.add("bootstrap");
        availableCommands.add("help");
        availableCommands.add("exit");


        try {
            ipTools = new IPTools();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* CLI doesn't support saved state - delete the file if exists */
        new StorageHandler().delete();

        while (!input.equals("exit")) {
            System.out.print("\n# ");

            input = sc.nextLine(); // read input from System.in
            inputWords = input.split("\\s+"); // split string into words

            switch (inputWords[0]) {
                case "help":
                    help();
                    break;
                case "bootstrap":
                    try {
                        bootstrap();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "register":
                    try {
                        register();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "login":
                    try {
                        login();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "message":
                    try {
                        message();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "print":
                    print();
                    break;
                case "exit":
                    exit();
                    break;
                default:
                    System.out.println("Command not recognised. Please type help for a list of available commands");
                    break;
            }
        }
    }

    /**
     * Prints out a list of available commands
     */
    private static void help() {

        /* Special case for 'help print' */
        if (inputWords.length > 1) {
            if (inputWords[1].equals("print")) {
                System.out.println("\nPrint command usage information:");
                System.out.println("-----------------------------");
                System.out.println("print routingtable - print a list of nodes in the local routing table");
                System.out.println("print users - print a list of users stored in the local node");
                return;
            }
        }

        System.out.println("\nCurrently available commands:");
        System.out.println("-----------------------------");

        if (availableCommands.contains("bootstrap"))
            System.out.println("bootstrap - Bootstrap the local node to a network");

        if (availableCommands.contains("register"))
            System.out.println("register - Register a user account on the network");

        if (availableCommands.contains("login"))
            System.out.println("login - Login to an existing user account");

        if (availableCommands.contains("message"))
            System.out.println("message - Message a user on the network");

        if (availableCommands.contains("print"))
            System.out.println("print - Use 'help print' for usage information");

        if (availableCommands.contains("help"))
            System.out.println("help - A summary of all available commands");

        if (availableCommands.contains("exit"))
            System.out.println("exit - Exit the program");
    }

    /**
     * Handles bootstrapping the localnode to a network
     *
     * @throws IOException
     */
    private static void bootstrap() throws IOException, ClassNotFoundException {
        if (!availableCommands.contains("bootstrap")) {
            return;
        }

        if (inputWords.length > 1) {
            String networkIpString = inputWords[1];

            /* Special case for first node in the network */
            if (networkIpString.equals("first")) {
                localNode = new LocalNode(ipTools, Configuration.DEFAULT_PORT);
                localNode.first();

                availableCommands.remove("bootstrap");
                availableCommands.add("login");
                availableCommands.add("register");
                availableCommands.add("message");
                availableCommands.add("print");

                System.out.println("Network created successfully");
                return;
            }

            InetAddress networkIp = null;

            /* Convert IP/hostname into InetAddress */
            try {
                networkIp = ipTools.validateAddress(networkIpString);
            } catch (UnknownHostException ignored) {
            }

            /* Create the localNode object */
            if (networkIp != null) {
                localNode = new LocalNode(ipTools, Configuration.DEFAULT_PORT);
            } else {
                System.err.println("Invalid network address");
            }

            /* Attempt to bootstrap to the network */
            boolean error = localNode.bootstrap(new Node(new KeyId(), networkIp, networkIp, Configuration.DEFAULT_PORT, Configuration.DEFAULT_PORT));

            if (error) {
                localNode.shutdown(false);
                localNode = null;
                System.err.println("Failed to bootstrap to the specified network");
            } else {
                availableCommands.remove("bootstrap");
                availableCommands.add("login");
                availableCommands.add("register");
                availableCommands.add("message");
                availableCommands.add("print");
                System.out.println("Successfully bootstrapped to network.");
            }
        }
    }

    private static void register() throws IOException {
        if (!availableCommands.contains("register")) {
            return;
        }

        if (inputWords.length > 2) {
            if (localNode.getUsers().registerUser(inputWords[1], inputWords[2])) {
                System.out.println("User " + inputWords[1] + " registered successfully");
            } else {
                System.err.println("User already exists");
            }
        }
    }

    private static void login() throws IOException {
        if (!availableCommands.contains("login")) {
            return;
        }

        if (inputWords.length > 2) {
            if (localNode.getUsers().loginUser(inputWords[1], inputWords[2])) {
                System.out.println("Logged in as " + inputWords[1] + " successfully");
            } else {
                System.err.println("Invalid username or password");
            }
        }
    }

    private static void message() throws IOException {
        if (!availableCommands.contains("message")) {
            return;
        }

        if (inputWords.length > 1) {
            String recipient = inputWords[1];

            System.out.println("Enter a message:");
            String message = sc.nextLine();

            localNode.getMessages().sendMessage(message, new User(recipient, ""));
        }
    }

    private static void print() {
        if (!availableCommands.contains("print")) {
            return;
        }


        if (inputWords.length > 1) {

            if (inputWords[1].equals("routingtable")) {
                System.out.println(localNode.getRoutingTable().toString());
            } else if (inputWords[1].equals("users")) {
                System.out.println(localNode.getUsers().toString());
            }
        }
    }

    private static void exit() {
        if (localNode != null) {
            try {
                localNode.shutdown(false); // saved state not supported by CLI
            } catch (IOException e) {
                System.err.println("Failed to shutdown cleanly");
            }
        }
    }
}
