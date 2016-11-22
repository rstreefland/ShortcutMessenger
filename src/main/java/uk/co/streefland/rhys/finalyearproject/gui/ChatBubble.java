package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
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

        /* double width = 0;
        for (Node child: textFlow.getChildren()) {
            width += (child.getLayoutBounds().getWidth() * 1.30); // No idea why this works
        } // TODO: 22/11/2016 futher improve this horrible hack to correct the width of the emoji as the last node

        if (width >= maxWidth) {
            this.maxWidth(maxWidth);
        } else {
            setMaxWidth(width + 10); // or this
        } */

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

        getChildren().add(textFlow);
    }


    @Override
    public void layoutChildren() {

        super.layoutChildren();

        double maxChildWidth = 0;
        for (Node child : getChildren()) {
            double childWidth = child.getLayoutBounds().getWidth();
            maxChildWidth = Math.max(maxChildWidth, childWidth);
        }
        double insetWidth = getInsets().getLeft() + getInsets().getRight();
        double adjustedWidth = maxChildWidth + insetWidth;

        System.out.println("New width is: " + adjustedWidth);

        setMaxWidth(adjustedWidth);
    }
}


