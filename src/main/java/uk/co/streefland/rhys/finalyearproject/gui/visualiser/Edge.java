package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/** Represents an edge on the graph */
public class Edge extends Group {

    private Cell source;
    private Cell target;
    private Line line;

    public Edge(Cell source, Cell target) {
        this.source = source;
        this.target = target;

        source.addCellChild(target);
        target.addCellParent(source);

        line = new Line();
        line.setSmooth(true);
        line.setStroke(Color.GREEN);

        line.startXProperty().bind( source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2));
        line.startYProperty().bind( source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2));

        line.endXProperty().bind( target.layoutXProperty().add( target.getBoundsInParent().getWidth() / 2));
        line.endYProperty().bind( target.layoutYProperty().add( target.getBoundsInParent().getHeight() / 2));

        getChildren().add( line);
    }

    public Cell getSource() {
        return source;
    }

    public Cell getTarget() {
        return target;
    }
}