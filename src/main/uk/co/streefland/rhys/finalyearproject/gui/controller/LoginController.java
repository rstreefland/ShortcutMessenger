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
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController {

    private LocalNode localNode;

    @FXML
    private Button btn1;
    @FXML
    private TextField userNameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Text message;
    @FXML
    private ImageView spinner;

    public void init(LocalNode localNode) {
        this.localNode = localNode;
    }

    @FXML
    protected void handleRegisterButtonAction(ActionEvent event) throws IOException {
        spinner.setVisible(true);

        User user = new User(userNameInput.getText(), passwordInput.getText());

        if (localNode.getUsers().registerUser(user)) {
            message.setText("REGISTERED!");
        } else {
            message.setText("ERROR");
        }

        spinner.setVisible(false);
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                return null;
            }
        };

        final Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
