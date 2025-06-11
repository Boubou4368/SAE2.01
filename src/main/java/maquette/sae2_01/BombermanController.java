package maquette.sae2_01;
// ==========================================
// BombermanController.java
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class BombermanController implements Initializable {
    @FXML
    private Canvas gameCanvas;
    @FXML private Label bombsLabel;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label profileLabel;

    private String player1Profile = "";
    private String player2Profile = "";
    private String player3Profile = "";
    private String player4Profile = "";
    private List<PlayerProfile> profileList = new ArrayList<>();

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;

    private GraphicsContext gc;
    private GameState gameState;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 150_000_000; // 150ms en nanosecondes

    private Stage primaryStage;
    private boolean gameStarted = false;

    // Images de fond
    private Image profileBackgroundImage;
    private Image statsBackgroundImage;
    private Image deleteBackgroundImage;
    private Image victoryBackgroundImage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        loadBackgroundImages();
        loadProfilesFromFile();
        
        // Afficher l'écran de saisie des profils au lieu de démarrer directement
        Platform.runLater(() -> showProfileSelectionScreen());
    }

    private void loadBackgroundImages() {
        try {
            // Chargement des images de fond depuis le dossier resources/images
            profileBackgroundImage = new Image(getClass().getResourceAsStream("/images/profile_background.jpg"));
            statsBackgroundImage = new Image(getClass().getResourceAsStream("/images/stats_background.jpg"));
            deleteBackgroundImage = new Image(getClass().getResourceAsStream("/images/delete_background.jpg"));
            victoryBackgroundImage = new Image(getClass().getResourceAsStream("/images/victory_background.jpg"));
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des images de fond : " + e.getMessage());
            // En cas d'erreur, on utilise des images par défaut ou on laisse null
        }
    }

    private BackgroundImage createBackgroundImage(Image image) {
        if (image == null) return null;

        return new BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO,
                false, false, false, true
            )
        );
    }

    private void showProfileSelectionScreen() {
        VBox mainBox = new VBox(20);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPrefSize(800, 600);

        // Application de l'image de fond
        BackgroundImage bgImage = createBackgroundImage(profileBackgroundImage);
        if (bgImage != null) {
            mainBox.setBackground(new Background(bgImage));
        } else {
            mainBox.setStyle("-fx-background-color: #2E7D32;");
        }

        Label titleLabel = new Label("BOMBERMAN - SÉLECTION DES PROFILS");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0, 2, 2);");

        // Saisie Joueur 1
        VBox player1Box = new VBox(10);
        player1Box.setAlignment(Pos.CENTER);
        Label player1Label = new Label("Joueur 1 :");
        player1Label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");
        TextField player1Field = new TextField();
        player1Field.setPromptText("Nom du profil joueur 1");
        player1Field.setMaxWidth(200);
        player1Box.getChildren().addAll(player1Label, player1Field);

        // Saisie Joueur 2
        VBox player2Box = new VBox(10);
        player2Box.setAlignment(Pos.CENTER);
        Label player2Label = new Label("Joueur 2 :");
        player2Label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");
        TextField player2Field = new TextField();
        player2Field.setPromptText("Nom du profil joueur 2");
        player2Field.setMaxWidth(200);
        player2Box.getChildren().addAll(player2Label, player2Field);

        // Saisie Joueur 3
        VBox player3Box = new VBox(10);
        player3Box.setAlignment(Pos.CENTER);
        Label player3Label = new Label("Joueur 3 :");
        player3Label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");
        TextField player3Field = new TextField();
        player3Field.setPromptText("Nom du profil joueur 3");
        player3Field.setMaxWidth(200);
        player3Box.getChildren().addAll(player3Label, player3Field);

        // Saisie Joueur 4
        VBox player4Box = new VBox(10);
        player4Box.setAlignment(Pos.CENTER);
        Label player4Label = new Label("Joueur 4 :");
        player4Label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");
        TextField player4Field = new TextField();
        player4Field.setPromptText("Nom du profil joueur 4");
        player4Field.setMaxWidth(200);
        player4Box.getChildren().addAll(player4Label, player4Field);

        // Container pour les boutons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button startButton = new Button("COMMENCER LA PARTIE");
        startButton.setStyle("-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setOnAction(e -> {
            String p1Name = player1Field.getText().trim();
            String p2Name = player2Field.getText().trim();
            String p3Name = player3Field.getText().trim();
            String p4Name = player4Field.getText().trim();

            if (p1Name.isEmpty() || p2Name.isEmpty() || p3Name.isEmpty() || p4Name.isEmpty()) {
                return; // Ne pas commencer si les noms sont vides
            }

            player1Profile = p1Name;
            player2Profile = p2Name;
            player3Profile = p3Name;
            player4Profile = p4Name;

            // Créer ou récupérer les profils
            createOrGetProfile(p1Name);
            createOrGetProfile(p2Name);
            createOrGetProfile(p3Name);
            createOrGetProfile(p4Name);

            startGame();
        });

        Button statsButton = new Button("VOIR LES STATISTIQUES");
        statsButton.setStyle("-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        statsButton.setOnAction(e -> showPlayerStatsFromProfileSelection());

        Button deleteProfileButton = new Button("SUPPRIMER UN PROFIL");
        deleteProfileButton.setStyle("-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteProfileButton.setOnAction(e -> showDeleteProfileScreen());

        buttonBox.getChildren().addAll(startButton, statsButton, deleteProfileButton);

        mainBox.getChildren().addAll(titleLabel, player1Box, player2Box, player3Box, player4Box, buttonBox);

        Scene profileScene = new Scene(mainBox);
        primaryStage.setScene(profileScene);
    }

    private boolean deleteProfile(String name) {
        boolean removed = profileList.removeIf(profile -> profile.name.equals(name));
        if (removed) {
            saveProfilesToFile();
        }
        return removed;
    }

    private void showDeleteProfileScreen() {
        VBox deleteBox = new VBox(20);
        deleteBox.setAlignment(Pos.CENTER);
        deleteBox.setPrefSize(800, 600);

        // Application de l'image de fond
        BackgroundImage bgImage = createBackgroundImage(deleteBackgroundImage);
        if (bgImage != null) {
            deleteBox.setBackground(new Background(bgImage));
        } else {
            deleteBox.setStyle("-fx-background-color: #2E7D32;");
        }

        Label titleLabel = new Label("SUPPRIMER UN PROFIL");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0, 2, 2);");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom du profil à supprimer");
        nameField.setMaxWidth(250);

        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");

        Button confirmButton = new Button("SUPPRIMER");
        confirmButton.setStyle("-fx-font-size: 16px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmButton.setOnAction(e -> {
            String nameToDelete = nameField.getText().trim();
            boolean removed = deleteProfile(nameToDelete);
            if (removed) {
                resultLabel.setText("Profil supprimé avec succès.");
            } else {
                resultLabel.setText("Profil introuvable.");
            }
        });

        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-font-size: 16px; -fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> showProfileSelectionScreen());

        deleteBox.getChildren().addAll(titleLabel, nameField, confirmButton, resultLabel, backButton);

        Scene deleteScene = new Scene(deleteBox);
        primaryStage.setScene(deleteScene);
    }

    private void showPlayerStatsFromProfileSelection() {
        // Trier par ratio de victoires
        profileList.sort((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()));

        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPrefSize(800, 600);

        // Application de l'image de fond
        BackgroundImage bgImage = createBackgroundImage(statsBackgroundImage);
        if (bgImage != null) {
            statsBox.setBackground(new Background(bgImage));
        } else {
            statsBox.setStyle("-fx-background-color: #2E7D32;");
        }

        Label title = new Label("CLASSEMENT DES JOUEURS");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, blue, 2, 0, 2, 2);");

        VBox playersBox = new VBox(10);
        playersBox.setAlignment(Pos.CENTER);

        if (profileList.isEmpty()) {
            Label noDataLabel = new Label("Aucune statistique disponible");
            noDataLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, black, 1, 0, 1, 1);");
            playersBox.getChildren().add(noDataLabel);
        } else {
            int rank = 1;
            for (PlayerProfile profile : profileList) {
                String statsText = String.format("%d. %s - %d V / %d D (%.1f%%)",
                    rank++, profile.name, profile.victories, profile.defeats, profile.getWinRate());
                Label statsLabel = new Label(statsText);
                statsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, blue, 1, 0, 1, 1);");
                playersBox.getChildren().add(statsLabel);
            }
        }

        Button backButton = new Button("Retour à la sélection");
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 8px 16px; -fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> showProfileSelectionScreen());

        statsBox.getChildren().addAll(title, playersBox, backButton);

        Scene statsScene = new Scene(statsBox);
        primaryStage.setScene(statsScene);
    }

    private void createOrGetProfile(String name) {
        boolean found = false;
        for (PlayerProfile profile : profileList) {
            if (profile.name.equals(name)) {
                found = true;
                break;
            }
        }
        if (!found) {
            profileList.add(new PlayerProfile(name));
        }
    }

    private void startGame() {
        gameState = new GameState();

        // Configurer le canvas pour recevoir les événements clavier
        if (gameCanvas != null) {
            gameCanvas.setFocusTraversable(true);

            // Revenir à la scène de jeu
            Scene gameScene = gameCanvas.getScene();
            if (gameScene != null) {
                primaryStage.setScene(gameScene);
            }
        }

        startGameLoop();
        updateUI();
        gameStarted = true;

        Platform.runLater(() -> {
            if (gameCanvas != null) {
                gameCanvas.requestFocus();
            }
        });
    }

    public void requestFocus() {
        if (gameCanvas != null) {
            gameCanvas.requestFocus();
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (gameStarted) {
            pressedKeys.add(event.getCode());
        }
    }

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        if (gameStarted) {
            pressedKeys.remove(event.getCode());
        }
    }

    private static class PlayerProfile {
        String name;
        int victories;
        int defeats;
        int bestScore;

        PlayerProfile(String name) {
            this.name = name;
            this.victories = 0;
            this.defeats = 0;
            this.bestScore = 0;
        }

        void addVictory(int score) {
            victories++;
            if (score > bestScore) {
                bestScore = score;
            }
        }

        void addDefeat() {
            defeats++;
        }

        int getTotalGames() {
            return victories + defeats;
        }

        double getWinRate() {
            if (getTotalGames() == 0) return 0.0;
            return (double) victories / getTotalGames() * 100;
        }
    }

    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_666) { // ~60 FPS
                    update(now);
                    render();
                    updateUI();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void update(long currentTime) {
        handleInput(currentTime);
        updateBombs();
        updateExplosions();
        checkPlayersHit();
        checkWinCondition();
    }

    private void handleInput(long currentTime) {
        if (pressedKeys.contains(KeyCode.ESCAPE)) {
            Platform.exit();
            pressedKeys.remove(KeyCode.ESCAPE);
            return;
        }
        if (pressedKeys.contains(KeyCode.X)) {
            restartGame();
            pressedKeys.remove(KeyCode.X);
            return;
        }

        if (currentTime - lastMoveTime < MOVE_DELAY) {
            // Gestion des bombes uniquement si le délai n'est pas écoulé pour le mouvement
            if (pressedKeys.contains(KeyCode.SPACE) && gameState.player1.bombsRemaining > 0 && gameState.player1.lives > 0) {
                placeBomb(gameState.player1);
                pressedKeys.remove(KeyCode.SPACE);
            }
            if (pressedKeys.contains(KeyCode.ENTER) && gameState.player2.bombsRemaining > 0 && gameState.player2.lives > 0) {
                placeBomb(gameState.player2);
                pressedKeys.remove(KeyCode.ENTER);
            }
            if (pressedKeys.contains(KeyCode.SHIFT) && gameState.player3.bombsRemaining > 0 && gameState.player3.lives > 0) {
                placeBomb(gameState.player3);
                pressedKeys.remove(KeyCode.SHIFT);
            }
            if (pressedKeys.contains(KeyCode.TAB) && gameState.player4.bombsRemaining > 0 && gameState.player4.lives > 0) {
                placeBomb(gameState.player4);
                pressedKeys.remove(KeyCode.TAB);
            }
            return;
        }

        // Mouvement Joueur 1 (ZQSD)
        if (gameState.player1.lives > 0) {
            Position newPos1 = new Position(gameState.player1.pos.x, gameState.player1.pos.y);
            boolean moved1 = false;

            if (pressedKeys.contains(KeyCode.Z)) {
                newPos1.y--; moved1 = true;
            } else if (pressedKeys.contains(KeyCode.S)) {
                newPos1.y++; moved1 = true;
            } else if (pressedKeys.contains(KeyCode.Q)) {
                newPos1.x--; moved1 = true;
            } else if (pressedKeys.contains(KeyCode.D)) {
                newPos1.x++; moved1 = true;
            }

            if (moved1 && canMoveTo(newPos1)) {
                gameState.player1.pos = newPos1;
                lastMoveTime = currentTime;
            }
        }

        // Mouvement Joueur 2 (Flèches)
        if (gameState.player2.lives > 0) {
            Position newPos2 = new Position(gameState.player2.pos.x, gameState.player2.pos.y);
            boolean moved2 = false;

            if (pressedKeys.contains(KeyCode.UP)) {
                newPos2.y--; moved2 = true;
            } else if (pressedKeys.contains(KeyCode.DOWN)) {
                newPos2.y++; moved2 = true;
            } else if (pressedKeys.contains(KeyCode.LEFT)) {
                newPos2.x--; moved2 = true;
            } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                newPos2.x++; moved2 = true;
            }

            if (moved2 && canMoveTo(newPos2)) {
                gameState.player2.pos = newPos2;
                lastMoveTime = currentTime;
            }
        }

        // Mouvement Joueur 3 (IJKL)
        if (gameState.player3.lives > 0) {
            Position newPos3 = new Position(gameState.player3.pos.x, gameState.player3.pos.y);
            boolean moved3 = false;

            if (pressedKeys.contains(KeyCode.I)) {
                newPos3.y--; moved3 = true;
            } else if (pressedKeys.contains(KeyCode.K)) {
                newPos3.y++; moved3 = true;
            } else if (pressedKeys.contains(KeyCode.J)) {
                newPos3.x--; moved3 = true;
            } else if (pressedKeys.contains(KeyCode.L)) {
                newPos3.x++; moved3 = true;
            }

            if (moved3 && canMoveTo(newPos3)) {
                gameState.player3.pos = newPos3;
                lastMoveTime = currentTime;
            }
        }

        // Mouvement Joueur 4 (TFGH)
        if (gameState.player4.lives > 0) {
            Position newPos4 = new Position(gameState.player4.pos.x, gameState.player4.pos.y);
            boolean moved4 = false;

            if (pressedKeys.contains(KeyCode.T)) {
                newPos4.y--; moved4 = true;
            } else if (pressedKeys.contains(KeyCode.G)) {
                newPos4.y++; moved4 = true;
            } else if (pressedKeys.contains(KeyCode.F)) {
                newPos4.x--; moved4 = true;
            } else if (pressedKeys.contains(KeyCode.H)) {
                newPos4.x++; moved4 = true;
            }

            if (moved4 && canMoveTo(newPos4)) {
                gameState.player4.pos = newPos4;
                lastMoveTime = currentTime;
            }
        }

        // Placement des bombes
        if (pressedKeys.contains(KeyCode.SPACE) && gameState.player1.bombsRemaining > 0 && gameState.player1.lives > 0) {
            placeBomb(gameState.player1);
            pressedKeys.remove(KeyCode.SPACE);
        }
        if (pressedKeys.contains(KeyCode.ENTER) && gameState.player2.bombsRemaining > 0 && gameState.player2.lives > 0) {
            placeBomb(gameState.player2);
            pressedKeys.remove(KeyCode.ENTER);
        }
        if (pressedKeys.contains(KeyCode.SHIFT) && gameState.player3.bombsRemaining > 0 && gameState.player3.lives > 0) {
            placeBomb(gameState.player3);
            pressedKeys.remove(KeyCode.SHIFT);
        }
        if (pressedKeys.contains(KeyCode.TAB) && gameState.player4.bombsRemaining > 0 && gameState.player4.lives > 0) {
            placeBomb(gameState.player4);
            pressedKeys.remove(KeyCode.TAB);
        }
    }

    private boolean canMoveTo(Position pos) {
        if (pos.x < 0 || pos.x >= GRID_SIZE || pos.y < 0 || pos.y >= GRID_SIZE) {
            return false;
        }

        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(pos)) {
                return false;
            }
        }

        return true;
    }

    private void placeBomb(Player player) {
        Position bombPos = new Position(player.pos.x, player.pos.y);

        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                return;
            }
        }

        gameState.bombs.add(new Bomb(bombPos.x, bombPos.y, player));
        player.bombsRemaining--;
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = gameState.bombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.timer--;

            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                bombIterator.remove();
                bomb.owner.bombsRemaining++;
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int range = bomb.owner.bombRange;

        gameState.explosions.add(new Explosion(bomb.pos.x, bomb.pos.y));

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int i = 1; i <= range; i++) {
                int newX = bomb.pos.x + dir[0] * i;
                int newY = bomb.pos.y + dir[1] * i;
                Position explPos = new Position(newX, newY);

                if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE) {
                    break;
                }

                if (gameState.walls.contains(explPos)) {
                    break;
                }

                gameState.explosions.add(new Explosion(newX, newY));

                if (gameState.destructibleWalls.contains(explPos)) {
                    gameState.destructibleWalls.remove(explPos);
                    bomb.owner.score += 10;
                    break;
                }
            }
        }
    }

    private void updateExplosions() {
        gameState.explosions.removeIf(explosion -> {
            explosion.timer--;
            return explosion.timer <= 0;
        });
    }

    private void checkPlayersHit() {
        for (Explosion explosion : gameState.explosions) {
            // Vérifier tous les joueurs
            if (explosion.pos.equals(gameState.player1.pos) && gameState.player1.lives > 0) {
                gameState.player1.lives--;
            }
            if (explosion.pos.equals(gameState.player2.pos) && gameState.player2.lives > 0) {
                gameState.player2.lives--;
            }
            if (explosion.pos.equals(gameState.player3.pos) && gameState.player3.lives > 0) {
                gameState.player3.lives--;
            }
            if (explosion.pos.equals(gameState.player4.pos) && gameState.player4.lives > 0) {
                gameState.player4.lives--;
            }
        }
    }

    private void updateGameStats(String winner, int winnerScore) {
        List<String> allPlayers = Arrays.asList(player1Profile, player2Profile, player3Profile, player4Profile);

        for (PlayerProfile profile : profileList) {
            if (profile.name.equals(winner)) {
                profile.addVictory(winnerScore);
            } else if (allPlayers.contains(profile.name)) {
                profile.addDefeat();
            }
        }
        saveProfilesToFile();
    }

    private void updateGameStatsAllDefeated() {
        List<String> allPlayers = Arrays.asList(player1Profile, player2Profile, player3Profile, player4Profile);

        for (PlayerProfile profile : profileList) {
            if (allPlayers.contains(profile.name)) {
                profile.addDefeat();
            }
        }
        saveProfilesToFile();
    }

    private void checkWinCondition() {
        // Compter les joueurs vivants et identifier le gagnant
        List<Player> alivePlayers = new ArrayList<>();
        List<String> alivePlayerNames = new ArrayList<>();

        if (gameState.player1.lives > 0) {
            alivePlayers.add(gameState.player1);
            alivePlayerNames.add(player1Profile);
        }
        if (gameState.player2.lives > 0) {
            alivePlayers.add(gameState.player2);
            alivePlayerNames.add(player2Profile);
        }
        if (gameState.player3.lives > 0) {
            alivePlayers.add(gameState.player3);
            alivePlayerNames.add(player3Profile);
        }
        if (gameState.player4.lives > 0) {
            alivePlayers.add(gameState.player4);
            alivePlayerNames.add(player4Profile);
        }

        // Condition de fin : plus qu'un joueur vivant ou aucun
        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                // Un seul gagnant
                String winner = alivePlayerNames.get(0);
                int winnerScore = alivePlayers.get(0).score;
                updateGameStats(winner, winnerScore);
                showVictoryScreen("Victoire de " + winner + " !");
            } else {
                // Aucun survivant (tous morts en même temps)
                updateGameStatsAllDefeated();
                showVictoryScreen("Tous les joueurs ont été éliminés !");
            }
        }
        saveProfilesToFile();
    }


    private void showVictoryScreen(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameStarted = false;

        Platform.runLater(() -> {
            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPrefSize(800, 600);

            BackgroundImage bgImage = createBackgroundImage(victoryBackgroundImage);
            if (bgImage != null) {
                vbox.setBackground(new Background(bgImage));
            } else {
                vbox.setStyle("-fx-background-color: #2E7D32;");
            }

            Label victoryLabel = new Label(message);
            victoryLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 0, 2, 2);");

            Button showStatsButton = new Button("Voir les statistiques");
            showStatsButton.setStyle("-fx-font-size: 16px; -fx-background-color: #2196F3; -fx-text-fill: white;");
            showStatsButton.setOnAction(e -> showPlayerStats());

            Button replayButton = new Button("Rejouer");
            replayButton.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
            replayButton.setOnAction(e -> restartGame());

            Button quitButton = new Button("Quitter");
            quitButton.setStyle("-fx-font-size: 16px; -fx-background-color: #F44336; -fx-text-fill: white;");
            quitButton.setOnAction(e -> Platform.exit());

            vbox.getChildren().addAll(victoryLabel, showStatsButton, replayButton, quitButton);

            Scene victoryScene = new Scene(vbox);
            primaryStage.setScene(victoryScene);
        });
    }

    private void showPlayerStats() {
        // Trier par ratio de victoires
        profileList.sort((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()));

        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPrefSize(800, 600);
        statsBox.setStyle("-fx-background-color: #2E7D32;");

        Label title = new Label("CLASSEMENT DES JOUEURS");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        VBox playersBox = new VBox(10);
        playersBox.setAlignment(Pos.CENTER);

        int rank = 1;
        for (PlayerProfile profile : profileList) {
            String statsText = String.format("%d. %s - %d V / %d D (%.1f%%)",
                rank++, profile.name, profile.victories, profile.defeats, profile.getWinRate());
            Label statsLabel = new Label(statsText);
            statsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            playersBox.getChildren().add(statsLabel);
        }

        Button backButton = new Button("Retour");
        backButton.setOnAction(e -> showVictoryScreen("Fin de partie"));

        statsBox.getChildren().addAll(title, playersBox, backButton);

        Scene statsScene = new Scene(statsBox);
        primaryStage.setScene(statsScene);
    }

    private void loadProfilesFromFile() {
        try (Scanner scanner = new Scanner(new File("donnees.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    String name = parts[0].trim();
                    int victories = Integer.parseInt(parts[1].trim());
                    int defeats = Integer.parseInt(parts[2].trim());
                    int bestScore = Integer.parseInt(parts[3].trim());

                    PlayerProfile profile = new PlayerProfile(name);
                    profile.victories = victories;
                    profile.defeats = defeats;
                    profile.bestScore = bestScore;
                    profileList.add(profile);
                }
            }
        } catch (Exception e) {
            System.out.println("Aucun fichier de profils trouvé ou erreur de lecture.");
        }
    }

    private void saveProfilesToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("donnees.txt"))) {
            for (PlayerProfile profile : profileList) {
                writer.println(profile.name + ":" + profile.victories + ":" +
                             profile.defeats + ":" + profile.bestScore);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'écriture des profils.");
            e.printStackTrace();
        }
    }

    private void restartGame() {
        Platform.runLater(() -> {
            try {
                primaryStage.close();
                BombermanApplication newApp = new BombermanApplication();
                Stage newStage = new Stage();
                newApp.start(newStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateUI() {
        if (gameState != null) {
            if (bombsLabel != null) {
                bombsLabel.setText("J1 Bombes: " + gameState.player1.bombsRemaining + " | J2 Bombes: " + gameState.player2.bombsRemaining);
            }
            if (scoreLabel != null) {
                scoreLabel.setText("J1 Score: " + gameState.player1.score + " | J2 Score: " + gameState.player2.score);
            }
            if (levelLabel != null) {
                levelLabel.setText("Vies J1: " + gameState.player1.lives + " | Vies J2: " + gameState.player2.lives);
            }
            if (profileLabel != null) {
                profileLabel.setText("J1: " + player1Profile + " | J2: " + player2Profile);
            }
        }
    }

    private void render() {
        if (gameState == null) return;

        // Fond
        gc.setFill(Color.web("#2E7D32"));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Grille
        gc.setStroke(Color.web("#1B5E20"));
        gc.setLineWidth(1);
        for (int i = 0; i <= GRID_SIZE; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, gameCanvas.getHeight());
            gc.strokeLine(0, i * CELL_SIZE, gameCanvas.getWidth(), i * CELL_SIZE);
        }

        // Murs indestructibles
        gc.setFill(Color.web("#424242"));
        for (Position wall : gameState.walls) {
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gc.setFill(Color.web("#616161"));
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, 3);
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, 3, CELL_SIZE);
            gc.setFill(Color.web("#424242"));
        }

        // Murs destructibles
        gc.setFill(Color.web("#8D6E63"));
        for (Position wall : gameState.destructibleWalls) {
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gc.setFill(Color.web("#A1887F"));
            for (int i = 0; i < 3; i++) {
                gc.fillRect(wall.x * CELL_SIZE + 5, wall.y * CELL_SIZE + i * 12 + 5, CELL_SIZE - 10, 8);
            }
            gc.setFill(Color.web("#8D6E63"));
        }

        // Explosions
        for (Explosion explosion : gameState.explosions) {
            double intensity = (double) explosion.timer / 30;
            Color explosionColor = Color.web("#FF5722").interpolate(Color.web("#FFEB3B"), 1 - intensity);
            gc.setFill(explosionColor);
            double size = CELL_SIZE * (0.5 + 0.5 * intensity);
            double offset = (CELL_SIZE - size) / 2;
            gc.fillOval(explosion.pos.x * CELL_SIZE + offset, explosion.pos.y * CELL_SIZE + offset, size, size);
        }

        // Bombes
        for (Bomb bomb : gameState.bombs) {
            boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;
            gc.setFill(blink ? Color.web("#F44336") : Color.web("#212121"));
            gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            gc.setFill(Color.web("#FF9800"));
            gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
        }

        // Joueur 1 (bleu)
        if (gameState.player1.lives > 0) {
            gc.setFill(Color.web("#2196F3"));
            gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 5, gameState.player1.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            gc.setFill(Color.web("#64B5F6"));
            gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 8, gameState.player1.pos.y * CELL_SIZE + 8, 8, 8);
        }

        // Joueur 2 (rouge)
        if (gameState.player2.lives > 0) {
            gc.setFill(Color.web("#F44336"));
            gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 5, gameState.player2.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            gc.setFill(Color.web("#EF5350"));
            gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 8, gameState.player2.pos.y * CELL_SIZE + 8, 8, 8);
        }

        // Joueur 3 (vert)
        if (gameState.player3.lives > 0) {
            gc.setFill(Color.web("#4CAF50"));
            gc.fillOval(gameState.player3.pos.x * CELL_SIZE + 5, gameState.player3.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            gc.setFill(Color.web("#81C784"));
            gc.fillOval(gameState.player3.pos.x * CELL_SIZE + 8, gameState.player3.pos.y * CELL_SIZE + 8, 8, 8);
        }

    // Joueur 4 (jaune)
        if (gameState.player4.lives > 0) {
            gc.setFill(Color.web("#FFEB3B"));
            gc.fillOval(gameState.player4.pos.x * CELL_SIZE + 5, gameState.player4.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            gc.setFill(Color.web("#FFF176"));
            gc.fillOval(gameState.player4.pos.x * CELL_SIZE + 8, gameState.player4.pos.y * CELL_SIZE + 8, 8, 8);
        }

    }

    // Classes de données
    static class Position {
        int x, y;
        Position(int x, int y) { this.x = x; this.y = y; }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Position position = (Position) obj;
            return x == position.x && y == position.y;
        }
        @Override
        public int hashCode() { return Objects.hash(x, y); }
    }

    static class Player {
        Position pos;
        int bombsRemaining = 3;
        int bombRange = 2;
        int lives = 3;
        int score = 0;
        Player(int x, int y) { this.pos = new Position(x, y); }
    }

    static class Bomb {
        Position pos;
        int timer = 120;
        Player owner;
        Bomb(int x, int y, Player owner) {
            this.pos = new Position(x, y);
            this.owner = owner;
        }
    }

    static class Explosion {
        Position pos;
        int timer = 30;
        Explosion(int x, int y) { this.pos = new Position(x, y); }
    }

    static class GameState {
        Player player1;
        Player player2;
        Player player3;
        Player player4;

        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();
        int level = 1;

        GameState() {
            player1 = new Player(1, 1);  // Joueur 1 en haut à gauche
            player2 = new Player(GRID_SIZE - 2, GRID_SIZE - 2);  // Joueur 2 en bas à droite
            player3 = new Player(1, GRID_SIZE - 2);
            player4 = new Player(GRID_SIZE - 2, 1);

            initializeMap();
        }

        private void initializeMap() {
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (i == 0 || i == GRID_SIZE - 1 || j == 0 || j == GRID_SIZE - 1) {
                        walls.add(new Position(i, j));
                    } else if (i % 2 == 0 && j % 2 == 0) {
                        walls.add(new Position(i, j));
                    }
                }
            }

            Random random = new Random();
            for (int i = 1; i < GRID_SIZE - 1; i++) {
                for (int j = 1; j < GRID_SIZE - 1; j++) {
                    if (!walls.contains(new Position(i, j)) &&
                            !((i <= 2 && j <= 2) || (i >= GRID_SIZE - 3 && j >= GRID_SIZE - 3)) &&
                            random.nextDouble() < 0.3) {
                        destructibleWalls.add(new Position(i, j));
                    }
                }
            }
        }
    }
}