package uk.co.streefland.rhys.finalyearproject.gui.chatbubble;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.util.Queue;

/**
 * Renders an input String as a chatbubble consisting of Text and Emoji objects.
 */
public class ChatBubble {

    public final static int COLOUR_GREY = 1;
    public final static int COLOUR_GREEN = 2;
    private FlowPane flowOutput;

    public ChatBubble(int colour) {
        flowOutput = new FlowPane();
        flowOutput.setPadding(new Insets(5));

        switch (colour) {
            case COLOUR_GREY:
                flowOutput.setStyle(
                        "-fx-background-radius: 1em;" +
                                "-fx-background-color: lightgrey;"
                );
                break;
            case COLOUR_GREEN:
                flowOutput.setStyle(
                        "-fx-background-radius: 1em;" +
                                "-fx-background-color: lightgreen;"
                );
                break;
            default:
                break;
        }

        flowOutput.setRowValignment(VPos.CENTER);
    }

    /** Converts a string into text and emoji nodes */
    public FlowPane convert(String text) {
        Queue<Object> obs = EmojiOne.getInstance().toEmojiAndText(text);

        /* If chatbubble only contains an emoji - make the emoji bigger and remove the border */
        if (obs.size() == 1 && obs.peek() instanceof Emoji) {
            Emoji emoji = (Emoji) obs.poll();
            flowOutput.getChildren().add(createEmojiNode(emoji, true));
            return flowOutput;
        }

        while (!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                String sentence = (String) ob;
                String[] words = sentence.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    Text word = new Text(words[i] + " ");
                    flowOutput.getChildren().add(word);
                }
            } else if (ob instanceof Emoji) {
                Emoji emoji = (Emoji) ob;
                flowOutput.getChildren().add(createEmojiNode(emoji, false));
            }
        }
        return flowOutput;
    }

    /** Creates an emoji node (ImageView) */
    private Node createEmojiNode(Emoji emoji, boolean loneEmoji) {
        ImageView imageView = new ImageView();

        imageView.setFitWidth(24);
        imageView.setFitHeight(24);

        if (loneEmoji) {
            imageView.setFitWidth(36);
            imageView.setFitHeight(36);
        } else {
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
        }

        imageView.setImage(ImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex())));
        return imageView;
    }

    /** Gets the image path of an emoji */
    private String getEmojiImagePath(String hexStr) {
        return Emoji.class.getResource("/emoji/" + hexStr + ".png").toExternalForm();
    }

    /** Calculates the correct width of the chatbubble based on the width of the child nodes.
     * This is needed otherwise the chatbubble will just expand to fill all of the width */
    public void fix() {
        double maxChildWidth = 0;

        flowOutput.applyCss();
        flowOutput.layout();

        for (Node child : flowOutput.getChildren()) {
            double childWidth = child.prefWidth(-1)*1.05;
            maxChildWidth = maxChildWidth + childWidth;
        }

        double insetWidth = flowOutput.getInsets().getLeft() + flowOutput.getInsets().getRight();
        double adjustedWidth = maxChildWidth + insetWidth;
        flowOutput.setMaxWidth(adjustedWidth);
    }
}
