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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.StoredTextMessage;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.gui.bubble.ChatBubble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class HomeController {

    private LocalNode localNode;
    private ObservableList<String> conversations;
    private String currentConversationUser;

    private String localUser;

    Image image = new Image(getClass().getResource("/uk/co/streefland/rhys/finalyearproject/gui/chatbubble.png").toExternalForm());

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
        this.localUser = localNode.getUsers().getLocalUser().getUserName();

        conversations = FXCollections.observableArrayList();
        listView.setItems(conversations);

        fromSavedState();

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
                        String messageString = message.getMessage();
                        String author = message.getAuthor();

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!conversations.contains(author) && !author.equals(localUser)) {
                                    conversations.add(author);
                                }

                                if (currentConversationUser != author && (author != localUser)) {
                                    createNotification(author, messageString);
                                }
                            }
                        });

                        if (currentConversationUser != null) {
                            if (currentConversationUser.equals(author) || currentConversationUser.equals(message.getRecipient())) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (author.equals(localUser)) {
                                            ChatBubble text = new ChatBubble(ChatBubble.COLOUR_GREEN);
                                            text.setText(author + ": " + messageString);

                                            textPane.add(text, 1, textPane.getChildren().size() + 1);
                                            GridPane.setHgrow(text, Priority.ALWAYS);
                                            GridPane.setHalignment(text, HPos.RIGHT);
                                        } else {
                                            ChatBubble text = new ChatBubble(ChatBubble.COLOUR_GREY);
                                            text.setText(author + ": " + messageString);

                                            textPane.add(text, 1, textPane.getChildren().size() + 1);
                                            GridPane.setHgrow(text, Priority.ALWAYS);
                                            GridPane.setHalignment(text, HPos.LEFT);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    public void fromSavedState() {
        Map<String, ArrayList<StoredTextMessage>> userMessages = localNode.getMessages().getUserMessages();

        if (userMessages != null) {
            for (Map.Entry entry : userMessages.entrySet()) {
                if (!conversations.contains(entry.getKey())) {
                    conversations.add((String) entry.getKey());
                }
            }
        }
    }

    @FXML
    private void newConversationDialog(ActionEvent event) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("New conversation");
        dialog.setContentText("Username:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            createConversationUser(result.get());
        }
    }


    private void createConversationUser(String userName) throws IOException {
        if (userName.equals(localNode.getUsers().getLocalUser())) {
            return;
        }

        spinner.setVisible(true);

        Task task = new Task() {
            @Override
            protected User call() throws Exception {
                User user = localNode.getUsers().findUserOnNetwork(userName);
                this.succeeded();
                return user;
            }
        };

        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                User user = (User) task.getValue(); // result of computation
                spinner.setVisible(false);

                if (user == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("User not found");
                    alert.setContentText("User not found!");
                    alert.showAndWait();
                } else {
                    changeConversation(userName);
                    listView.getSelectionModel().select(userName);
                }
            }
        });
    }

    private void changeConversation(String userName) {
        if (!userName.equals(localUser)) {

            if (!conversations.contains(userName)) {
                conversations.add(userName);
            }

            currentConversationUser = userName;

            textPane.getChildren().clear();

            if (currentConversationUser != null) {
                ArrayList<StoredTextMessage> conversation = localNode.getMessages().getUserMessages().get(currentConversationUser);

                if (conversation != null) {
                    for (int i = 0; i < conversation.size(); i++) {
                        if (conversation.get(i).getAuthor().equals(localUser)) {
                            ChatBubble text = new ChatBubble(ChatBubble.COLOUR_GREEN);
                            text.setText(conversation.get(i).getAuthor() + ": " + conversation.get(i).getMessage());

                            textPane.add(text, 1, i);
                            GridPane.setHgrow(text, Priority.ALWAYS);
                            GridPane.setHalignment(text, HPos.RIGHT);
                        } else {
                            ChatBubble text = new ChatBubble(ChatBubble.COLOUR_GREY);
                            text.setText(conversation.get(i).getAuthor() + ": " + conversation.get(i).getMessage());

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

    private void createNotification(String author, String message) {
        String title = "New message from " + author;
        TrayNotification tray = new TrayNotification();
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(NotificationType.INFORMATION);
        tray.setRectangleFill(Paint.valueOf("#000000"));
        tray.setImage(image);
        tray.setAnimationType(AnimationType.POPUP);
        tray.showAndDismiss(Duration.seconds(5));
    }

    public void handleKeyPressed(KeyEvent key) {
        if (key.getCode() == KeyCode.ENTER) {
            sendButton.fire();
        }
    }
}
