package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.NetworkTraversalOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rhys on 08/12/2016.
 */
public class Visualiser {

    private LocalNode localNode;
    private Graph graph = new Graph();
    private Map<KeyId, List<Node>> nodeRoutingTables;

    public Visualiser(LocalNode localNode) {
        this.localNode = localNode;

        Task task = new Task() {
            @Override
            protected Map<KeyId, List<Node>> call() throws Exception {
                NetworkTraversalOperation nto = new NetworkTraversalOperation(localNode);
                nto.execute();

                succeeded();
                return nto.getNodeRoutingTables();
            }
        };

        /* Run the task in a separate thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* On task finish */
        task.setOnSucceeded(event1 -> {
            System.out.println("FINISHED NETWORK TRAVERSAL OPERATION");

            Map<KeyId, List<Node>> nodeRoutingTables = (Map<KeyId, List<Node>>) task.getValue(); // result of computation

            if (nodeRoutingTables != null) {
                this.nodeRoutingTables = nodeRoutingTables;
                draw();
            }
        });
    }

    private void draw() {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();

        graph = new Graph();
        root.setCenter(graph.getScrollPane());

        Scene scene = new Scene(root, 1024, 768);

        addGraphComponents();

        Layout layout = new RandomLayout(graph);
        layout.execute();

        stage.setScene(scene);
        stage.show();
    }

    private void addGraphComponents() {
        Model model = graph.getModel();

        graph.beginUpdate();

        for (Map.Entry routingTable : nodeRoutingTables.entrySet()) {
            for (Node node : (ArrayList<Node>) routingTable.getValue()) {
                model.addCell(node.getNodeId(), node.getPublicInetAddress().getHostAddress());
                model.addEdge((KeyId) routingTable.getKey(), node.getNodeId());
            }
        }

        graph.endUpdate();
    }
}
