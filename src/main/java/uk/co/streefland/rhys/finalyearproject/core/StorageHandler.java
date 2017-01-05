package uk.co.streefland.rhys.finalyearproject.core;

import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.*;

/**
 * Provides methods for saving to and reading from files.
 * Useful for saving the state on shutdown but will eventually be used to offload data from memory in real time as well.
 */
public class StorageHandler {

    private Configuration config;
    private KeyId networkId;
    private Node node;
    private RoutingTable routingTable;
    private Users users;
    private Messages messages;

    public StorageHandler() {
        this.config = null;
        this.networkId = null;
        this.node = null;
        this.routingTable = null;
        this.users = null;
        this.messages = null;
    }

    /**
     * Save the provided objects to the file specified in the Configuration class
     *
     * @param node         The Node object to write to file
     * @param routingTable The RoutingTable object to write to file
     */
    public void save(Configuration config, KeyId networkId, Node node, RoutingTable routingTable, Users users, Messages messages) throws IOException {
        FileOutputStream fout;
        ObjectOutputStream oos;

        fout = new FileOutputStream(Configuration.FILE_PATH, false);
        oos = new ObjectOutputStream(fout);

        /* Write objects to stream */
        oos.writeObject(config);
        oos.writeObject(networkId);
        oos.writeObject(node);
        oos.writeObject(routingTable);
        oos.writeObject(users);
        oos.writeObject(messages);

        /* Close output streams */
        oos.close();
        fout.close();
    }

    /**
     * Reads the Node and RoutingTable objects from the file specified in the Configuration class.
     */
    public void load() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(Configuration.FILE_PATH);
        ObjectInputStream ois = new ObjectInputStream(fis);

        /* Read objects from stream */
        config = (Configuration) ois.readObject();
        networkId = (KeyId) ois.readObject();
        node = (Node) ois.readObject();
        routingTable = (RoutingTable) ois.readObject();
        users = (Users) ois.readObject();
        messages = (Messages) ois.readObject();

        /* Close input streams */
        ois.close();
        fis.close();
    }

    public void delete() {
        File f = new File(Configuration.FILE_PATH);
        f.delete();
    }

    /**
     * @return True if the file exists, false if not
     */
    public boolean doesSavedStateExist() {
        File f = new File(Configuration.FILE_PATH);
        return f.exists() && !f.isDirectory();
    }

    /**
     * @return The Configuration object that was read from file by the load() method
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @return The KeyId object that was read from file by the load() method
     */
    public KeyId getNetworkId() {
        return networkId;
    }

    /**
     * @return The Node object that was read from file by the load() method
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return The RoutingTable object that was read from file by the load() method
     */
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    /**
     * @return The Users object that was read from file by the load() method
     */
    public Users getUsers() {
        return users;
    }

    /**
     * @return The Messages object that was read from file by the load() method
     */
    public Messages getMessages() {
        return messages;
    }
}
