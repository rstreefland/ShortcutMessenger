package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;

import java.util.Collection;

public class ChatBubble extends TextFlow {

    public final static int COLOUR_GREY = 1;
    public final static int COLOUR_GREEN = 2;

    public ChatBubble(TextFlow textFlow, int colour) {
        super();

        getChildren().add(textFlow);

        double width = 0;
        for (Node child: textFlow.getChildren()) {
            width += (child.getLayoutBounds().getWidth()*1.30); // No idea why this works
        }

        setMaxWidth(width + 15); // or this
        setPadding(new Insets(5));

        switch (colour) {
            case COLOUR_GREY:
                this.setStyle(
                        "-fx-background-radius: 1em;" +
                                "-fx-background-color: lightgrey;"
                );
                break;
            case COLOUR_GREEN:
                this.setStyle(
                        "-fx-background-radius: 1em;" +
                                "-fx-background-color: lightgreen;"
                );
                break;
            default:
                break;
        }
    }
}


