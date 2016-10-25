package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;

import java.util.ArrayList;
import java.util.Map;

public class HomeController {

    private LocalNode localNode;
    private Map<String, ArrayList<String>> userMessages;

    private String currentConversationUser;
    private int currentConversationMessages;

    @FXML
    private ListView<String> listView;
    @FXML
    private GridPane textPane;

    public void init(LocalNode localNode) {
        this.localNode = localNode;
        this.userMessages = localNode.getMessages().getUserMessages();
        currentConversationMessages = 0;

        listView.setItems(FXCollections.observableArrayList(
                "one", "two", "Item 3", "Item 4"));

        currentConversationUser = "one";
        changeConversation();

        listView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {

                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        // change the label text value to the newly selected
                        // item.
                        currentConversationUser = newValue;
                        changeConversation();
                    }
                });


        localNode.getMessages().messageCountProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> o, Number oldVal,
                                        Number newVal) {

                        System.out.println("I IS HERE");

                        if (userMessages.get(currentConversationUser).size() != currentConversationMessages) {
                            ArrayList<String> conversation = userMessages.get(currentConversationUser);

                            if (conversation != null) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = currentConversationMessages; i < userMessages.get(currentConversationUser).size(); i++) {
                                            Text text = new Text(conversation.get(i));
                                            textPane.add(text, 1, i);
                                            currentConversationMessages++;
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void changeConversation() {
        textPane.getChildren().clear();
        currentConversationMessages = 0;

        if (currentConversationUser != null) {
            ArrayList<String> conversation = localNode.getMessages().getUserMessages().get(currentConversationUser);

            if (conversation != null) {
                for (int i = 0; i < conversation.size(); i++) {
                    Text text = new Text(conversation.get(i));
                    textPane.add(text, 1, i);
                    currentConversationMessages++;
                }
            }
        }
    }
}
