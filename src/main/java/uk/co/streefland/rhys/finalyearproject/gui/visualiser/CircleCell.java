package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;

/** Circle layout for graph cell */
class CircleCell extends Cell {

    public CircleCell(KeyId id, String name) {
        super(id);

        StackPane stackPane = new StackPane();
        Circle circle = new Circle();
        Text text = new Text(name);
        circle.setFill(Color.LIGHTGREEN);
        circle.setStroke(Color.GREEN);
        circle.radiusProperty().set(text.getLayoutBounds().getWidth()/1.5);
        stackPane.getChildren().addAll(circle, text);

        setView(stackPane);
    }
}