package uk.co.streefland.rhys.finalyearproject.core;

import org.junit.Before;
import org.junit.Test;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.routing.RoutingTable;

import java.io.File;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the StorageHandler class
 */
public class StorageHandlerTest {

    private StorageHandler storageHandler;
    private StorageHandler storageHandler2;


    @Before
    public void setUp() throws Exception {
        storageHandler = new StorageHandler();
        storageHandler2 = new StorageHandler();
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        LocalNode localNode = new LocalNode(new KeyId(), new KeyId(), 12345);

        KeyId networkId = localNode.getNetworkId();
        Configuration config = localNode.getConfig();
        RoutingTable routingTable = localNode.getRoutingTable();
        Users users = localNode.getUsers();
        Messages messages = localNode.getMessages();
        Node node = new Node(new KeyId(), InetAddress.getByName("127.0.0.1"), InetAddress.getByName("127.0.0.2"), 12345, 54321);

        storageHandler.save(config, networkId, node, routingTable, users, messages);
        storageHandler2.load();

        Configuration loadedConfig = storageHandler2.getConfig();
        Node loadedNode = storageHandler2.getNode();
        RoutingTable loadedRoutingTable = storageHandler2.getRoutingTable();
        Users loadedUsers = storageHandler2.getUsers();

        assertEquals(config.getMaxConnectionAttempts(), loadedConfig.getMaxConnectionAttempts());
        assertEquals(config.getOperationTimeout(), loadedConfig.getOperationTimeout());
        assertEquals(config.getRefreshInterval(), loadedConfig.getRefreshInterval());
        assertEquals(config.getResponseTimeout(), loadedConfig.getResponseTimeout());

        assertEquals(node.getNodeId(), loadedNode.getNodeId());
        assertEquals(node.getPrivateInetAddress(), loadedNode.getPrivateInetAddress());
        assertEquals(node.getPublicInetAddress(), loadedNode.getPublicInetAddress());
        assertEquals(node.getPrivatePort(), loadedNode.getPrivatePort());
        assertEquals(node.getPublicPort(), loadedNode.getPublicPort());

        assertEquals(routingTable.getAllNodes(true), loadedRoutingTable.getAllNodes(true));
        assertEquals(routingTable.getBuckets().length, loadedRoutingTable.getBuckets().length);

        assertEquals(users.getLocalUser(), users.getLocalUser());
        assertEquals(users.getUsers(), loadedUsers.getUsers());
    }

    @Test
    public void testDoesSavedStateExist() throws Exception {
        File f = new File(Configuration.FILE_PATH);
        f.delete();
        f.createNewFile();

        assertTrue(storageHandler.doesSavedStateExist());
        f.delete();
    }
}