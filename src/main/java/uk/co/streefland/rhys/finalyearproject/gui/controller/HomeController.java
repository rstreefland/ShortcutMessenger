package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.Statistics;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.gui.EmojiConverter;
import uk.co.streefland.rhys.finalyearproject.gui.visualiser.Visualiser;
import uk.co.streefland.rhys.finalyearproject.message.content.StoredTextMessage;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.operation.SendMessageOperation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private LocalNode localNode;
    private ObservableList<String> conversations;
    private String currentConversationUser;
    private List<KeyId> currentConversationMessages;
    private String localUser;
    private boolean click;

    Image logo = new Image(getClass().getResource("/icon5.png").toExternalForm());

    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu settingsMenu;
    @FXML
    private Menu conversationsMenu;
    @FXML
    private Menu developerMenu;
    @FXML
    private Menu aboutMenu;
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

        /* ArrayList of messageID's */
        currentConversationMessages = new ArrayList<>();

        /* Attempt to load conversations from saved state */
        fromSavedState();

        /* Bind width of listView to 1/5th of the borderPane width (minimum width of 100px) */
        listView.prefWidthProperty().bind(borderPane.widthProperty().divide(5));

        /* Nasty little hack because for some reason JavaFX doesn't support onAction for Menus */
        MenuItem dummyMenuItem1 = new MenuItem();
        MenuItem dummyMenuItem2 = new MenuItem();
        MenuItem dummyMenuItem3 = new MenuItem();
        settingsMenu.getItems().add(dummyMenuItem1);
        conversationsMenu.getItems().add(dummyMenuItem2);
        aboutMenu.getItems().add(dummyMenuItem3);

        setEventListeners();
    }

    /**
     * Loads existing conversations if they exist
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
     * Sets the required event listeners
     */
    private void setEventListeners() {

        menuBar.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> click = true);

        /* Listener for settings menu */
        settingsMenu.showingProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue) {
                        settingsMenu.hide();

                        if (click) {
                            try {
                                settingsDialog();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


        /* Listener for conversations menu */
        conversationsMenu.showingProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue) {
                        conversationsMenu.hide();

                        if (click) {
                            try {
                                newConversationDialog();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        developerMenu.showingProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue) {
                        click = false;
                    }
                });

        /* Listener for about menu */
        aboutMenu.showingProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue) {
                        aboutMenu.hide();

                        if (click) {
                            try {
                                aboutDialog();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

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

                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm());
                dialogPane.getStyleClass().add("dialog-pane");

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
     * Handles a new message
     *
     * @param message
     */
    private void newMessage(StoredTextMessage message) {

        /* Extract message data */
        KeyId messageId = message.getMessageId();
        String messageString = message.getMessage();
        String author = message.getAuthor();
        SendMessageOperation.Status messageStatus = message.getMessageStatus();

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

    /**
     * Updates the delivery status (ticks) of a message
     * @param message
     */
    private void updateMessage(StoredTextMessage message) {
        if (message != null) {

        /* Extract message data */
            KeyId messageId = message.getMessageId();
            String messageString = message.getMessage();
            String author = message.getAuthor();
            SendMessageOperation.Status messageStatus = message.getMessageStatus();

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

    /**
     * Draws a single chat bubble to the gridPane
     * @param messageId
     * @param messageString
     * @param author
     * @param messageStatus
     */
    private synchronized void drawChatBubble(KeyId messageId, String messageString, String author, SendMessageOperation.Status messageStatus) {

        FlowPane output;
        Node emoji = null;

        if (author.equals(localUser)) {

            int index;

            if (currentConversationMessages.contains(messageId)) {
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

            if (messageStatus == SendMessageOperation.Status.FORWARDED) {
                ImageView tick = new ImageView("/singletick.png");
                status.getChildren().add(tick);
            } else if (messageStatus == SendMessageOperation.Status.DELIVERED) {
                ImageView tick = new ImageView("/doubletick.png");
                status.getChildren().add(tick);
            } else if (messageStatus == SendMessageOperation.Status.FAILED) {
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
                    localNode.getMessages().sendMessage(message, new User(currentConversationUser, ""));
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
     * Dialog to add a new conversation
     *
     * @throws IOException
     */
    @FXML
    private void newConversationDialog() throws IOException {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("New conversation");
        dialog.setContentText("Who would you like to message:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            createConversationUser(result.get());
        }
    }

    /**
     * About dialog
     *
     * @throws IOException
     */
    @FXML
    private void settingsDialog() throws IOException {
        Dialog dialog = new Dialog();
        dialog.setTitle("Settings");
        dialog.setHeaderText("Settings");

        GridPane content = new GridPane();
        content.vgapProperty().set(10);
        content.hgapProperty().set(10);
        content.setMaxWidth(Double.MAX_VALUE);

        content.add(new Label("Maximum connection attempts"), 1, 1);
        content.add(new Label("Operation timeout (ms)"), 1, 2);
        content.add(new Label("Response timeout (ms)"), 1, 3);
        content.add(new Label("Refresh interval (ms)"), 1, 4);

        TextField maxConnectionAttemptsInput = new TextField(localNode.getConfig().getMaxConnectionAttempts() + "");
        TextField operationTimeoutInput = new TextField(localNode.getConfig().getOperationTimeout() + "");
        TextField responseTimeoutInput = new TextField(localNode.getConfig().getResponseTimeout() + "");
        TextField refreshIntervalInput = new TextField(localNode.getConfig().getRefreshInterval() + "");

        content.add(maxConnectionAttemptsInput, 2, 1);
        content.add(operationTimeoutInput, 2, 2);
        content.add(responseTimeoutInput, 2, 3);
        content.add(refreshIntervalInput, 2, 4);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        dialogPane.getButtonTypes().add(ButtonType.NO);
        dialogPane.getButtonTypes().add(ButtonType.YES);
        Button discardButton = (Button) dialogPane.lookupButton(ButtonType.NO);
        Button applyButton = (Button) dialogPane.lookupButton(ButtonType.YES);

        applyButton.setText("Apply");
        discardButton.setText("Discard");

        applyButton.setOnAction(e -> {
            localNode.getConfig().setMaxConnectionAttempts(Integer.parseInt(maxConnectionAttemptsInput.getText()));
            localNode.getConfig().setOperationTimeout(Integer.parseInt(operationTimeoutInput.getText()));
            localNode.getConfig().setResponseTimeout(Integer.parseInt(responseTimeoutInput.getText()));
            localNode.getConfig().setRefreshInterval(Integer.parseInt(refreshIntervalInput.getText()));
        });

        dialog.show();
    }

    /**
     * Stats dialog
     *
     * @throws IOException
     */
    @FXML
    private void statsDialog() throws IOException {
        Dialog dialog = new Dialog();
        dialog.setTitle("Statistics");
        dialog.setHeaderText("Statistics");

        GridPane content = new GridPane();
        content.vgapProperty().set(10);
        content.hgapProperty().set(10);
        content.setMaxWidth(Double.MAX_VALUE);

        Statistics stats = localNode.getServer().getStats();

        updateStats(content, stats);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        final Timeline timeline = new Timeline( new KeyFrame( Duration.millis( 1000 ), event -> {
            updateStats(content, stats);

            if (dialogPane.getScene() != null) {
                dialogPane.getScene().getWindow().sizeToScene();
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        dialog.show();
    }

    private void updateStats(GridPane content, Statistics stats) {
        content.getChildren().clear();
        content.add(new Label("Bytes sent"), 1, 1);
        content.add(new Label("Bytes received"), 1, 2);
        content.add(new Label("Messages sent"), 1, 4);
        content.add(new Label("Messages received"), 1, 5);
        content.add(new Label("Last communication with network"), 1, 7);

        long currentTime = new Date().getTime();
        long seconds = (currentTime - stats.getLastCommunication()) / 1000;

        Label bytesSent = new Label(stats.getBytesSent() + "");
        Label bytesReceived = new Label(stats.getBytesReceived() + "");
        Label messagesSent = new Label(stats.getMessagesSent() + "");
        Label messagesReceived = new Label(stats.getMessagesReceived() + "");
        Label secondsAgo = new Label( seconds + " seconds ago");

        bytesSent.setStyle("-fx-text-fill: #9db4c0");
        bytesReceived.setStyle("-fx-text-fill: #9db4c0");
        messagesSent.setStyle("-fx-text-fill: #9db4c0");
        messagesReceived.setStyle("-fx-text-fill: #9db4c0");
        secondsAgo.setStyle("-fx-text-fill: #9db4c0");

        content.add(bytesSent, 2, 1);
        content.add(bytesReceived, 2, 2);
        content.add(messagesSent, 2, 4);
        content.add(messagesReceived, 2, 5);
        content.add(secondsAgo, 2, 7);
    }


    /**
     * About dialog
     *
     * @throws IOException
     */
    @FXML
    private void aboutDialog() throws IOException {
        Dialog dialog = new Dialog();
        dialog.setTitle("About");
        dialog.setHeaderText("Shortcut Messenger");

        StringBuilder sb = new StringBuilder("Shortcut Messenger build " + LocalNode.BUILD_NUMBER);
        sb.append("\nCreated by Rhys Streefland for CS3IP16");

        Hyperlink link = new Hyperlink("Shortcut Messenger is made possible by open source software.");
        link.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://rstreefland.github.io/shortcutmessengerweb/"));
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
        });

        GridPane content = new GridPane();
        content.vgapProperty().set(10);
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(new Label(sb.toString()), 1, 1);
        content.add(link, 1, 2);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = dialogPane.lookupButton(ButtonType.CLOSE);

        dialog.show();
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
        tray.setRectangleFill(Paint.valueOf("#253237"));
        tray.setImage(logo);
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

        if (click.getClickCount() == 3) {
            conversations.remove(listView.getSelectionModel().getSelectedItem());
            gridPane.getChildren().clear();
            currentConversationUser = null;
        }
    }

    @FXML
    public void menuBarClick() {
        this.click = true;
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

    @FXML
    private void openVisualiser() {
        Visualiser vis = new Visualiser(localNode);
    }

    /**
     * Deletes any existing saved state and force shuts down the program
     * @throws IOException
     */
    @FXML
    private void reset() throws IOException {
        StorageHandler temp = new StorageHandler();
        temp.delete();

        localNode.shutdown(false);
        System.exit(0);
    }
}
