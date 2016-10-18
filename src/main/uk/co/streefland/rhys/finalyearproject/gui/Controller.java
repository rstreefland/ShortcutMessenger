package uk.co.streefland.rhys.finalyearproject.gui;

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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    private LocalNode localNode;

    private String reason;

    @FXML
    private Button btn1;

    @FXML
    private TextField localIpInput;

    @FXML
    private TextField networkIpInput;

    @FXML
    private Text message;

    public Controller() {

    }

    @FXML
    protected void handleConnectButtonAction(ActionEvent event) throws IOException {
        Task task = new Task() {
            @Override
            protected String call() throws Exception {

                boolean error = false;

                String localIp = null;
                int localPort = 0;

                final String ipPattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):?(\\d{1,5})?";
                final Pattern p = Pattern.compile(ipPattern);
                Matcher m = p.matcher(localIpInput.getText());

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

                m = p.matcher(networkIpInput.getText());

                if (m.matches()) {
                    if (m.group(1) != null) {
                        networkIp = m.group(1);
                        if (m.group(2) != null) {
                            networkPort = Integer.parseInt(m.group(2));
                        }
                    }
                } else {
                    /* Special case for first node in the network */
                    if (networkIpInput.getText().equals("first")) {
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
                    message.setText(errorMessage);
                } else {
                    Stage stage;
                    stage = (Stage) btn1.getScene().getWindow();
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("login.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Scene scene = new Scene(root, 500, 500);
                    stage.setScene(scene);
                    stage.show();
                }
            }
        });
    }

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event) {
        message.setText("hello");
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        System.out.println("Sign in button pressed");
    }
}
