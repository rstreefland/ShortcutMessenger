package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.User;

import java.io.IOException;

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

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                User user = new User(userNameInput.getText(), passwordInput.getText());
                if (localNode.getUsers().registerUser(user)) {
                    this.succeeded();
                    return null;
                } else {
                    this.succeeded();
                    return "User already exists";
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
                    spinner.setVisible(false);
                    message.setText(errorMessage);
                } else {
                    spinner.setVisible(false);
                }
            }
        });
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        spinner.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                User user = new User(userNameInput.getText(), passwordInput.getText());
                if (localNode.getUsers().loginUser(user, passwordInput.getText())) {
                    this.succeeded();
                    return null;
                } else {
                    this.succeeded();
                    return "Invalid username/password";
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
                    spinner.setVisible(false);
                    message.setText(errorMessage);
                } else {
                    spinner.setVisible(false);
                }
            }
        });
    }
}
