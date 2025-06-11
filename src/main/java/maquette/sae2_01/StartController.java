package maquette.sae2_01;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StartController {


    @FXML
    private void onStart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Menu");
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
