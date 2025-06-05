package maquette.sae2_01;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Label;


public class MenuController {

    @FXML
    private Label titleLabel;

    @FXML
    public void initialize() {
        titleLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("menu.css").toExternalForm());
            }
        });
    }

    @FXML
    private void onPlay(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Bomberman");
            stage.setWidth(600);
            stage.setHeight(800);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            BombermanController controller = loader.getController();
            controller.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onQuit(ActionEvent event) {
        System.exit(0);
    }
}
