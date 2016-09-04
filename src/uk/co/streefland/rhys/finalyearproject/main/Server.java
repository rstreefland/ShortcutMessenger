package uk.co.streefland.rhys.finalyearproject.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.test.LoggingTest;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * UDP server that handles sending and receiving messages as UDP packets between nodes on the network
 */
public class Server {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Configuration config;
    private final Node localNode;
    private final MessageHandler messageHandler;

    /* Server Objects */
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private final Timer timer = new Timer(true);    // Schedule future tasks
    private final Map<Integer, TimerTask> tasks = new HashMap<>();  // Keep track of scheduled tasks
    private final Map<Integer, Receiver> receivers = new HashMap<>();

    public Server(int udpPort, MessageHandler messageHandler, Node localNode, Configuration config) throws SocketException {
        this.config = config;
        this.socket = new DatagramSocket(udpPort);
        this.localNode = localNode;
        this.messageHandler = messageHandler;

        /* Start listening for incoming requests in a new thread */
        startListener();
    }

    /**
     * Starts the listener thread to listen for incoming messages
     */
    private void startListener() {
        new Thread() {
            @Override
            public void run() {
                logger.info("Starting server");
                listen();
            }
        }.start();
    }

    /**
     * Listens for incoming messages over UDP
     */
    private void listen() {
        try {
            logger.info("Server is running");
            while (isRunning) {
                try {
                    /* Wait for a packet*/
                    byte[] buffer = new byte[config.getPacketSize()];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    /* Handle the received packet */
                    try (ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                         DataInputStream din = new DataInputStream(bin)) {

                        /* Read the communicationId and messageCode */
                        int communicationId = din.readInt();
                        byte messageCode = din.readByte();

                        logger.debug("Incoming message code is {}", messageCode);
                        /* Create the message and close the input stream */
                        Message msg = messageHandler.createMessage(messageCode, din);
                        din.close();

                        /* Check if a receiver already exists and create one if not */
                        Receiver receiver;
                        if (receivers.containsKey(communicationId)) {
                            /* If there is a receiver in the receivers list to handle this */
                            synchronized (this) {
                                receiver = receivers.remove(communicationId);
                                TimerTask task = tasks.remove(communicationId);
                                if (task != null) {
                                    task.cancel();
                                }
                            }
                        } else {
                            /* There is currently no receivers, try to get one */
                            logger.debug("No receiver exists, creating one using code {}", messageCode);
                            receiver = messageHandler.createReceiver(messageCode, this);
                        }

                        /* Invoke the receiver */
                        if (receiver != null) {
                            receiver.receive(msg, communicationId);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
                }
            }
        } finally {
            if (!socket.isClosed()) {
                socket.close();
            }
            isRunning = false;
        }
    }

    /**
     * Replies to a received message
     *
     * @param destination   The destination node
     * @param msg  The reply message
     * @param communicationId The communication ID that was received
     * @throws java.io.IOException
     */
    public synchronized void reply(Node destination, Message msg, int communicationId) throws IOException {
        if (!isRunning) {
            throw new IllegalStateException("Server is not running.");
        }
        sendMessage(destination, msg, communicationId);
    }

    /**
     * Sends a message
     *
     * @param destination The destination node
     * @param msg The message
     * @param recv The receiver for the reply
     * @return The communicationId of the message
     * @throws IOException
     */
    public synchronized int sendMessage(Node destination, Message msg, Receiver recv) throws IOException {
        if (!isRunning) {
            throw new IOException(localNode + "- Server is not running.");
        }

        /* Generate a random communication ID */
        int communicationId = new Random().nextInt();

        /* If a receiver exists */
        if (recv != null) {
            try {
                /* Setup the receiver to handle message response */
                receivers.put(communicationId, recv);
                TimerTask task = new TimeoutTask(communicationId, recv);
                timer.schedule(task, config.getResponseTimeout());
                tasks.put(communicationId, task);
            } catch (IllegalStateException ex) {
            }
        }

        /* Send the message using the private method below */
        sendMessage(destination, msg, communicationId);

        return communicationId;
    }

    /**
     * Internal sendMessage method called by the public sendMessage method after a communicationId is generated
     *
     * @param destination The destination node
     * @param msg   The message
     * @param communicationId The communicationId of the message
     * @throws IOException
     */
    private void sendMessage(Node destination, Message msg, int communicationId) throws IOException {

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(bout)) {

            /* Setup the message for transmission */
            dout.writeInt(communicationId);
            dout.writeByte(msg.code());
            msg.toStream(dout);
            dout.close();

            byte[] data = bout.toByteArray();

            if (data.length > config.getPacketSize()) {
                // TODO: split large message into smaller datagram packets
                throw new IOException("Message is too big");
            }

            /* Create the packet and send it */
            DatagramPacket pkt = new DatagramPacket(data, 0, data.length);
            pkt.setSocketAddress(destination.getSocketAddress());
            socket.send(pkt);
        }
    }

    /**
     * Task that gets called by a separate thread if a timeout for a receiver occurs.
     * When a reply arrives this task must be canceled using the cancel()
     * method inherited from TimerTask
     */
    class TimeoutTask extends TimerTask {

        private final int communicationId;
        private final Receiver recv;

        public TimeoutTask(int communicationId, Receiver recv) {
            this.communicationId = communicationId;
            this.recv = recv;
        }

        @Override
        public void run() {
            if (!Server.this.isRunning) {
                return;
            }

            try {
                unregister(communicationId);
                recv.timeout(communicationId);
            } catch (IOException e) {
                System.err.println("Cannot unregister a receiver. Message: " + e.getMessage());
            }
        }
    }

    /**
     * Remove a conversation receiver
     *
     * @param communicationId The id of this conversation
     */
    private synchronized void unregister(int communicationId) {
        receivers.remove(communicationId);
        tasks.remove(communicationId);
    }

    /**
     * Stops listening and shuts down the server
     */
    public synchronized void shutdown() {
        isRunning = false;
        socket.close();
        timer.cancel();
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
