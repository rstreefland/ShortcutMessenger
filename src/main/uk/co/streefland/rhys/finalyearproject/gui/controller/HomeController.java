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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
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

    @FXML
    private ListView<String> listView;
    @FXML
    private GridPane textPane;

    public void init(LocalNode localNode) {
        this.localNode = localNode;
        this.userMessages = localNode.getMessages().getUserMessages();

        conversations = FXCollections.observableArrayList();
        listView.setItems(conversations);

        listView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<String>() {

                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        // change the label text value to the newly selected
                        // item.
                        changeConversation(newValue);
                    }
                });


        localNode.getMessages().lastMessageProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> o, String oldVal,
                                        String newVal) {

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                changeConversation(localNode.getMessages().getLastMessageUser());

                                String title = "New message from " + localNode.getMessages().getLastMessageUser();
                                NotificationType notification = NotificationType.INFORMATION;

                                Image image = new Image(getClass().getResource("../chatbubble.png").toExternalForm());
                                TrayNotification tray = new TrayNotification();
                                tray.setTitle(title);
                                tray.setMessage(newVal);
                                tray.setNotificationType(notification);
                                tray.setRectangleFill(Paint.valueOf("#000000"));
                                tray.setImage(image);
                                tray.setAnimationType(AnimationType.POPUP);
                                tray.showAndDismiss(Duration.seconds(3));
                            }
                        });

                        if (currentConversationUser == localNode.getMessages().getLastMessageUser()) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Text text = new Text(newVal);
                                    textPane.add(text, 1, textPane.getChildren().size() + 1);
                                }
                            });
                        }
                    }
                });
    }

    private void changeConversation(String userName) {

        if (!conversations.contains(userName)) {
            conversations.add(userName);
            return;
        }

        currentConversationUser = userName;

        textPane.getChildren().clear();

        if (currentConversationUser != null) {
            ArrayList<String> conversation = localNode.getMessages().getUserMessages().get(currentConversationUser);

            if (conversation != null) {
                for (int i = 0; i < conversation.size(); i++) {
                    Text text = new Text(conversation.get(i));
                    textPane.add(text, 1, i);
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
            if (!setConversationUser(result.get())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User not found");
                alert.setContentText("User not found!");
                alert.showAndWait();
            }
        }
    }


    private boolean setConversationUser(String userName) throws IOException {

        User user = localNode.getUsers().findUserOnNetwork(userName);

        if (user == null) {
            return false;
        }

        changeConversation(userName);
        return true;
    }
}
