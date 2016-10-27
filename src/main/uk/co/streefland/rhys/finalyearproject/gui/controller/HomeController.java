package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.StoredTextMessage;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.gui.bubble.BubbleSpec;
import uk.co.streefland.rhys.finalyearproject.gui.bubble.BubbledLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class HomeController {

    private LocalNode localNode;
    private ObservableList<String> conversations;
    private String currentConversationUser;

    @FXML
    private ListView<String> listView;
    @FXML
    private GridPane textPane;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private ImageView spinner;

    public void init(LocalNode localNode) {
        this.localNode = localNode;

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
                new ChangeListener<StoredTextMessage>() {
                    @Override
                    public void changed(ObservableValue<? extends StoredTextMessage> o, StoredTextMessage oldVal,
                                        StoredTextMessage newVal) {

                        StoredTextMessage message = newVal;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!conversations.contains(message.getAuthor()) && !message.getAuthor().equals(localNode.getUsers().getLocalUser().getUserName())) {
                                    conversations.add(message.getAuthor());
                                    return;
                                }

                                if (currentConversationUser != message.getAuthor() && message.getAuthor() != localNode.getUsers().getLocalUser().getUserName()) {
                                    String title = "New message from " + message.getAuthor();
                                    NotificationType notification = NotificationType.INFORMATION;
                                    Image image = new Image(getClass().getResource("/uk/co/streefland/rhys/finalyearproject/gui/chatbubble.png").toExternalForm());
                                    TrayNotification tray = new TrayNotification();
                                    tray.setTitle(title);
                                    tray.setMessage(message.getMessage());
                                    tray.setNotificationType(notification);
                                    tray.setRectangleFill(Paint.valueOf("#000000"));
                                    tray.setImage(image);
                                    tray.setAnimationType(AnimationType.POPUP);
                                    tray.showAndDismiss(Duration.seconds(5));
                                }
                            }
                        });

                        if (currentConversationUser.equals(message.getAuthor()) || currentConversationUser.equals(message.getRecipient())) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (message.getAuthor().equals(localNode.getUsers().getLocalUser().getUserName())) {
                                        BubbledLabel text = new BubbledLabel(BubbleSpec.FACE_RIGHT_CENTER);
                                        text.setText(message.getAuthor() + ": " + message.getMessage());
                                        text.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                                                null, null)));


                                        textPane.add(text, 1, textPane.getChildren().size() + 1);
                                        GridPane.setHgrow(text, Priority.ALWAYS);
                                        GridPane.setHalignment(text, HPos.RIGHT);

                                    } else {
                                        BubbledLabel text = new BubbledLabel(BubbleSpec.FACE_LEFT_CENTER);
                                        text.setText(message.getAuthor() + ": " + message.getMessage());
                                        text.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY,
                                                null, null)));


                                        textPane.add(text, 1, textPane.getChildren().size() + 1);
                                        GridPane.setHgrow(text, Priority.ALWAYS);
                                        GridPane.setHalignment(text, HPos.LEFT);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void changeConversation(String userName) {
        if (!userName.equals(localNode.getUsers().getLocalUser().getUserName())) {

            if (!conversations.contains(userName)) {
                conversations.add(userName);
                return;
            }

            currentConversationUser = userName;

            textPane.getChildren().clear();

            if (currentConversationUser != null) {
                ArrayList<StoredTextMessage> conversation = localNode.getMessages().getUserMessages().get(currentConversationUser);

                if (conversation != null) {
                    for (int i = 0; i < conversation.size(); i++) {
                        if (conversation.get(i).getAuthor().equals(localNode.getUsers().getLocalUser().getUserName())) {
                            BubbledLabel text = new BubbledLabel(BubbleSpec.FACE_RIGHT_CENTER);
                            text.setText(conversation.get(i).getAuthor() + ": " + conversation.get(i).getMessage());
                            text.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                                    null, null)));


                            textPane.add(text, 1, i);
                            GridPane.setHgrow(text, Priority.ALWAYS);
                            GridPane.setHalignment(text, HPos.RIGHT);

                        } else {
                            BubbledLabel text = new BubbledLabel(BubbleSpec.FACE_LEFT_CENTER);
                            text.setText(conversation.get(i).getAuthor() + ": " + conversation.get(i).getMessage());
                            text.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY,
                                    null, null)));


                            textPane.add(text, 1, i);
                            GridPane.setHgrow(text, Priority.ALWAYS);
                            GridPane.setHalignment(text, HPos.LEFT);
                        }
                    }
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
            if (!setConversationUser(result.get())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User not found");
                alert.setContentText("User not found!");
                alert.showAndWait();
            }
        }
    }


    private boolean setConversationUser(String userName) throws IOException {
        if (userName.equals(localNode.getUsers().getLocalUser())) {
            return false;
        }

        spinner.setVisible(true);
        User user = localNode.getUsers().findUserOnNetwork(userName);

        if (user == null) {
            return false;
        }

        changeConversation(userName);
        listView.getSelectionModel().select(userName);
        spinner.setVisible(false);
        return true;
    }

    @FXML
    private void sendMessage(ActionEvent event) throws IOException {
        if (currentConversationUser != null) {
            spinner.setVisible(true);
            String message = messageField.getText();
            messageField.clear();

            Task task = new Task() {
                @Override
                protected String call() throws Exception {
                    localNode.message(message, new User(currentConversationUser, ""));
                    this.succeeded();
                    return "";
                }
            };

            final Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    spinner.setVisible(false);
                }
            });
        }
    }

    public void handleKeyPressed(KeyEvent key) {
        if (key.getCode() == KeyCode.ENTER) {
            sendButton.fire();
        }
    }
}
