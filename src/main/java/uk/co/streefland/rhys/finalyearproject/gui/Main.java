package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.Configuration;
import uk.co.streefland.rhys.finalyearproject.core.IPTools;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;
import uk.co.streefland.rhys.finalyearproject.gui.controller.ConnectController;
import uk.co.streefland.rhys.finalyearproject.gui.controller.HomeController;

import javax.swing.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * The starting point for the JavaFX application
 */
public class Main extends Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalNode localNode = null;

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String java = System.getProperty("java.specification.version");
        double version = Double.valueOf(java);
        if (version < 1.8) {
            JOptionPane
                    .showMessageDialog(
                            null,
                            "Java 8 is required to run this application!\nPlease install JRE 8 and try again...",
                            "Error", JOptionPane.ERROR_MESSAGE);
            Platform.exit();
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        /* Load in custom fonts */
        Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto-Light.ttf"), 14);

        Parent root;

        logger.info("Shortcut Messenger UI");

        try {
            /* Check if we can load the saved state from the file and show the relevant scene */
            if (StorageHandler.doesSavedStateExist()) {
                try {
                    IPTools ipTools = new IPTools();
                    localNode = new LocalNode(ipTools, Configuration.DEFAULT_PORT);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
                    root = loader.load();

                    HomeController controller =
                            loader.getController();
                    controller.init(localNode);
                } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                    localNode = null;
                    StorageHandler.delete(); // Delete the dodgy saved state file - the next run will take care of the encryption keys if needed
                    root = showConnectScreen();
                    errorDialog();
                }
            } else {
                root = showConnectScreen();
            }

            Scene scene = new Scene(root, 650, 500);
            stage.setTitle("Shortcut Messenger");
            stage.setScene(scene);
            stage.setMinHeight(420);
            stage.setMinWidth(500);
            stage.getIcons().add(new Image("/graphics/icon.png"));
            stage.show();

        } catch (IOException e) {
            logger.error("Launch error: {}", e);
        }
    }

    /** Loads in the connect scene */
    private Parent showConnectScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/connect.fxml"));
        Parent root = loader.load();

        ConnectController controller =
                loader.getController();

        controller.init(this);

        return root;
    }

    /**
     * Error dialog which is shown if a saved state could not be loaded
     * @throws IOException
     */
    private void errorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fatal Error");
        alert.setHeaderText("Failed to load saved data or corresponding private key");
        alert.setContentText("Please re-connect to the network");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    /**
     * Shuts down the server cleanly and saves the state if appropriate
     */
    @Override
    public void stop() {
        if (localNode != null) {
            try {
                if (localNode.getUsers().getLocalUser() == null) {
                    localNode.shutdown(false);
                } else {
                    localNode.shutdown(true);
                }
            } catch (IOException e) {
                logger.error("Failed to shutdown cleanly - forcing a shutdown instead");
                System.exit(-1);
            }
        }
    }

    public void setLocalNode(LocalNode localNode) {
        this.localNode = localNode;
    }
}