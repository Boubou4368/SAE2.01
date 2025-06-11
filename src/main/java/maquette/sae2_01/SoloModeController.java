package maquette.sae2_01;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SoloModeController {

    @FXML
    private void onNormal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Bomberman");
            stage.setWidth(1000);
            stage.setHeight(1000);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            BombermanController controller = loader.getController();
            controller.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCTF(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman2.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Bomberman");
            stage.setWidth(1000);
            stage.setHeight(1000);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            BombermanController2 controller = loader.getController();
            controller.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onQuit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Paramètres");
            stage.setWidth(600);
            stage.setHeight(500);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            MenuController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
