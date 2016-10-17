package uk.co.streefland.rhys.finalyearproject.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {

    private BorderPane border = new BorderPane();
    private VBox topContainer = new VBox();
    private VBox bottomContainer = new VBox();

    @Override
    public void start(Stage primaryStage) {

        // Set up MAIN MENU
        MenuBar mainMenu = new MenuBar();
        mainMenu.getStyleClass().add("menu");
        topContainer.getChildren().add(mainMenu);
        border.setTop(topContainer);

        // Create FILE sub-menu.
        Menu fileMenu = new Menu("File");
        MenuItem newConfig = new MenuItem("New configuration");
        MenuItem openConfig = new MenuItem("Open configuration file");
        MenuItem saveConfig = new MenuItem("Save");
        MenuItem saveConfigAs = new MenuItem("Save as");
        MenuItem exit = new MenuItem("Exit");
        fileMenu.getItems().addAll(newConfig, openConfig, saveConfig,
                saveConfigAs, exit);

        // Create VIEW sub-menu
        Menu viewMenu = new Menu("View");
        MenuItem displayConfig = new MenuItem("Display configuration");
        MenuItem editConfig = new MenuItem("Edit configuration");
        MenuItem lifeFormInfo = new MenuItem("Display life form info");
        MenuItem mapInfo = new MenuItem("Display map info");
        viewMenu.getItems().addAll(displayConfig, editConfig, lifeFormInfo,
                mapInfo);

        // Create EDIT sub-menu
        Menu editMenu = new Menu("Edit");
        MenuItem addLifeForm = new MenuItem("Add life form");
        MenuItem removeLifeForm = new MenuItem("Remove life form");
        MenuItem modifyLifeForm = new MenuItem("Modify life form");
        editMenu.getItems().addAll(addLifeForm, removeLifeForm, modifyLifeForm);

        // Create SIMULATION sub-menu
        Menu simulationMenu = new Menu("Simulation");
        MenuItem start = new MenuItem("Start");
        MenuItem stop = new MenuItem("Stop");
        MenuItem pauseRestart = new MenuItem("Pause/Restart");
        MenuItem reset = new MenuItem("Reset");
        MenuItem toggleMapDisplay = new MenuItem("Toggle map display");
        simulationMenu.getItems().addAll(start, stop, pauseRestart, reset,
                toggleMapDisplay);

        // Create HELP sub-menu.
        Menu helpMenu = new Menu("Help");
        MenuItem applicationInfo = new MenuItem("Application info");
        MenuItem authorInfo = new MenuItem("Author info");
        MenuItem aboutInfo = new MenuItem("About");
        helpMenu.getItems().addAll(applicationInfo, authorInfo, aboutInfo);
        mainMenu.getMenus().addAll(fileMenu, viewMenu, editMenu,
                simulationMenu, helpMenu);

        // Set up TOOLBAR
        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("background");
        bottomContainer.getChildren().add(toolBar);
        border.setBottom(bottomContainer);

        // Create TOOLBAR buttons
        Button startButton = new Button("Start");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        Button resetButton = new Button("Reset");
        toolBar.getItems().addAll(startButton, pauseButton, stopButton,
                resetButton);

        // Make the scene and apply CSS
        final Scene scene = new Scene(border, 600, 500);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        // Setup the Stage
        primaryStage.setTitle("FinalYearProject");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(500);
        primaryStage.show();

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