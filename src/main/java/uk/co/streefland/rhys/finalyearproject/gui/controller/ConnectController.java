package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
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
 * This class will eventually
 */
public class ConnectController {

    private Main main;
    private LocalNode localNode;
    private IPTools ipTools;

    @FXML
    private BorderPane borderPane;
    @FXML
    private Button connectButton;
    @FXML
    private TextField networkIpField;
    @FXML
    private Text errorText;
    @FXML
    private ImageView loader;

    public void init(Main main) {
        this.main = main;

        try {
            ipTools = new IPTools();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the localNode and bootstraps the node to the network
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleConnectButtonAction() {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {

                String publicIpString = ipTools.getPublicIp();
                String privateIpString = ipTools.getPrivateIp();
                String networkIpString = networkIpField.getText();

                InetAddress publicIp = ipTools.validateAddress(publicIpString);
                InetAddress privateIp = ipTools.validateAddress(privateIpString);

                /* Special case for first node in the network */
                if (networkIpString.equals("first")) {
                    localNode = new LocalNode(ipTools);
                    localNode.first();
                    this.succeeded();
                    return null;
                }

                InetAddress networkIp = null;

                try {
                    networkIp = ipTools.validateAddress(networkIpString);
                } catch (UnknownHostException uho) {
                }

                if (networkIp != null) {
                    localNode = new LocalNode(ipTools);
                } else {
                    this.succeeded();
                    return "Invalid network address";
                }

                boolean error = localNode.bootstrap(new Node(new KeyId(), networkIp, networkIp, Configuration.DEFAULT_PORT, Configuration.DEFAULT_PORT));

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
                showLoginScene();
            }

            loader.setVisible(false);
        });
    }

    /**
     * Changes the current scene to the login scene
     */
    private void showLoginScene() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        System.out.println(getClass());
        Parent root = null;

        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LoginController controller =
                loader.getController();
        controller.init(localNode);

        main.setLocalNode(localNode);

        Stage stage;
        stage = (Stage) connectButton.getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
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
