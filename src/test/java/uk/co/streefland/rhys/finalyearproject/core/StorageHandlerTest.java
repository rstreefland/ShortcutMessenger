package uk.co.streefland.rhys.finalyearproject.core;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.File;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * Created by Rhys on 18/10/2016.
 */
public class StorageHandlerTest {

    StorageHandler storageHandler;
    StorageHandler storageHandler2;


    @Before
    public void setUp() throws Exception {
        storageHandler = new StorageHandler();
        storageHandler2 = new StorageHandler();
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        /* Configuration config = new Configuration();
        Node node = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), 123);
        RoutingTable routingTable = new RoutingTable(node);
        Users users = new Users(new LocalNode(new KeyId(), 123));
        Messages messages = new Messages(new LocalNode("",1));

        storageHandler.save(config, node, routingTable, users, messages);

        storageHandler2.load();

        Configuration loadedConfig = storageHandler2.getConfig();
        Node loadedNode = storageHandler2.getLocalNode();
        RoutingTable loadedRoutingTable = storageHandler2.getRoutingTable();
        Users loadedUsers = storageHandler2.getUsers();

        assertEquals(config.getMaxConnectionAttempts(), loadedConfig.getMaxConnectionAttempts());
        assertEquals(config.getOperationTimeout(), loadedConfig.getOperationTimeout());
        assertEquals(config.getPort(), loadedConfig.getPort());
        assertEquals(config.getRefreshInterval(), loadedConfig.getRefreshInterval());
        assertEquals(config.getResponseTimeout(), loadedConfig.getResponseTimeout());

        assertEquals(node.getNodeId(), loadedNode.getNodeId());
        assertEquals(node.getSocketAddress(), loadedNode.getSocketAddress());

        assertEquals(routingTable.getAllNodes(true), loadedRoutingTable.getAllNodes(true));
        assertEquals(routingTable.getBuckets().length, loadedRoutingTable.getBuckets().length);

        assertEquals(users.getLocalUser(), users.getLocalUser());
        assertEquals(users.getUsers(), loadedUsers.getUsers()); */
    }

    @Test
    public void testDoesSavedStateExist() throws Exception {
        File f = new File(Configuration.FILE_PATH);
        f.delete();
        f.createNewFile();

        assertTrue(storageHandler.doesSavedStateExist());
    }
}