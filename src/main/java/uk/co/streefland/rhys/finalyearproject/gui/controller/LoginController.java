package uk.co.streefland.rhys.finalyearproject.gui.controller;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;

import java.io.IOException;

public class LoginController {

    private LocalNode localNode;

    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Text errorText;
    @FXML
    private ImageView loader;

    public void init(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Handles registering a user on the network
     *
     * @throws IOException
     */
    @FXML
    protected void handleRegisterButtonAction() throws IOException {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                if (localNode.getUsers().registerUser(userNameField.getText(), passwordField.getText())) {
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
     * Handles logging a user in on the network
     */
    @FXML
    protected void handleLoginButtonAction() {
        loader.setVisible(true);

        Task task = new Task() {
            @Override
            protected String call() throws Exception {
                if (localNode.getUsers().loginUser(userNameField.getText(), passwordField.getText())) {
                    this.succeeded();
                    return null;
                } else {
                    this.succeeded();
                    return "Invalid username/password";
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
        stage = (Stage) registerButton.getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        stage.getIcons().add(new Image("/icon6.png"));
        stage.show();
    }

    public void handleKeyPressed(KeyEvent key) {
        if (key.getCode() == KeyCode.ENTER) {
            loginButton.fire();
        }
    }
}
