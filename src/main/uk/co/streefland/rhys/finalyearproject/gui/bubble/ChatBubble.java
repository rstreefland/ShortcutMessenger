package uk.co.streefland.rhys.finalyearproject.gui.bubble;

import javafx.geometry.Insets;
import javafx.scene.control.Label;

public class ChatBubble extends Label {

    public final static int COLOUR_GREY = 1;
    public final static int COLOUR_GREEN = 2;

    public ChatBubble(int colour) {
        super();

        setPadding(new Insets(5));

        switch (colour) {
            case COLOUR_GREY: this.setStyle(
                    "-fx-background-radius: 1em;" +
                            "-fx-background-color: lightgrey;"
            ); break;
            case COLOUR_GREEN: this.setStyle(
                    "-fx-background-radius: 1em;" +
                            "-fx-background-color: lightgreen;"
            ); break;
            default: break;
        }
    }
}
