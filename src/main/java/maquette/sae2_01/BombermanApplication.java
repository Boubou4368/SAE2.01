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
import javafx.scene.Parent;


import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class BombermanApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("start.fxml"));
        Parent root = loader.load();
        root.getStylesheets().add(getClass().getResource("start.css").toExternalForm());
        Scene scene = new Scene(root);

        primaryStage.setTitle("Bomberman JavaFX - FXML Edition");
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Donner le focus au contrôleur pour la gestion des touches
        //MenuController controller = loader.getController();
        //controller.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
