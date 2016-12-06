package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.core.IPTools;
import uk.co.streefland.rhys.finalyearproject.core.LocalNode;
import uk.co.streefland.rhys.finalyearproject.core.StorageHandler;
import uk.co.streefland.rhys.finalyearproject.gui.controller.ConnectController;
import uk.co.streefland.rhys.finalyearproject.gui.controller.HomeController;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;

/**
 * The starting point for the JavaFX application
 */
public class Main extends Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    LocalNode localNode = null;

    private int width = 650;
    private int height = 500;

    @Override
    public void start(Stage stage) throws IOException {

        /* Load in custom fonts */
        Font.loadFont(getClass().getResourceAsStream("/Roboto-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/Roboto-Light.ttf"), 14);

        StorageHandler temp = new StorageHandler();
        Parent root;

        logger.info("Shortcut Messenger UI v1.0");

        /* Check if we can load the saved state from the file and show the relevant scene */
        if (temp.doesSavedStateExist()) {
            IPTools ipTools = new IPTools();
            localNode = new LocalNode(ipTools, InetAddress.getByName(""), InetAddress.getByName(""), 0);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
            root = loader.load();

            HomeController controller =
                    loader.getController();
            controller.init(localNode);

        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/connect.fxml"));
            root = loader.load();

            ConnectController controller =
                    loader.getController();
            controller.init(this);
        }


        Scene scene = new Scene(root, width, height);
        stage.setTitle("Shortcut Messenger");
        stage.setScene(scene);
        stage.setMinHeight(420);
        stage.setMinWidth(500);
        stage.getIcons().add(new Image("/icon6.png"));
        stage.show();
    }

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
    public void stop(){
        if (localNode != null) {
            localNode.shutdown(false); // TODO: 16/11/2016 change this back to true once you've fixed the saving and loading
        }
    }

    public void setLocalNode(LocalNode localNode) {
        this.localNode = localNode;
    }
}