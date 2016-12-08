package uk.co.streefland.rhys.finalyearproject.core;

import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.MessageHandler;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.message.content.TextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP server that handles sending and receiving messages as UDP packets between nodes on the network
 */
public class Server {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private LocalNode localNode;

    /* Server Objects */
    private final DatagramSocket socket;
    private final Timer timer = new Timer(true);    // Schedule future tasks
    private final Map<Integer, TimerTask> tasks = new HashMap<>();  // Keep track of scheduled tasks
    private final Map<Integer, Receiver> receivers = new HashMap<>();
    private DatagramPacket packet;
    private boolean isRunning;

    /* Cached threadPool so we can run receivers in parallel */
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(LocalNode localNode, int udpPort) throws IOException {
        this.localNode = localNode;
        this.socket = new DatagramSocket(udpPort);

        byte[] buffer = new byte[Configuration.PACKET_SIZE];
        packet = new DatagramPacket(buffer, buffer.length);
        isRunning = false;
    }

    /**
     * Starts the listener thread to listen for incoming messages
     */
    public void startListener() {
        logger.info("Starting server");
        isRunning = true;

        /* Start the listener thread*/
        final Thread thread = new Thread(() -> listen());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Listens for incoming messages over UDP
     */
    private void listen() {
        logger.info("Server is listening on port {}", socket.getLocalPort());
        while (isRunning) {
            try {
                /* Wait for a packet*/
                socket.receive(packet);

                /* Handle the received packet */
                ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                DataInputStream din = new DataInputStream(bin);

                /* Read the communicationId and messageCode */
                int communicationId = din.readInt();
                byte messageCode = din.readByte();

                /* Create the message and close the input stream */
                Message msg = localNode.getMessageHandler().createMessage(messageCode, din);

                /* Close the input stream */
                din.close();

                /* Check if the node is part of this network - drop the packet if not
                   allow if message is connect message
                   allow if ack and local network ID is null
                   allow if local network ID == remote network ID */ // TODO: 06/12/2016 sort out the null pointer exception here
                if (messageCode == 0x02 || (messageCode == 0x01 && localNode.getNetworkId() == null) || localNode.getNetworkId().equals(msg.getNetworkId())) {

                    /* Check if IPs match - if not, ignore the message. Saves processing, future exceptions, and maintains security */
                    if (packet.getAddress().equals(msg.getSource().getPublicInetAddress()) || packet.getAddress().equals(msg.getSource().getPrivateInetAddress())) {

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
                        /* There is currently no receivers, create one*/
                            logger.debug("No receiver exists, creating one using code {}", messageCode);
                            receiver = localNode.getMessageHandler().createReceiver(packet.getPort(), messageCode);
                        }

                        /* Start the ReceiverTask on a thread in the cached threadPool*/
                        /* This is done so the computation for each receiver doesn't block the listener thread */
                        if (receiver != null) {
                            threadPool.execute(new ReceiverTask(receiver, msg, communicationId));
                        }
                    } else {
                        logger.debug("Remote node has IP address mismatch - ignoring message");
                    }
                } else {
                    logger.info("Discarding message from node that isn't part of this network");
                    logger.info("   Local netId: " + localNode.getNetworkId());
                    logger.info("  Remote netId: " + msg.getNetworkId());
                }
            } catch (IOException e) {
                if (isRunning) {
                    logger.error("The listener thread encountered an error:", e);
                }
            }
        }
        logger.debug("Shutdown command received - listener thread stopping");
    }

    /**
     * Sends a message to a remote node
     *
     * @param destination The destination node
     * @param msg         The message
     * @param recv        The receiver for the reply
     * @return The communicationId of the message
     * @throws IOException
     */
    public synchronized int sendMessage(Node destination, Message msg, Receiver recv) throws IOException {
        if (!isRunning) {
            return 0;
        }

        /* Generate a random communication ID */
        int communicationId = new Random().nextInt();

        /* If a receiver exists */
        if (recv != null) {
            /* Setup the receiver to handle message response */
            receivers.put(communicationId, recv);
            TimerTask task = new TimeoutTask(communicationId, recv);
            timer.schedule(task, localNode.getConfig().getResponseTimeout());
            tasks.put(communicationId, task);
        }

        /* Send the message using the private method below */
        sendMessage(destination, msg, communicationId);

        return communicationId;
    }

    /**
     * Internal sendMessage method called by the public sendMessage method after a communicationId is generated
     *
     * @param destination     The destination node
     * @param msg             The message
     * @param communicationId The communicationId of the message
     * @throws IOException
     */
    private void sendMessage(Node destination, Message msg, int communicationId) throws IOException {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        /* Setup the message for transmission */
        dout.writeInt(communicationId);
        dout.writeByte(msg.getCode());
        msg.toStream(dout);
        dout.close();

        byte[] data = bout.toByteArray();

        if (data.length > Configuration.PACKET_SIZE) {
            // TODO: split large message into smaller datagram packets
            throw new IllegalStateException("Message is too large");
        }

            /* Create the packet and send it */
        DatagramPacket pkt = new DatagramPacket(data, 0, data.length);

        if (destination.getPublicInetAddress().equals(localNode.getNode().getPublicInetAddress())) {
            pkt.setSocketAddress(destination.getPrivateSocketAddress());
        } else {
            pkt.setSocketAddress(destination.getPublicSocketAddress());
        }

        socket.send(pkt);
    }

    /**
     * Replies to a received message
     *
     * @param destination     The destination node
     * @param msg             The reply message
     * @param communicationId The communication ID that was received
     * @throws java.io.IOException
     */
    public synchronized void reply(Node destination, Message msg, int communicationId) throws IOException {
        if (!isRunning) {
            return;
        }
        sendMessage(destination, msg, communicationId);
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
     * Stops listening and shuts down the server in a clean manner
     */
    public synchronized void shutdown() {
        logger.info("Shutting down server");

        threadPool.shutdownNow();  // shut down any running threads in the threadPool;
        isRunning = false;
        socket.close();
        timer.cancel();

        System.out.println("ThreadPool is shutdown? "  + threadPool.isShutdown());
    }

    public boolean isRunning() {
        return isRunning;
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
}
