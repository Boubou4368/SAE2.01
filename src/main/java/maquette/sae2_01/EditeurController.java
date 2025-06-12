package maquette.sae2_01;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

public class EditeurController {

    private String[][] levelMap = new String[13][13];


    @FXML private GridPane gridPane;

    private ToggleGroup toggleGroup = new ToggleGroup();  // crée le ToggleGroup ici

    @FXML
    private RadioButton btnIndestructible, btnDestructible, btnPlayer1, btnPlayer2, btnPlayer3, btnPlayer4,
            btnFlag1, btnFlag2, btnFlag3, btnFlag4, btnFeu, btnVitesse, btnKick, btnBombe, btnSkull;

    // Images
    private Image brickImage;
    private Image feuImage;
    private Image vitesseImage;
    private Image kickImage;
    private Image bombeImage;
    private Image iconeImage;
    private Image skullImage;
    private Image joueur1;
    private Image joueur2;
    private Image joueur3;
    private Image joueur4;
    private Image drapeau1;
    private Image drapeau2;
    private Image drapeau3;
    private Image drapeau4;

    @FXML
    public void initialize() {
        // associe tous les RadioButtons au même ToggleGroup
        btnIndestructible.setToggleGroup(toggleGroup);
        btnDestructible.setToggleGroup(toggleGroup);
        btnPlayer1.setToggleGroup(toggleGroup);
        btnPlayer2.setToggleGroup(toggleGroup);
        btnPlayer3.setToggleGroup(toggleGroup);
        btnPlayer4.setToggleGroup(toggleGroup);
        btnFlag1.setToggleGroup(toggleGroup);
        btnFlag2.setToggleGroup(toggleGroup);
        btnFlag3.setToggleGroup(toggleGroup);
        btnFlag4.setToggleGroup(toggleGroup);
        btnFeu.setToggleGroup(toggleGroup);
        btnVitesse.setToggleGroup(toggleGroup);
        btnBombe.setToggleGroup(toggleGroup);
        btnSkull.setToggleGroup(toggleGroup);
        btnKick.setToggleGroup(toggleGroup);

        loadGameImages();

        // reste de ton init avec la grille
        for (int row = 0; row < 13; row++) {
            for (int col = 0; col < 13; col++) {
                Rectangle cell = new Rectangle(40, 40, Color.web("#2E7D32"));
                int finalRow = row;
                int finalCol = col;
                cell.setOnMouseClicked(e -> handleClick(finalRow, finalCol, cell, e));
                gridPane.add(cell, col, row);
                levelMap[row][col] = "EMPTY";
            }
        }

    }

    private void handleClick(int row, int col, Rectangle cell, MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            // Clic droit : on enlève l'élément sur la case
            levelMap[row][col] = "EMPTY";
            cell.setFill(Color.web("#2E7D32")); // couleur par défaut
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Clic gauche : on place l'élément sélectionné
            RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
            if (selected != null) {
                System.out.println(selected.getText());
                String text = selected.getText().toUpperCase().replace(" ", "_");
                System.out.println(text);
                switch (text) {
                    case "MUR_INDESTRUCTIBLE":
                        cell.setFill(Color.web("#1B5E20"));
                        break;
                    case "MUR_DESTRUCTIBLE":
                        System.out.println("rer");
                        cell.setFill(new ImagePattern(brickImage));
                        break;
                    case "JOUEUR_1":
                        cell.setFill(new ImagePattern(joueur1));
                        break;
                    case "JOUEUR_2":
                        cell.setFill(new ImagePattern(joueur2));
                        break;
                    case "JOUEUR_3":
                        cell.setFill(new ImagePattern(joueur3));
                        break;
                    case "JOUEUR_4":
                        cell.setFill(new ImagePattern(joueur4));
                        break;
                    case "DRAPEAU_1":
                        cell.setFill(new ImagePattern(drapeau1));
                        break;
                    case "DRAPEAU_2":
                        cell.setFill(new ImagePattern(drapeau2));
                        break;
                    case "DRAPEAU_3":
                        cell.setFill(new ImagePattern(drapeau3));
                        break;
                    case "DRAPEAU_4":
                        cell.setFill(new ImagePattern(drapeau4));
                        break;
                    case "FEU":
                        cell.setFill(new ImagePattern(feuImage));
                        break;
                    case "VITESSE":
                        cell.setFill(new ImagePattern(vitesseImage));
                        break;
                    case "BOMBE":
                        cell.setFill(new ImagePattern(bombeImage));
                        break;
                    case "KICK":
                        cell.setFill(new ImagePattern(kickImage));
                        break;
                    case "SKULL":
                        cell.setFill(new ImagePattern(skullImage));
                        break;

                }
                levelMap[row][col] = text;
            }
        }
    }


    @FXML
    private void handleSave() {
        try (FileWriter writer = new FileWriter("niveau_sauvegarde.txt")) {
            for (String[] row : levelMap) {
                for (String cell : row) {
                    writer.write(cell + " ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGameImages() {
        brickImage = BombermanController.loadImage("/maquette/sae2_01/brique.png");
        kickImage =  BombermanController.loadImage("/maquette/sae2_01/kick.png");
        feuImage =  BombermanController.loadImage("/maquette/sae2_01/feu.png");
        vitesseImage =  BombermanController.loadImage("/maquette/sae2_01/vitesse.png");
        bombeImage =  BombermanController.loadImage("/maquette/sae2_01/bombe.png");
        skullImage =  BombermanController.loadImage("/maquette/sae2_01/skull.png");
        joueur1 =  BombermanController.loadImage("/maquette/sae2_01/icone.png");
        joueur2 =  BombermanController.loadImage("/maquette/sae2_01/icone2.png");
        joueur3 =  BombermanController.loadImage("/maquette/sae2_01/icone3.png");
        joueur4 =  BombermanController.loadImage("/maquette/sae2_01/icone4.png");
        drapeau1 =  BombermanController.loadImage("/maquette/sae2_01/drapeau1.png");
        drapeau2 =  BombermanController.loadImage("/maquette/sae2_01/drapeau2.png");
        drapeau3 =  BombermanController.loadImage("/maquette/sae2_01/drapeau3.png");
        drapeau4 =  BombermanController.loadImage("/maquette/sae2_01/drapeau4.png");
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