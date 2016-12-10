package uk.co.streefland.rhys.finalyearproject.gui.visualiser;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class RectangleCell extends Cell {

    public RectangleCell(String id) {
        super(id);

        StackPane stackPane = new StackPane();
        Circle circle = new Circle();
        Text text = new Text(id);
        circle.setFill(Color.LIGHTGREY);
        circle.setStroke(Color.GRAY);
        circle.radiusProperty().set(text.getLayoutBounds().getWidth());
        stackPane.getChildren().addAll(circle, text);

        setView(stackPane);
    }
}