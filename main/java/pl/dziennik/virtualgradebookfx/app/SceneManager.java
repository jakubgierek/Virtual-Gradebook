package pl.dziennik.virtualgradebookfx.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static Stage primaryStage;
    private static final double APP_WIDTH = 1600;
    private static final double APP_HEIGHT = 900;

    private SceneManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setWidth(APP_WIDTH);
        primaryStage.setHeight(APP_HEIGHT);
        primaryStage.setMinWidth(APP_WIDTH);
        primaryStage.setMinHeight(APP_HEIGHT);
        primaryStage.centerOnScreen();
    }

    public static void switchTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), APP_WIDTH, APP_HEIGHT);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setWidth(APP_WIDTH);
            primaryStage.setHeight(APP_HEIGHT);
            primaryStage.setMinWidth(APP_WIDTH);
            primaryStage.setMinHeight(APP_HEIGHT);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}