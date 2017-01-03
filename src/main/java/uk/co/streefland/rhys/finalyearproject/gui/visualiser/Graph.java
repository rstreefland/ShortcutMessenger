package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class Graph {

    private Model model;
    private Group canvas;
    private ZoomableScrollPane scrollPane;
    private MouseGestures mouseGestures;

    /**
     * the pane wrapper is necessary or else the scrollpane would always align
     * the top-most and left-most child to the top and left eg when you drag the
     * top child down, the entire scrollpane would move down
     */
    private Pane pane;

    public Graph() {
        this.model = new Model();
        canvas = new Group();
        pane = new Pane();

        canvas.getChildren().add(pane);

        mouseGestures = new MouseGestures(this);

        scrollPane = new ZoomableScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("vis");
    }

    public ZoomableScrollPane getScrollPane() {
        return scrollPane;
    }

    public Model getModel() {
        return model;
    }

    public void endUpdate() {
        // add components to graph pane
        pane.getChildren().addAll(model.getAddedEdges());
        pane.getChildren().addAll(model.getAddedCells());

        // remove components from graph pane
        pane.getChildren().removeAll(model.getRemovedCells());
        pane.getChildren().removeAll(model.getRemovedEdges());

        // enable dragging of cells
        for (Cell cell : model.getAddedCells()) {
            mouseGestures.makeDraggable(cell);
        }

        // every cell must have a parent, if it doesn't, then the graphParent is
        // the parent
        getModel().attachOrphansToGraphParent(model.getAddedCells());

        // remove reference to graphParent
        getModel().disconnectFromGraphParent(model.getRemovedCells());

        // merge added & removed cells with all cells
        getModel().merge();
    }

    public double getScale() {
        return this.scrollPane.getScaleValue();
    }
}