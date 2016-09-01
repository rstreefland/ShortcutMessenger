package uk.co.streefland.rhys.finalyearproject.irrelevant;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * The server that handles sending and receiving messages between nodes on the Kad Network
 *
 * @author Joshua Kissoon
 * @created 20140215
 */
public class ServerPOC {

    /* Maximum size of a Datagram Packet */
    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;      // 64KB

    /* Basic Kad Objects */
    private final transient Configuration config;

    /* Server Objects */
    private final DatagramSocket socket;
    private transient boolean isRunning = true;
    //private final Map<Integer, Receiver> receivers;
    private final Timer timer = new Timer(true);      // Schedule future tasks
    private final Map<Integer, TimerTask> tasks = new HashMap<>();  // Keep track of scheduled tasks

    public ServerPOC(int udpPort, Configuration config) throws SocketException {
        this.config = config;
        this.socket = new DatagramSocket(udpPort);

        /* Start listening for incoming requests in a new thread */
        this.startListener();
    }

    /**
     * Starts the listener to listen for incoming messages
     */
    private void startListener() {
        new Thread() {
            @Override
            public void run() {
                listen();
            }
        }.start();
    }

    /**
     * Listen for incoming messages in a separate thread
     */
    private void listen() {
        System.out.println("Listener is running");
        try {
            while (isRunning) {
                try {
                    receivePacket();
                } catch (IOException e) {
                    //this.isRunning = false;
                    System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
                }
            }
            System.out.println("Listener has stopped running");
        } finally {
            if (!socket.isClosed()) {
                socket.close();
            }
            this.isRunning = false;
        }
    }

    private void sendMessage() throws IOException {

        System.out.println("Please enter IP to connect to:");
        Scanner sc = new Scanner(System.in);
        String ip = sc.nextLine();

        DatagramSocket clientSocket = new DatagramSocket();


        while (true) {
            sc = new Scanner(System.in);
            String message = sc.nextLine();

            byte[] buffer;
            buffer = message.getBytes();
            DatagramPacket packet = buildPacket(message, ip , 9001);
            clientSocket.send(packet);
            //clientSocket.close();
        }
    }

    private DatagramPacket buildPacket(String message, String host, int port) throws IOException {
        // Create a byte array from a string
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeBytes(message);
        byte[] data = byteOut.toByteArray();
        //Return the new object with the byte array payload
        return new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
    }

    private void receivePacket() throws IOException {
        byte buffer[] = new byte[DATAGRAM_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        // Convert the byte array read from network into a string
        ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(byteIn));

        // Read in data from a standard format
        String message = "";
        while ((message = dataIn.readLine()) != null) {
            System.out.println("Message from " + packet.getAddress().getCanonicalHostName() + ": " + message);
        }
    }

    /**
     * Stops listening and shuts down the server
     */
    public synchronized void shutdown() {
        this.isRunning = false;
        this.socket.close();
        timer.cancel();
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public static void main(String[] args) {
        try {
            ServerPOC server = new ServerPOC(9001, new Configuration());
            //Server server2 = new Server(9001, new Configuration());


            server.sendMessage();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
