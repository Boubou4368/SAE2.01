package maquette.sae2_01;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class SelectModeController {
    public static String fichier;


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

            // Récupérer le controller et l'initialiser
            BombermanController controller = loader.getController();
            controller.initializeGame(false);
            controller.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onNormalMulti(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Bomberman");
            stage.setWidth(1000);
            stage.setHeight(1000);
            stage.show();

            // Récupérer le controller et l'initialiser
            BombermanController controller = loader.getController();
            controller.initializeGame(true);
            controller.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCustom(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.bbmap")
        );

        Stage stage1 = (Stage)((Node)event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage1);

        if (selectedFile != null) {
            System.out.println("Fichier sélectionné : " + selectedFile.getAbsolutePath());
            // Passe-le en argument où tu veux
        }
        fichier = selectedFile.getAbsolutePath();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bomberman3.fxml"));
            Scene gameScene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Bomberman");
            stage.setWidth(1000);
            stage.setHeight(1000);
            stage.show();

            // Donne le focus au canvas pour les touches clavier
            BombermanController3 controller = loader.getController();
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
