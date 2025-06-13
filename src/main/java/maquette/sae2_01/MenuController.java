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
                newScene.getStylesheets().add(getClass().getResource("style/menu.css").toExternalForm());
            }
        });
    }

    @FXML
    private void onPlaySolo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("soloMode.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Choisir le mode");
            stage.setWidth(600);
            stage.setHeight(500);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            SelectModeController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPlaymulti(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MultiMode.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Choisir le mode");
            stage.setWidth(600);
            stage.setHeight(500);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            SelectModeController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEditeur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editeur.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Editeur de niveau");
            stage.setWidth(1000);
            stage.setHeight(1000);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            EditeurController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onParametre(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("parametre.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Paramètres");
            stage.setWidth(600);
            stage.setHeight(500);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            ParametreController controller = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onQuit(ActionEvent event) {
        System.exit(0);
    }
}
