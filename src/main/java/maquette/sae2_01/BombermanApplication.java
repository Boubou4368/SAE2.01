package maquette.sae2_01;// BombermanApplication.java
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


public class BombermanApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("start.fxml"));
        Parent root = loader.load();
        root.getStylesheets().add(getClass().getResource("style/start.css").toExternalForm());
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
