package uk.co.streefland.rhys.finalyearproject.storage;

import uk.co.streefland.rhys.finalyearproject.main.Configuration;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.*;

/**
 * Created by Rhys on 05/09/2016.
 */
public class StorageHandler {

    Configuration config;
    Node localNode;
    RoutingTable routingTable;


    public StorageHandler(Configuration config) {
        this.config = config;
        this.localNode = null;
        this.routingTable = null;
    }

    public void save(Node localNode, RoutingTable routingTable) {
        FileOutputStream fout;
        ObjectOutputStream oos;

        try {
            fout = new FileOutputStream(config.getFilePath(), false);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(localNode);
            oos.writeObject(routingTable);
            oos.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(config.getFilePath());
            ois = new ObjectInputStream(fis);

            localNode = (Node) ois.readObject();
            routingTable = (RoutingTable) ois.readObject();

            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean doesSavedStateExist() {
        File f = new File(config.getFilePath());
        if (f.exists() && !f.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    public Node getLocalNode() {
        return localNode;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
