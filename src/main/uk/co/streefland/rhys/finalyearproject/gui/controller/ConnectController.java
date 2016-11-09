package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.gui.Main;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facilitates connecting to a network by specifying their local IP address and the target IP address to bootstrap to.
 * This class will eventually
 */
public class ConnectController {

    private Main main;
    private LocalNode localNode;

    @FXML
    private Button connectButton;
    @FXML
    private TextField localIpField;
    @FXML
    private TextField networkIpField;
    @FXML
    private Text errorText;
    @FXML
    private ImageView loadingAnimation;

    public void init(Main main) {
        this.main = main;
    }

    @FXML
    protected void handleConnectButtonAction(ActionEvent event) {
        loadingAnimation.setVisible(true);
        Task task = new Task() {
            @Override
            protected String call() throws Exception {

                boolean error = false;

                String localIp = null;
                int localPort = 0;

                final String ipPattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):?(\\d{1,5})?";
                final Pattern p = Pattern.compile(ipPattern);
                Matcher m = p.matcher(localIpField.getText());

                if (m.matches()) {
                    if (m.group(1) != null) {
                        localIp = m.group(1);
                        if (m.group(2) != null) {
                            localPort = Integer.parseInt(m.group(2));
                        }
                    }
                } else {
                    this.succeeded();
                    return "Invalid local IP address";
                }

                try {
                    if (localNode == null) {
                        localNode = new LocalNode(localIp, localPort);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String networkIp = null;
                int networkPort = 0;

                m = p.matcher(networkIpField.getText());

                if (m.matches()) {
                    if (m.group(1) != null) {
                        networkIp = m.group(1);
                        if (m.group(2) != null) {
                            networkPort = Integer.parseInt(m.group(2));
                        }
                    }
                } else {
                    /* Special case for first node in the network */
                    if (networkIpField.getText().equals("first")) {
                        localNode.first();
                        this.succeeded();
                        return null;
                    }
                    this.succeeded();
                    return "Invalid network IP address";
                }
                try {
                    if (networkPort != 0) {
                        error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(networkIp), networkPort));
                    } else {
                        error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(networkIp), 12345));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (error) {
                    this.succeeded();
                    return "Failed to bootstrap to the specified network";
                } else {
                    this.succeeded();
                    return null;
                }
            }
        };

        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

                String errorMessage = (String) task.getValue(); // result of computation

                if (errorMessage != null) {
                    loadingAnimation.setVisible(false);
                    errorText.setText(errorMessage);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/uk/co/streefland/rhys/finalyearproject/gui/view/login.fxml"));
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
                    Scene scene = new Scene(root, 500, 500);
                    stage.setScene(scene);
                    stage.show();
                }
            }
        });
    }

    public void handleKeyPressed(KeyEvent key)
    {
        if(key.getCode() == KeyCode.ENTER)
        {
            connectButton.fire();
        }
    }
}
