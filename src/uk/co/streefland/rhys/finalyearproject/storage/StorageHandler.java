package uk.co.streefland.rhys.finalyearproject.storage;

import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.Users;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.*;

/**
 * Provides methods for saving to and reading from files.
 * Useful for saving the state on shutdown but will eventually be used to offload data from memory in real time as well.
 */
public class StorageHandler {

    private Node localNode;
    private RoutingTable routingTable;
    private Users users;

    public StorageHandler() {
        this.localNode = null;
        this.routingTable = null;
        this.users = null;
    }

    /**
     * Save the provided objects to the file specified in the Configuration class
     *
     * @param localNode    The Node object to write to file
     * @param routingTable The RoutingTable object to write to file
     */
    public void save(Node localNode, RoutingTable routingTable, Users users) {
        FileOutputStream fout;
        ObjectOutputStream oos;

        try {
            fout = new FileOutputStream(Configuration.FILE_PATH, false);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(localNode);
            oos.writeObject(routingTable);
            oos.writeObject(users);
            oos.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the Node and RoutingTable objects from the file specified in the Configuration class.
     */
    public void load() {
        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(Configuration.FILE_PATH);
            ois = new ObjectInputStream(fis);

            localNode = (Node) ois.readObject();
            routingTable = (RoutingTable) ois.readObject();
            users = (Users) ois.readObject();

            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return True if the file exists, false if not
     */
    public boolean doesSavedStateExist() {
        File f = new File(Configuration.FILE_PATH);
        return f.exists() && !f.isDirectory();
    }

    /**
     * @return The Node object that was read from file by the load() method
     */
    public Node getLocalNode() {
        return localNode;
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
}
