package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println(Font.loadFont(getClass().getResourceAsStream("Roboto-Regular.ttf"), 14));
        System.out.println(Font.loadFont(getClass().getResourceAsStream("Roboto-Light.ttf"), 14));
        System.out.println(Font.loadFont(getClass().getResourceAsStream("Roboto-Bold.ttf"), 14));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("connect.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 500);

        stage.setTitle("FinalYearProject");
        stage.setScene(scene);
        stage.setMinHeight(500);
        stage.setMinWidth(500);
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
}