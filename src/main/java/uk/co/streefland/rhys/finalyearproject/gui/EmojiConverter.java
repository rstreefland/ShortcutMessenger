package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Queue;

/**
 * Created by UltimateZero on 9/12/2016.
 */
public class EmojiConverter {

    @FXML
    private TextFlow flowOutput;

    public EmojiConverter() {
        flowOutput = new TextFlow();
    }

    @FXML
    public TextFlow convert(String text) {
        Queue<Object> obs = EmojiOne.getInstance().toEmojiAndText(text);

        if (obs.size() == 1 && obs.peek() instanceof Emoji) {
            Emoji emoji = (Emoji) obs.poll();
            flowOutput.getChildren().add(createEmojiNode(emoji, true));
            return flowOutput;
        }

        while (!obs.isEmpty()) {
            Object ob = obs.poll();
            if (ob instanceof String) {
                addText((String) ob);
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
        return Emoji.class.getResource("/png_64/" + hexStr + ".png").toExternalForm();
    }

    private void addText(String text) {
        Text textNode = new Text(text);
        flowOutput.getChildren().add(textNode);
    }
}
