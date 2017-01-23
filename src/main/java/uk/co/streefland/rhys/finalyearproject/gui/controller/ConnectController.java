package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.Encryption;
import uk.co.streefland.rhys.finalyearproject.core.IPTools;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.gui.Main;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Facilitates connecting to a network by specifying their local IP address and the target IP address to bootstrap to.
 */
public class ConnectController {

    private Main main;
    private LocalNode localNode;
    private IPTools ipTools;

    @FXML
    private HBox internetError;
    @FXML
    private Button connectButton;
    @FXML
    private Button advancedButton;
    @FXML
    private TextField networkIpField;
    @FXML
    private Text errorText;
    @FXML
    private ImageView loader;
    @FXML
    private GridPane gridPane;
    @FXML
    private HBox buttonBox;
    @FXML
    private Label userNameLabel;
    @FXML
    private TextField userNameField;

    private final Label networkPort = new Label("Network port:");
    private final TextField networkPortField = new TextField();
    private final Label localPort = new Label("Local port:");
    private final TextField localPortField = new TextField();

    private String userName;

    public void init(Main main) {
        this.main = main;

        try {
            ipTools = new IPTools();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!ipTools.isConnected()) {
            internetError.setVisible(true);
        }

        try {
            userName = Encryption.getUserName();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (userName != null) {
            replaceUserNameField();
        }
    }

    /**
     * Creates the localNode and bootstraps the node to the network
     *
     * @throws IOException
     */
    @FXML
    private void handleConnectButtonAction() {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                String networkIpString = networkIpField.getText();
                int networkPort = Configuration.DEFAULT_PORT;
                int localPort = Configuration.DEFAULT_PORT;

                try {
                    networkPort = Integer.parseInt(networkPortField.getText());
                } catch (NumberFormatException ignored) {}

                try {
                    localPort = Integer.parseInt(localPortField.getText());
                } catch (NumberFormatException ignored) {}

                /* Special case for first node in the network */
                if (networkIpString.equals("first")) {
                    localNode = new LocalNode(ipTools, localPort);
                    localNode.first();
                    this.succeeded();
                    return null;
                }

                /* Convert the network IP/URL into an InetAddress */
                InetAddress networkIp = null;
                try {
                    networkIp = ipTools.validateAddress(networkIpString);
                } catch (UnknownHostException ignored) {
                }

                /* Create the localNode object */
                if (networkIp != null) {
                    localNode = new LocalNode(ipTools, localPort);
                } else {
                    this.succeeded();
                    return "Invalid network address";
                }

                /* Attempt to bootstrap to the network */
                boolean error = localNode.bootstrap(new Node(new KeyId(), networkIp, networkIp, networkPort, networkPort));

                if (error) {
                    this.succeeded();
                    localNode.shutdown(false);
                    localNode = null;
                    return "Failed to bootstrap to the specified network";
                } else {
                    this.succeeded();
                    return null;
                }
            }
        };

        /* Run the task in a separate thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* On task finish */
        task.setOnSucceeded(event1 -> {
            String errorMessage = (String) task.getValue(); // result of computation

            if (errorMessage != null) {
                errorText.setText(errorMessage);
            } else {
                if (userName != null) {
                    login();
                } else {
                    register();
                }
            }

            loader.setVisible(false);
            main.setLocalNode(localNode);
        });
    }

    private void replaceUserNameField() {
        userNameField.setDisable(true);
        userNameField.setText(userName);
    }

    @FXML
    private void handleAdvancedButtonAction() {
        if (advancedButton.getText().equals("Advanced")) {
            gridPane.getChildren().remove(buttonBox);

            localPortField.setPromptText("12345");
            networkPortField.setPromptText("12345");

            gridPane.add(networkPort, 1, 4);
            gridPane.add(networkPortField, 2, 4);
            gridPane.add(localPort, 1, 5);
            gridPane.add(localPortField, 2, 5);

            gridPane.add(buttonBox, 2, 6);
            advancedButton.setText("Simple");
        } else {
            gridPane.getChildren().removeAll(buttonBox, localPort, localPortField, networkPort, networkPortField);
            gridPane.add(buttonBox, 2, 4);
            advancedButton.setText("Advanced");
        }
    }

    /**
     * Handles logging a user in on the network
     */
    protected void login() {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                if (localNode.getUsers().loginUser(userName)){
                    this.succeeded();
                    return null;
                } else {
                    this.succeeded();
                    return "Invalid keyfile for username";
                }
            }
        };

        /* Run the task in a separate thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* On task finish */
        task.setOnSucceeded(event1 -> {
            String errorMessage = (String) task.getValue(); // result of computation

            if (errorMessage != null) {
                errorText.setText(errorMessage);
            } else {
                showHomeScene();
            }
            loader.setVisible(false);
        });
    }

    @FXML
    protected void register() {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                if (localNode.getUsers().registerUser(userNameField.getText())) {
                    this.succeeded();
                    return null;
                } else {
                    this.succeeded();
                    return "User already exists";
                }
            }
        };

        /* Run the task in a separate thread to avoid blocking the JavaFX thread */
        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        /* On task finish */
        task.setOnSucceeded(event1 -> {
            String errorMessage = (String) task.getValue(); // result of computation

            if (errorMessage != null) {
                errorText.setText(errorMessage);
            } else {
                showHomeScene();
            }
            loader.setVisible(false);
        });
    }

    /**
     * Changes the current scene to the login scene
     */
    private void showHomeScene() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
        Parent root = null;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HomeController controller =
                loader.getController();
        controller.init(localNode);

        Stage stage;
        stage = (Stage) connectButton.getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        stage.getIcons().add(new Image("/graphics/icon.png"));
        stage.show();
    }

    /**
     * Fires the connect button event handler if the enter key was pressed
     *
     * @param key The key event
     */
    @FXML
    private void handleKeyPressed(KeyEvent key) {
        if (key.getCode() == KeyCode.ENTER) {
            connectButton.fire();
        }
    }
}
