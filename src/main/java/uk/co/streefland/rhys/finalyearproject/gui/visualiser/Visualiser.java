package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
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

    private Stage stage;

    public Visualiser(LocalNode localNode) {
        this.localNode = localNode;
        stage = new Stage();

        BorderPane root = new BorderPane();
        root.setCenter(new Label("Generating network visualisation"));

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add("style.css");
        root.getStyleClass().add("vis");

        stage.setTitle("Shortcut Messenger - Network Visualisation");
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icon6.png"));
        stage.show();

        doComputation();
    }

    private void doComputation() {
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
        BorderPane root = new BorderPane();
        root.getStyleClass().add("vis");

        graph = new Graph();

        ZoomableScrollPane zoomableScrollPane = graph.getScrollPane();
        root.setCenter(zoomableScrollPane);

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add("style.css");

        addGraphComponents();

        Layout layout = new RandomLayout(graph);
        layout.execute();

        HBox bottomToolbar = new HBox(5);
        Button routingTable = new Button("Routing Table");
        Button newLayout = new Button("New Layout");
        bottomToolbar.setPadding(new Insets(5));
        bottomToolbar.setAlignment(Pos.CENTER_RIGHT);

        Slider slider = new Slider(0, 2, zoomableScrollPane.getScaleValue());
        slider.valueProperty().addListener((ov, oldValue, newValue) -> zoomableScrollPane.zoomTo(newValue.floatValue()));

        bottomToolbar.getChildren().addAll(slider, routingTable, newLayout);
        root.setBottom(bottomToolbar);
        routingTable.setOnAction(e -> printRoutingTable());
        newLayout.setOnAction(e -> layout.execute());

        stage.setScene(scene);
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

    private void printRoutingTable() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Shortcut Messenger - Routing Table");
        alert.setHeaderText("Routing Table");

        TextArea textArea = new TextArea(localNode.getRoutingTable().toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 1);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }
}
