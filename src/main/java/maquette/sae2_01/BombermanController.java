package maquette.sae2_01;
// ==========================================
// BombermanController.java
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.application.Platform;

import javafx.geometry.Pos;
import javafx.event.EventHandler;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;


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


    private String currentProfile = "";
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
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();
        originalGameScene = gameCanvas.getScene();

        loadScoresFromFile();

        // Afficher l'écran de sélection de profil au démarrage
        Platform.runLater(() -> showProfileSelection()); // NOUVEAU


        // Configurer le canvas pour recevoir les événements clavier
        gameCanvas.setFocusTraversable(true);

        startGameLoop();
        updateUI();
    }

    public void requestFocus() {
        gameCanvas.requestFocus();
    }




    @FXML
    private void handleKeyPressed(KeyEvent event) {pressedKeys.add(event.getCode());}
    @FXML
    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    private void startGameLoop() {
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


    private void startGame() {
        // Configurer le canvas pour recevoir les événements clavier
        gameCanvas.setFocusTraversable(true);
        startGameLoop();
        updateUI();
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




    private void update(long currentTime) {
        handleInput(currentTime);
        updateBombs();
        updateExplosions();
        checkPlayerHit();
        checkVictoryCondition();
    }

    private void handleInput(long currentTime) {
        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (pressedKeys.contains(KeyCode.ESCAPE)) {
            Platform.exit(); // Quitte proprement l'application
            pressedKeys.remove(KeyCode.ESCAPE); // Pour ne pas quitter en boucle
            return;
        }
        if (pressedKeys.contains(KeyCode.X)) {
            restartGame(); // méthode déjà créée précédemment
            pressedKeys.remove(KeyCode.X); // pour ne pas le relancer en boucle
            return;
        }
        if (currentTime - lastMoveTime < MOVE_DELAY) {
            // On peut quand même placer des bombes même si on ne peut pas bouger
            if (pressedKeys.contains(KeyCode.SPACE) && gameState.player.bombsRemaining > 0) {
                placeBomb();
                pressedKeys.remove(KeyCode.SPACE);
            }
            return;
        }

        Position newPos = new Position(gameState.player.pos.x, gameState.player.pos.y);
        boolean moved = false;

        if (pressedKeys.contains(KeyCode.UP) || pressedKeys.contains(KeyCode.Z)) {
            newPos.y--;
            moved = true;
        } else if (pressedKeys.contains(KeyCode.DOWN) || pressedKeys.contains(KeyCode.S)) {
            newPos.y++;
            moved = true;
        } else if (pressedKeys.contains(KeyCode.LEFT) || pressedKeys.contains(KeyCode.Q)) {
            newPos.x--;
            moved = true;
        } else if (pressedKeys.contains(KeyCode.RIGHT) || pressedKeys.contains(KeyCode.D)) {
            newPos.x++;
            moved = true;
        }

        if (moved && canMoveTo(newPos)) {
            gameState.player.pos = newPos;
            lastMoveTime = currentTime; // Mettre à jour le temps du dernier mouvement
        }

        if (pressedKeys.contains(KeyCode.SPACE) && gameState.player.bombsRemaining > 0) {
            placeBomb();
            pressedKeys.remove(KeyCode.SPACE);
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

    private void placeBomb() {
        Position bombPos = new Position(gameState.player.pos.x, gameState.player.pos.y);

        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                return;
            }
        }

        gameState.bombs.add(new Bomb(bombPos.x, bombPos.y));
        gameState.player.bombsRemaining--;
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = gameState.bombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.timer--;

            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                bombIterator.remove();
                gameState.player.bombsRemaining++;
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int range = gameState.player.bombRange;

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
                    gameState.score += 10;
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

    private void checkPlayerHit() {
        for (Explosion explosion : gameState.explosions) {
            if (explosion.pos.equals(gameState.player.pos)) {
                gameState.player.lives--;
                if (gameState.player.lives <= 0) {
                    gameOver();
                } else {
                    respawnPlayer();
                }
                break;
            }
        }
    }

    private void respawnPlayer() {
        gameState.player.pos = new Position(1, 1);
    }

    public int score = 0;

    private List<PlayerScore> scoreList = new ArrayList<>();

    private static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }


    private void gameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        Platform.runLater(() -> {
            // Image de fond
            Image backgroundImage = new Image(getClass().getResourceAsStream("/gameoverbomberman.jpg"));
            BackgroundImage bgImage = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(800, 600, false, false, false, false));
            Background background = new Background(bgImage);

            // Saisie du nom
            Label promptLabel = new Label("Entrez votre nom :");
            promptLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            TextField nameField = new TextField();
            nameField.setMaxWidth(200);

            Button submitButton = new Button("Valider le score");
            submitButton.setOnAction(e -> {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    scoreList.add(new PlayerScore(name, gameState.player.score));
                    saveScoresToFile(); // Sauvegarde ici
                    showScoreboard();
                }
            });


            VBox vbox = new VBox(15, promptLabel, nameField, submitButton);
            vbox.setAlignment(Pos.CENTER);
            vbox.setBackground(background);
            vbox.setPrefSize(800, 600);

            Scene gameOverScene = new Scene(vbox);
            primaryStage.setScene(gameOverScene);
        });
    }


    private void loadScoresFromFile() {
        try (Scanner scanner = new Scanner(new java.io.File("score.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    scoreList.add(new PlayerScore(name, score));
                }
            }
        } catch (Exception e) {
            System.out.println("Aucun fichier de score trouvé ou erreur de lecture.");
        }
    }

    private void saveScoresToFile() {
        try (PrintWriter writer = new PrintWriter("score.txt")) {
            for (PlayerScore ps : scoreList) {
                writer.println(ps.name + ":" + ps.score);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'écriture des scores.");
            e.printStackTrace();
        }
    }



    private void showScoreboard() {

        // Tri décroissant des scores
        scoreList.sort((a, b) -> Integer.compare(b.score, a.score));

        Label title = new Label("Classement des scores");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox scoresBox = new VBox(10);
        scoresBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < Math.min(5, scoreList.size()); i++) {
            PlayerScore ps = scoreList.get(i);
            Label label = new Label((i + 1) + ". " + ps.name + " - " + ps.score + " pts");
            label.setStyle("-fx-font-size: 16px;");
            scoresBox.getChildren().add(label);
        }

        Button replayButton = new Button("Rejouer");
        replayButton.setOnAction(e -> restartGame());

        Button quitButton = new Button("Quitter");
        quitButton.setOnAction(e -> Platform.exit());

        VBox root = new VBox(20, title, scoresBox, replayButton, quitButton);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(800, 600);

        Scene scoreScene = new Scene(root);
        primaryStage.setScene(scoreScene);
    }



    private void updateScoreBoard(VBox scoreBox) {
        scoreBox.getChildren().removeIf(node -> node instanceof HBox);
        int rank = 1;
        for (PlayerScore ps : scoreList) {
            Label label = new Label(rank++ + ". " + ps.name + " - " + ps.score);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            scoreBox.getChildren().add(label);
        }
    }





    private Scene originalGameScene;



    private void restartGame() {
        Platform.runLater(() -> {
            try {
                primaryStage.close(); // ← le stage est connu et non null
                BombermanApplication newApp = new BombermanApplication();
                Stage newStage = new Stage();
                newApp.start(newStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }





    private void updateUI() {
        bombsLabel.setText("Bombes: " + gameState.player.bombsRemaining);
        scoreLabel.setText("Score: " + gameState.score);
        levelLabel.setText("Niveau: " + gameState.level);
    }

    private void render() {
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
            // Effet 3D
            gc.setFill(Color.web("#616161"));
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, 3);
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, 3, CELL_SIZE);
            gc.setFill(Color.web("#424242"));
        }

        // Murs destructibles
        gc.setFill(Color.web("#8D6E63"));
        for (Position wall : gameState.destructibleWalls) {
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            // Texture brique
            gc.setFill(Color.web("#A1887F"));
            for (int i = 0; i < 3; i++) {
                gc.fillRect(wall.x * CELL_SIZE + 5, wall.y * CELL_SIZE + i * 12 + 5, CELL_SIZE - 10, 8);
            }
            gc.setFill(Color.web("#8D6E63"));
        }

        // Explosions avec effet de pulsation
        for (Explosion explosion : gameState.explosions) {
            double intensity = (double) explosion.timer / 30;
            Color explosionColor = Color.web("#FF5722").interpolate(Color.web("#FFEB3B"), 1 - intensity);
            gc.setFill(explosionColor);

            double size = CELL_SIZE * (0.5 + 0.5 * intensity);
            double offset = (CELL_SIZE - size) / 2;
            gc.fillOval(explosion.pos.x * CELL_SIZE + offset, explosion.pos.y * CELL_SIZE + offset, size, size);
        }

        // Bombes avec animation de clignotement
        for (Bomb bomb : gameState.bombs) {
            boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;
            gc.setFill(blink ? Color.web("#F44336") : Color.web("#212121"));
            gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

            // Mèche
            gc.setFill(Color.web("#FF9800"));
            gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
        }

        // Joueur avec dégradé
        gc.setFill(Color.web("#2196F3"));
        gc.fillOval(gameState.player.pos.x * CELL_SIZE + 5, gameState.player.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        // Highlight
        gc.setFill(Color.web("#64B5F6"));
        gc.fillOval(gameState.player.pos.x * CELL_SIZE + 8, gameState.player.pos.y * CELL_SIZE + 8, 8, 8);
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
        public int score;
        Position pos;
        int bombsRemaining = 3;
        int bombRange = 2;
        int lives = 3;
        Player(int x, int y) { this.pos = new Position(x, y); }
    }

    static class Bomb {
        Position pos;
        int timer = 120;
        Bomb(int x, int y) { this.pos = new Position(x, y); }
    }

    static class Explosion {
        Position pos;
        int timer = 30;
        Explosion(int x, int y) { this.pos = new Position(x, y); }
    }

    static class GameState {
        Player player;
        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();
        int score = 0;
        int level = 1;

        GameState() {
            player = new Player(1, 1);
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
                            !(i <= 2 && j <= 2) &&
                            random.nextDouble() < 0.3) {
                        destructibleWalls.add(new Position(i, j));
                    }
                }
            }
        }
    }
}