package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.gui.EmojiConverter;
import uk.co.streefland.rhys.finalyearproject.message.content.StoredTextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private LocalNode localNode;
    private ObservableList<String> conversations;
    private String currentConversationUser;
    private List<KeyId> currentConversationMessages;
    private String localUser;

    Image image = new Image(getClass().getResource("/chatbubble.png").toExternalForm());

    @FXML
    private ListView<String> listView;
    @FXML
    private GridPane gridPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField messageField;
    @FXML
    private HBox conversationHeader;

    public void init(LocalNode localNode) {
        this.localNode = localNode;
        this.localUser = localNode.getUsers().getLocalUser().getUserName();

        /* Observable ArrayList of conversations */
        conversations = FXCollections.observableArrayList();
        listView.setItems(conversations);

        currentConversationMessages = new ArrayList<>();

        // Attempt to load conversations from saved state
        fromSavedState();

        /* Listener for current conversation ListView */
        listView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    // change the label text value to the newly selected
                    // item.
                    if (newValue != null) {
                        changeConversation(newValue);
                    }
                });

        /* Listener for new message */
        localNode.getMessages().lastMessageProperty().addListener(
                (o, oldVal, newVal) -> {
                    newMessage(newVal);
                });

        /* Listener for updated message status */
        localNode.getMessages().lastMessageUpdateProperty().addListener(
                (o, oldVal, newVal) -> {
                    updateMessage(newVal);
                });

        /* Listener for auto scroll of ScrollPane- - when a new message is received */
        gridPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            gridPane.layout();
            scrollPane.setVvalue(1.0d);  // scroll to bottom
        });
    }

    /**
     * Handles a new message
     *
     * @param message
     */
    private void newMessage(StoredTextMessage message) {

        /* Extract message data */
        KeyId messageId = message.getMessageId();
        String messageString = message.getMessage();
        String author = message.getAuthor();
        int messageStatus = message.getMessageStatus();

        /* Run on the JavaFX thread */
        Platform.runLater(() -> {
            /* If the message author is not in the conversation list - add them */
            if (!conversations.contains(author) && !author.equals(localUser)) {
                conversations.add(author);
            }

            /* Show a notification only if the message isn't destined for the current conversation */
            if (currentConversationUser != null) {
                if (!currentConversationUser.equals(author) && !author.equals(localUser)) {
                    createNotification(author, messageString);
                }
            } else {
                createNotification(author, messageString);
            }
        });

        /* If the message is for the current conversation - add it to the conversation */
        if (currentConversationUser != null) {
            if (currentConversationUser.equals(author) || currentConversationUser.equals(message.getRecipient())) {
                Platform.runLater(() -> {
                    drawChatBubble(messageId, messageString, author, messageStatus);
                    currentConversationMessages.add(messageId);
                });
            }
        }
    }

    private void updateMessage(StoredTextMessage message) {
        if (message != null) {

        /* Extract message data */
            KeyId messageId = message.getMessageId();
            String messageString = message.getMessage();
            String author = message.getAuthor();
            int messageStatus = message.getMessageStatus();

         /* If the message is for the current conversation - add it to the conversation */
            if (currentConversationUser != null) {
                if (currentConversationUser.equals(author) || currentConversationUser.equals(message.getRecipient())) {
                    Platform.runLater(() -> {
                        drawChatBubble(messageId, messageString, author, messageStatus);
                    });
                }
            }
        }
    }

    public void drawChatBubble(KeyId messageId, String messageString, String author, int messageStatus) {

        FlowPane output;
        Node emoji = null;

        if (author.equals(localUser)) {

            int index;

            if (currentConversationMessages.contains(messageId))  {
             index = currentConversationMessages.indexOf(messageId);
                gridPane.getChildren().remove(index);
            } else {
                index = gridPane.getChildren().size();
            }

            EmojiConverter emojiConverter = new EmojiConverter(EmojiConverter.COLOUR_GREEN);
            output = emojiConverter.convert(messageString, gridPane.getWidth() / 2);

            if (output.getChildren().size() == 1) {
                if (output.getChildren().get(output.getChildren().size() - 1) instanceof ImageView) {
                    emoji = output.getChildren().get(output.getChildren().size() - 1);
                }
            }

            HBox master = new HBox();
            HBox status = new HBox();

            if (messageStatus == SendMessageOperation.FORWARDED) {
                ImageView tick = new ImageView("/singletick.png");
                status.getChildren().add(tick);
            } else if (messageStatus == SendMessageOperation.DELIVERED) {
                ImageView tick = new ImageView("/doubletick.png");
                status.getChildren().add(tick);
            } else if (messageStatus == SendMessageOperation.FAILED) {
                output.setStyle(
                        "-fx-background-radius: 1em;" +
                                "-fx-background-color: red;"
                );
            }

            status.setAlignment(Pos.BOTTOM_RIGHT);

            if (emoji != null) {
                master.getChildren().addAll(emoji, status);
            } else {
                master.getChildren().addAll(output, status);
            }

            master.setAlignment(Pos.CENTER_RIGHT);

            gridPane.add(master, 1, index);
            GridPane.setHgrow(master, Priority.ALWAYS);
            GridPane.setHalignment(master, HPos.RIGHT);
            gridPane.layout();
            emojiConverter.fix();

        } else {
            EmojiConverter emojiConverter = new EmojiConverter(EmojiConverter.COLOUR_GREY);
            output = emojiConverter.convert(messageString, gridPane.getWidth() / 2);

            if (output.getChildren().size() == 1) {
                if (output.getChildren().get(output.getChildren().size() - 1) instanceof ImageView) {
                    emoji = output.getChildren().get(output.getChildren().size() - 1);
                }
            }

            if (emoji != null) {
                gridPane.add(emoji, 1, gridPane.getChildren().size());
                GridPane.setHgrow(emoji, Priority.ALWAYS);
                GridPane.setHalignment(emoji, HPos.LEFT);
            } else {
                gridPane.add(output, 1, gridPane.getChildren().size());
                GridPane.setHgrow(output, Priority.ALWAYS);
                GridPane.setHalignment(output, HPos.LEFT);
                gridPane.layout();
                emojiConverter.fix();
            }
        }
    }

    /**
     * Attempts to load the conversations from the saved state
     */

    private void fromSavedState() {
        Map<String, ArrayList<StoredTextMessage>> userMessages = localNode.getMessages().getUserMessages();

        if (userMessages != null) {
            for (Map.Entry entry : userMessages.entrySet()) {
                if (!conversations.contains(entry.getKey())) {
                    conversations.add((String) entry.getKey());
                }
            }
        }
    }

    /**
     * Dialog to add a new conversation
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void newConversationDialog(ActionEvent event) throws IOException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("New conversation");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            createConversationUser(result.get());
        }
    }


    /**
     * Adds a new user to the conversation ListView
     *
     * @param userName
     * @throws IOException
     */
    private void createConversationUser(String userName) throws IOException {

        /* Don't add the local user */
        if (userName.equals(localNode.getUsers().getLocalUser())) {
            return;
        }

        /* Find the user object on the network */
        Task task = new Task() {
            @Override
            protected User call() throws Exception {
                User user = localNode.getUsers().findUserOnNetwork(userName);
                this.succeeded();
                return user;
            }
        };

        /* Run task in different thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* When the task had finished */
        task.setOnSucceeded(event -> {
            User user = (User) task.getValue(); // result of computation

            if (user == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User not found");
                alert.setContentText("User not found!");
                alert.showAndWait();
            } else {
                /* Change the current conversation to the new user */
                listView.getSelectionModel().select(userName);
            }
        });
    }

    /**
     * Changes the conversation
     *
     * @param userName
     */
    private void changeConversation(String userName) {
        if (!userName.equals(localUser)) {

            conversationHeader.getChildren().clear();
            Text conversationHeaderText = new Text(userName);
            HBox.setHgrow(conversationHeaderText, Priority.ALWAYS);
            conversationHeader.getChildren().add(conversationHeaderText);
            conversationHeader.setPadding(new Insets(10));

            if (!conversations.contains(userName)) {
                conversations.add(userName);
            }

            currentConversationUser = userName;

            gridPane.getChildren().clear();
            currentConversationMessages.clear();

            if (currentConversationUser != null) {
                ArrayList<StoredTextMessage> conversation = localNode.getMessages().getUserMessages().get(currentConversationUser);

                if (conversation != null) {
                    for (int i = 0; i < conversation.size(); i++) {
                        drawChatBubble(conversation.get(i).getMessageId(), conversation.get(i).getMessage(), conversation.get(i).getAuthor(), conversation.get(i).getMessageStatus());
                        currentConversationMessages.add(conversation.get(i).getMessageId());
                    }
                }
            }
        }
    }

    /**
     * Sends a message to the currentConversationUser
     *
     * @throws IOException
     */
    @FXML
    private void sendMessage() throws IOException {
        if (currentConversationUser != null) {
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
        }
    }

    /**
     * Creates a notification for a new message
     *
     * @param author
     * @param message
     */
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

    /**
     * Removes a conversation from the list when it is double clicked
     *
     * @param click
     */
    @FXML
    public void onMouseClick(MouseEvent click) {
        if (click.getClickCount() == 2) {
            conversations.remove(listView.getSelectionModel().getSelectedItem());
            gridPane.getChildren().clear();
            currentConversationUser = null;
        }
    }

    /**
     * Sends a message when the enter key is pressed
     *
     * @param key
     */
    public void handleKeyPressed(KeyEvent key) {
        if (key.getCode() == KeyCode.ENTER) {
            try {
                sendMessage();
            } catch (IOException e) {
                logger.error("Could not send message {}", e);
            }
        }
    }
}