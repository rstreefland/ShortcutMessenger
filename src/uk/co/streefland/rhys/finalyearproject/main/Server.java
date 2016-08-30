package uk.co.streefland.rhys.finalyearproject.main;

import com.sun.xml.internal.bind.v2.TODO;
import uk.co.streefland.rhys.finalyearproject.message.Message;
import uk.co.streefland.rhys.finalyearproject.message.MessageFactory;
import uk.co.streefland.rhys.finalyearproject.message.Receiver;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The server that handles sending and receiving messages between nodes on the Kad Network
 *
 * @author Joshua Kissoon
 * @created 20140215
 */
public class Server {

    /* Maximum size of a Datagram Packet */
    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;      // 64KB

    /* Basic Kad Objects */
    private final transient Configuration config;

    private final Node localNode;
    private final MessageFactory messageFactory;

    /* Server Objects */
    private final DatagramSocket socket;
    private transient boolean isRunning = true;
    private final Timer timer = new Timer(true);      // Schedule future tasks
    private final Map<Integer, TimerTask> tasks = new HashMap<>();  // Keep track of scheduled tasks
    private final Map<Integer, Receiver> receivers = new HashMap<>();

    public Server(int udpPort, MessageFactory messageFactory, Node localNode, Configuration config) throws SocketException
    {
        this.config = config;
        this.socket = new DatagramSocket(udpPort);
        this.localNode = localNode;
        this.messageFactory = messageFactory;

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
    private void listen()
    {
        try
        {
            while (isRunning)
            {
                try
                {
                    /* Wait for a packet */
                    byte[] buffer = new byte[DATAGRAM_BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    /* We've received a packet, now handle it */
                    try (ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                         DataInputStream din = new DataInputStream(bin))
                    {

                        /* Read in the conversation Id to know which handler to handle this response */
                        int comm = din.readInt();
                        byte messCode = din.readByte();

                        Message msg = messageFactory.createMessage(messCode, din);
                        din.close();

                        /* Get a receiver for this message */
                        Receiver receiver;
                        if (this.receivers.containsKey(comm))
                        {
                            /* If there is a reciever in the receivers to handle this */
                            synchronized (this)
                            {
                                receiver = this.receivers.remove(comm);
                                TimerTask task = (TimerTask) tasks.remove(comm);
                                if (task != null)
                                {
                                    task.cancel();
                                }
                            }
                        }
                        else
                        {
                            /* There is currently no receivers, try to get one */
                            receiver = messageFactory.createReceiver(messCode, this);
                        }

                        /* Invoke the receiver */
                        if (receiver != null)
                        {
                            receiver.receive(msg, comm);
                        }
                    }
                }
                catch (IOException e)
                {
                    //this.isRunning = false;
                    System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
                }
            }
        }
        finally
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
            this.isRunning = false;
        }
    }

    /**
     * Method called to reply to a message received
     *
     * @param to   The Node to send the reply to
     * @param msg  The reply message
     * @param comm The communication ID - the one received
     *
     * @throws java.io.IOException
     */
    public synchronized void reply(Node to, Message msg, int comm) throws IOException
    {
        if (!isRunning)
        {
            throw new IllegalStateException("Kad Server is not running.");
        }
        sendMessage(to, msg, comm);
    }

    public synchronized int sendMessage(Node to, Message msg, Receiver recv) throws IOException
    {
        if (!isRunning)
        {
            throw new IOException(this.localNode + " - Kad Server is not running.");
        }

        /* Generate a random communication ID */
        int communicationId = new Random().nextInt();

        /* If we have a receiver */
        if (recv != null)
        {
            try
            {
                /* Setup the receiver to handle message response */
                receivers.put(communicationId, recv);
                TimerTask task = new TimeoutTask(communicationId, recv);
                timer.schedule(task, this.config.responseTimeout());
                tasks.put(communicationId, task);
            }
            catch (IllegalStateException ex)
            {
                /* The timer is already cancelled so we cannot do anything here really */
            }
        }

        /* Send the message */
        sendMessage(to, msg, communicationId);

        return communicationId;
    }

    /**
     * Internal sendMessage method called by the public sendMessage method after a communicationId is generated
     */
    private void sendMessage(Node destination, Message msg, int commmunicationId) throws IOException
    {
        /* Use a try-with resource to auto-close streams after usage */
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(); DataOutputStream dout = new DataOutputStream(bout))
        {
            /* Setup the message for transmission */
            dout.writeInt(commmunicationId);
            dout.writeByte(msg.code());
            msg.toStream(dout);
            dout.close();

            byte[] data = bout.toByteArray();

            if (data.length > DATAGRAM_BUFFER_SIZE) {
                // TODO: split large message into smaller datagram packets
                throw new IOException("Message is too big");
            }

            /* Everything is good, now create the packet and send it */
            DatagramPacket pkt = new DatagramPacket(data, 0, data.length);
            pkt.setSocketAddress(destination.getSocketAddress());
            socket.send(pkt);
        }
    }

    /**
     * Task that gets called by a separate thread if a timeout for a receiver occurs.
     * When a reply arrives this task must be canceled using the <code>cancel()</code>
     * method inherited from <code>TimerTask</code>. In this case the caller is
     * responsible for removing the task from the <code>tasks</code> map.
     * */
    class TimeoutTask extends TimerTask
    {

        private final int communicationId;
        private final Receiver recv;

        public TimeoutTask(int communicationId, Receiver recv)
        {
            this.communicationId = communicationId;
            this.recv = recv;
        }

        @Override
        public void run()
        {
            if (!Server.this.isRunning)
            {
                return;
            }

            try
            {
                unregister(communicationId);
                recv.timeout(communicationId);
            }
            catch (IOException e)
            {
                System.err.println("Cannot unregister a receiver. Message: " + e.getMessage());
            }
        }
    }

    /**
     * Remove a conversation receiver
     *
     * @param communicationId The id of this conversation
     */
    private synchronized void unregister(int communicationId)
    {
        receivers.remove(communicationId);
        this.tasks.remove(communicationId);
    }

    /**
     * Stops listening and shuts down the server
     */
    public synchronized void shutdown() {
        this.isRunning = false;
        this.socket.close();
        timer.cancel();
    }

    public void printReceivers()
    {
        for (Integer r : this.receivers.keySet())
        {
            System.out.println("Receiver for comm: " + r + "; Receiver: " + this.receivers.get(r));
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
