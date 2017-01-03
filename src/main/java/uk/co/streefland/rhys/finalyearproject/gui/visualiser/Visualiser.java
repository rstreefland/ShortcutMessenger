package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * Runs the NetworkTraversalOperation and generates a graph with the information
 */
public class Visualiser {

    private LocalNode localNode;
    private Stage stage;
    private Graph graph = new Graph();
    private Map<KeyId, List<Node>> nodeRoutingTables;

    public Visualiser(LocalNode localNode) {
        this.localNode = localNode;
        stage = new Stage();

        /* Placeholder for while the NetworkTraversalOperation is running */
        BorderPane root = new BorderPane();
        VBox vBox = new VBox(20);
        Image image = new Image("/graphics/loader.gif");
        ImageView imageView = new ImageView(image);
        vBox.getChildren().addAll(new Label("Generating network visualisation"), imageView);
        vBox.setAlignment(Pos.CENTER);
        root.setCenter(vBox);

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add("style.css");
        root.getStyleClass().add("vis");

        stage.setTitle("Network Visualisation");
        stage.setScene(scene);
        stage.getIcons().add(new Image("/graphics/icon.png"));
        stage.show();

        runTraversalOperation();
    }

    /** Invokes the NetworkTraversalOperation in a separate thread to avoid blocking the JavaFX thread */
    private void runTraversalOperation() {
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
            Map<KeyId, List<Node>> nodeRoutingTables = (Map<KeyId, List<Node>>) task.getValue(); // result of computation

            if (nodeRoutingTables != null) {
                this.nodeRoutingTables = nodeRoutingTables;
                generateGraph();
            }
        });
    }

    /** Generates the graph based on the information in nodeRoutingTables */
    private void generateGraph() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("vis");
        graph = new Graph();

        ZoomableScrollPane zoomableScrollPane = graph.getScrollPane();
        root.setCenter(zoomableScrollPane);

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add("style.css");

        addGraphComponents();

        RandomLayout layout = new RandomLayout(graph);
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

    /** Add nodes and edges to the graph */
    private void addGraphComponents() {
        Model model = graph.getModel();

        for (Map.Entry routingTable : nodeRoutingTables.entrySet()) {
            for (Node node : (ArrayList<Node>) routingTable.getValue()) {
                model.addCell(node.getNodeId(), node.getPublicInetAddress().getHostAddress());
                model.addEdge((KeyId) routingTable.getKey(), node.getNodeId());
            }
        }

        graph.endUpdate();
    }

    /** A dialog which displays a text representation of the routing table*/
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

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
