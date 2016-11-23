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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Collection;

public class ChatBubble extends TextFlow {
    
    public final static int COLOUR_GREY = 1;
    public final static int COLOUR_GREEN = 2;

    public ChatBubble(FlowPane textFlow, int colour) {
        super();
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

        double maxChildWidth = 0;
        for (Node child : textFlow.getChildren()) {
            double childWidth = child.getLayoutBounds().getWidth();
            maxChildWidth = maxChildWidth + childWidth;
        }
        double insetWidth = textFlow.getInsets().getLeft() + textFlow.getInsets().getRight();
        double adjustedWidth = maxChildWidth + insetWidth;

        System.out.println("CHATBUBBLE width is " + adjustedWidth);
        textFlow.maxWidth(adjustedWidth);

        setMaxWidth((adjustedWidth*1.25) + 10);
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

        maxWidth(adjustedWidth);
    }
}


