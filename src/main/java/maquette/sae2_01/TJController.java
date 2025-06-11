package maquette.sae2_01;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TJController {
    @FXML
    private VBox root;

    @FXML
    private Button btnAssign;

    @FXML
    private Label lblInfo;

    private boolean attenteTouche = false;

    private String joueur;   // ou dynamique selon contexte
    //private String directionBouton = "U";  // ou via fx:id ou paramètre

    public void setJoueur(String joueur) throws IOException {
        this.joueur = joueur;
        lireTouche(joueur);
    }

    /*public static Map<String, String> lireTouches(String joueur) throws IOException {
        Path path = Paths.get("src/main/resources/maquette/sae2_01/Touches.yaml");
        List<String> lignes = Files.readAllLines(path);

        Map<String, String> touches = new HashMap<>();
        boolean joueurTrouve = false;

        for (String ligne : lignes) {
            ligne = ligne.trim();

            if (ligne.isEmpty()) continue;

            if (ligne.endsWith(":") && ligne.replace(":", "").equalsIgnoreCase(joueur)) {
                joueurTrouve = true;
                continue;
            }

            if (joueurTrouve) {
                // Si une nouvelle section commence (autre joueur)
                if (ligne.matches("^[A-Za-z0-9]+\\s*:$")) break;

                // Ligne de type U : R
                String[] parts = ligne.split(":", 2);
                if (parts.length == 2) {
                    String direction = parts[0].trim();
                    String touche = parts[1].trim();
                    touches.put(direction, touche);
                }
            }
        }

        if (!joueurTrouve) {
            throw new IOException("Joueur " + joueur + " non trouvé.");
        }

        return touches;
    }*/

    @FXML
    private void TJ(ActionEvent event) throws IOException {
        Button source = (Button) event.getSource();
        String direction = source.getId();

        // Activer immédiatement la logique
        source.getStyleClass().add("bouton-actif");
        lblInfo.setText("Appuie sur une touche...");
        attenteTouche = true;
        root.requestFocus();

        // On capte l'événement clavier une seule fois
        root.setOnKeyPressed(keyEvent -> {
            if (attenteTouche) {
                KeyCode code = keyEvent.getCode();

                lblInfo.setText("Touche assignée : " + code.getName());
                source.setText(code.getName()); // mettre à jour le bouton cliqué

                try {
                    modifierTouche(joueur, direction, code.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    lblInfo.setText("Erreur lors de la sauvegarde.");
                }

                attenteTouche = false;
                source.getStyleClass().remove("bouton-actif");
            }
        });

        System.out.println("Direction sélectionnée : " + direction);
    }

    /*@FXML
    public void TJS() {
        btnAssign.setOnAction(e -> {
            // Changer la classe CSS du bouton
            btnAssign.getStyleClass().add("bouton-actif");

            // Changer le texte du label
            lblInfo.setText("Appuie sur une touche...");

            // Préparer pour capter la touche
            attenteTouche = true;

            // Donner le focus au VBox pour capter la touche
            root.requestFocus();
        });
        // Gestionnaire de touche
        root.setOnKeyPressed(event -> {
            if (attenteTouche) {
                KeyCode code = event.getCode();

                lblInfo.setText("Touche assignée : " + code.getName());
                btnAssign.setText(code.getName());

                try {
                    modifierTouche(
                            joueur,
                            directionBouton,
                            code.getName()
                    );
                } catch (IOException ex) {
                    ex.printStackTrace();
                    lblInfo.setText("Erreur lors de la sauvegarde.");
                }

                attenteTouche = false;

                btnAssign.getStyleClass().remove("bouton-actif");
            }
        });

    }*/

    public static void modifierTouche(String joueur, String direction, String nouvelleTouche) throws IOException {


        Path path = Paths.get("src/main/resources/maquette/sae2_01/Touches.yaml");
        List<String> lignes = Files.readAllLines(path);

        // Index du joueur
        int indexJoueur = -1;
        for (int i = 0; i < lignes.size(); i++) {
            if (lignes.get(i).trim().equalsIgnoreCase(joueur + " :")) {
                indexJoueur = i;
                break;
            }
        }
        if (indexJoueur == -1) {
            throw new IOException("Joueur " + joueur + " non trouvé dans le fichier");
        }

        // Chercher la ligne correspondant à la direction sous ce joueur
        for (int i = indexJoueur + 1; i < lignes.size(); i++) {
            String ligne = lignes.get(i).trim();
            if (ligne.isEmpty()) break; // fin du bloc joueur

            // Exemple ligne : "U : UP"
            if (ligne.startsWith(direction)) {
                // Modifier la ligne : "U : nouvelleTouche"
                lignes.set(i, direction + " : " + nouvelleTouche);
                break;
            }
        }

        // Réécrire le fichier
        Files.write(path, lignes);
    }

    public void lireTouche(String joueur) throws IOException {
        Path path = Paths.get("src/main/resources/maquette/sae2_01/Touches.yaml");
        List<String> lignes = Files.readAllLines(path);

        // Index du joueur
        int indexJoueur = -1;
        for (int i = 0; i < lignes.size(); i++) {
            if (lignes.get(i).trim().equalsIgnoreCase(joueur + " :")) {
                indexJoueur = i;
                break;
            }
        }
        if (indexJoueur == -1) {
            throw new IOException("Joueur " + joueur + " non trouvé dans le fichier");
        }

        // Chercher la ligne correspondant à la direction sous ce joueur
        for (int i = indexJoueur + 1; i < lignes.size(); i++) {
            String ligne = lignes.get(i).trim();
            if (ligne.isEmpty() || ligne.startsWith("J")) break; // fin du bloc joueur
            System.out.println(ligne);
            if (ligne.length() >= 4) {
                char btn = ligne.charAt(0);
                Button Id = (Button) root.lookup("#" + btn);
                Id.setText(ligne.substring(4));
            }
            else {
                lignes.set(i, "");
            }




        }
    }


    @FXML
    private void onQuit(ActionEvent event) {
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
}
