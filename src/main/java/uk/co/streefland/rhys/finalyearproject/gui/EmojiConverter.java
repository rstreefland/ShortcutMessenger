package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.util.Queue;

/**
 * Created by UltimateZero on 9/12/2016.
 */
public class EmojiConverter {

    public final static int COLOUR_GREY = 1;
    public final static int COLOUR_GREEN = 2;

    @FXML
    private FlowPane flowOutput;

    public EmojiConverter(int colour) {
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

    @FXML
    public FlowPane convert(String text, double wrapWidth) {
        Queue<Object> obs = EmojiOne.getInstance().toEmojiAndText(text);

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

    private String getEmojiImagePath(String hexStr) {
        return Emoji.class.getResource("/emoji/" + hexStr + ".png").toExternalForm();
    }

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
