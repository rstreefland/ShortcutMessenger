package uk.co.streefland.rhys.finalyearproject.cli;

import uk.co.streefland.rhys.finalyearproject.core.IPTools;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Command line interface for the framework
 */
class Main {

    private static IPTools ipTools;

    private static final Scanner sc = new Scanner(System.in);
    private static String input = "";
    private static String[] inputWords = new String[2];
    private static LocalNode localNode = null;

    public static void main(String[] args) {
        System.out.println("Shortcut Messenger CLI v1.0");

        try {
            ipTools = new IPTools();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!input.equals("exit")) {
            System.out.print("\n# ");

            input = sc.nextLine();
            inputWords = input.split("\\s+");

            switch (inputWords[0]) {
                case "help":
                    help();
                    break;
                case "bootstrap":
                    try {
                        bootstrap();
                    } catch (IOException e) {
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
                    message();
                    break;
                case "print":
                    //routingTable();
                    break;
                case "exit":
                    exit();
                    break;
                default:
                    break;
            }
        }
    }

    private static void help() {

        if (inputWords.length > 1) {
            if (inputWords[1].equals("print")) {
                System.out.println("Print command usage information:");
                System.out.println("-----------------------------");
                System.out.println("print routingtable - print a list of nodes in the local routing table");
                System.out.println("print users - print a list of users stored in the local node");
                return;
            }
        }

        System.out.println("Currently available commands:");
        System.out.println("-----------------------------");
        System.out.println("bootstrap - Bootstrap the local node to a network");
        System.out.println("register - Register a user account on the network");
        System.out.println("login - Login to an existing user account");
        System.out.println("message - Message a user on the network");
        System.out.println("print - Use 'help print' for usage information");
        System.out.println("quit - Quit the program");

    }

    private static void bootstrap() throws IOException {
        if (inputWords.length > 1) {

            String publicIpString = ipTools.getPublicIp();
            String privateIpString = ipTools.getPrivateIp();
            String networkIpString = inputWords[1];

            InetAddress publicIp = ipTools.validateAddress(publicIpString);
            InetAddress privateIp = ipTools.validateAddress(privateIpString);

            /* Special case for first node in the network */
            if (networkIpString.equals("first")) {
                localNode = new LocalNode(ipTools, publicIp, privateIp, 12345);
                localNode.first();
            }

            InetAddress networkIp = null;

            try {
                networkIp = ipTools.validateAddress(networkIpString);
            } catch (UnknownHostException uho) {
            }

            if (networkIp != null) {
                localNode = new LocalNode(ipTools, publicIp, privateIp, 12345);
            } else {
                System.err.println("Invalid network address");
            }

            boolean error = localNode.bootstrap(new Node(new KeyId(), networkIp, networkIp, 12345, 12345));

            if (error) {
                localNode.shutdown(false);
                localNode = null;
                System.err.println("Failed to bootstrap to the specified network");
            }
        }
    }

    private static void register() throws IOException {
        if (inputWords.length > 2) {
            if (localNode.getUsers().registerUser(inputWords[1], inputWords[2])) {
                System.out.println("User " + inputWords[1] + " registered successfully");
            } else {
                System.err.println("User already exists");
            }
        }
    }

    private static void login() throws IOException {
        if (inputWords.length > 2) {
            if (localNode.getUsers().loginUser(inputWords[1], inputWords[2])) {
                System.out.println("Logged in as " + inputWords[1] + " successfully");
            } else {
                System.err.println("Invalid username or password");
            }
        }
    }

    private static void message() {
    }

    private static void exit() {
        if (localNode != null) {
            localNode.shutdown(true);
        }
    }
}
