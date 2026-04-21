package pl.dziennik.virtualgradebookfx.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        SceneManager.switchTo("/fxml/login-view.fxml", "Logowanie");
    }

    public static void main(String[] args) {
        launch(args);
    }
}