package maquette.sae2_01;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TJController {
    @FXML
    private VBox root;

    @FXML
    private Button btnAssign;

    @FXML
    private Label lblInfo;

    private boolean attenteTouche = false;

    private String joueur = "J1";   // ou dynamique selon contexte
    private String directionBouton = "U";  // ou via fx:id ou paramètre

    @FXML
    public void initialize() {
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

    }

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
            if (ligne.startsWith(direction + " :")) {
                // Modifier la ligne : "U : nouvelleTouche"
                lignes.set(i, direction + " : " + nouvelleTouche);
                break;
            }
        }

        // Réécrire le fichier
        Files.write(path, lignes);
    }
}
