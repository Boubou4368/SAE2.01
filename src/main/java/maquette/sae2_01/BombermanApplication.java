package maquette.sae2_01;// BombermanApplication.java
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
