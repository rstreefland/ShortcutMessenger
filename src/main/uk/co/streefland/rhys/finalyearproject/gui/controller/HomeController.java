package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;

public class HomeController {

    private LocalNode localNode;

    @FXML
    private Button btn1;
    @FXML
    private Text message;
    @FXML
    private ImageView spinner;

    public void init(LocalNode localNode) {
        this.localNode = localNode;

        localNode.getMessages().messageCountProperty().addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ObservableValue o, Object oldVal,
                                        Object newVal) {
                        System.out.println("New message received on JavaFx thread!");
                    }
                });
    }
}
