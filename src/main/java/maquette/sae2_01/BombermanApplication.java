package maquette.sae2_01;// BombermanApplication.java
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class BombermanApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("bomberman.css").toExternalForm());


        primaryStage.setTitle("Bomberman JavaFX - FXML Edition");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(1000);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Donner le focus au contrôleur pour la gestion des touches
        BombermanController controller = loader.getController();
        controller.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
