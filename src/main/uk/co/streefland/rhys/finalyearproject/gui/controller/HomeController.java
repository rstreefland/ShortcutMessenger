package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class HomeController {

    private LocalNode localNode;
    private Map<String, ArrayList<String>> userMessages;

    private ObservableList<String> conversations;

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

        conversations = FXCollections.observableArrayList("test");
        listView.setItems(conversations);

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

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!conversations.contains(localNode.getMessages().getLastMessageUser())) {
                                    conversations.add(localNode.getMessages().getLastMessageUser());
                                    listView.getSelectionModel().select(localNode.getMessages().getLastMessageUser());
                                    currentConversationUser = localNode.getMessages().getLastMessageUser();
                                    changeConversation();
                                }
                            }
                        });


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

    @FXML
    private void newConversation(ActionEvent event) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("New conversation");
        dialog.setContentText("Username:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("Your name: " + result.get());
            if (!setUser(result.get())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User not found");
                alert.setContentText("User not found!");
                alert.showAndWait();
            }
        }
    }


    private boolean setUser(String userName) throws IOException {
        User user = localNode.getUsers().findUserOnNetwork(userName);

        if (user == null) {
            return false;
        }

        conversations.add(userName);
        listView.getSelectionModel().select(userName);
        changeConversation();
        return true;
    }
}
