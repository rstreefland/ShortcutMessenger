package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;
import uk.co.streefland.rhys.finalyearproject.gui.Main;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectController {

    private LocalNode localNode;

    @FXML
    private Button btn1;
    @FXML
    private TextField localIpInput;
    @FXML
    private TextField networkIpInput;
    @FXML
    private TextField userNameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Text message;
    @FXML
    private ImageView spinner;

    public ConnectController() {

    }

    @FXML
    protected void handleConnectButtonAction(ActionEvent event) throws IOException {
        spinner.setVisible(true);
        message.setText("bootstrapping");

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
            message.setText("Invalid local IP address");
            spinner.setVisible(false);
            return;
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

            try {
                if (networkPort != 0) {
                    error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(networkIp), networkPort));
                } else {
                    error = localNode.bootstrap(new Node(new KeyId(), InetAddress.getByName(networkIp), 12345));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            /* Special case for first node in the network */
            if (networkIpInput.getText().equals("first")) {
                localNode.first();
            } else {
                message.setText("Invalid network IP address");
                spinner.setVisible(false);
                return;
            }
        }

        if (error) {
            message.setText("Failed to bootstrap to the specified network");
            spinner.setVisible(false);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../login.fxml"));
        Parent root = loader.load();

        LoginController controller =
                loader.getController();
        controller.init(localNode);

        Stage stage;
        stage = (Stage) btn1.getScene().getWindow();
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.show();
    }
}
