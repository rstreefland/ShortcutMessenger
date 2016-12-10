package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.operation.NetworkTraversalOperation;

/**
 * Created by Rhys on 08/12/2016.
 */
public class Visualiser {

    private LocalNode localNode;
    Graph graph = new Graph();

    public Visualiser(LocalNode localNode) {
        this.localNode = localNode;

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                NetworkTraversalOperation nto = new NetworkTraversalOperation(localNode);
                nto.execute();

                succeeded();
                return "";
            }
        };

        /* Run the task in a separate thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* On task finish */
        task.setOnSucceeded(event1 -> {
            System.out.println("FINISHED NETWORK TRAVERSAL OPERATION");
            draw();
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

        model.addCell("Cell A");
        model.addCell("Cell B");
        model.addCell("Cell C");
        model.addCell("Cell D");
        model.addCell("Cell E");
        model.addCell("Cell F");
        model.addCell("Cell G");

        model.addEdge("Cell A", "Cell B");
        model.addEdge("Cell A", "Cell C");
        model.addEdge("Cell B", "Cell C");
        model.addEdge("Cell C", "Cell D");
        model.addEdge("Cell B", "Cell E");
        model.addEdge("Cell D", "Cell F");
        model.addEdge("Cell D", "Cell G");

        graph.endUpdate();
    }
}
