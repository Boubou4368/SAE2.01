package maquette.sae2_01;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class BombermanController implements Initializable {
    @FXML private Canvas gameCanvas;
    @FXML private Label player1BombsLabel;
    @FXML private Label player2BombsLabel;
    @FXML private Label player3BombsLabel;
    @FXML private Label player4BombsLabel;
    @FXML private Label levelLabel;

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;

    private GraphicsContext gc;
    private GameState gameState;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    // Contrôle de la vitesse de déplacement pour chaque joueur
    private long[] lastMoveTimes = new long[4];
    private static final long MOVE_DELAY = 150_000_000; // 150ms en nanosecondes

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();

        // Configurer le canvas pour recevoir les événements clavier
        gameCanvas.setFocusTraversable(true);

        startGameLoop();
        updateUI();
    }

    public void requestFocus() {
        gameCanvas.requestFocus();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());
    }

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

    private void update(long currentTime) {
        handleInput(currentTime);
        updateBombs();
        updateExplosions();
        checkPlayerHit();
    }

    private void handleInput(long currentTime) {
        // Gestion du joueur 1 (Flèches + Entrée)
        handlePlayerInput(currentTime, gameState.players[0], 0);

        // Gestion du joueur 2 (ZQSD + Espace)
        handlePlayerInput(currentTime, gameState.players[1], 1);

        // Gestion du joueur 3 (IJKL + U)
        handlePlayerInput(currentTime, gameState.players[2], 2);

        // Gestion du joueur 4 (Pavé numérique 8456 + 0)
        handlePlayerInput(currentTime, gameState.players[3], 3);
    }

    private void handlePlayerInput(long currentTime, Player player, int playerIndex) {
        if (!player.isAlive) return;

        long lastMoveTime = lastMoveTimes[playerIndex];

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < MOVE_DELAY) {
            // On peut quand même placer des bombes même si on ne peut pas bouger
            checkBombPlacement(player, playerIndex);
            return;
        }

        Position newPos = new Position(player.pos.x, player.pos.y);
        boolean moved = false;

        // Contrôles spécifiques à chaque joueur
        switch (playerIndex) {
            case 0: // Joueur 1 : ZQSD
                if (pressedKeys.contains(KeyCode.Z)) {
                    newPos.y--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.S)) {
                    newPos.y++;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.Q)) {
                    newPos.x--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.D)) {
                    newPos.x++;
                    moved = true;
                }
                break;

            case 1: // Joueur 2 : Flèches
                if (pressedKeys.contains(KeyCode.UP)) {
                    newPos.y--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    newPos.y++;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    newPos.x--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    newPos.x++;
                    moved = true;
                }
                break;

            case 2: // Joueur 3 : OKLM
                if (pressedKeys.contains(KeyCode.O)) {
                    newPos.y--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.L)) {
                    newPos.y++;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.K)) {
                    newPos.x--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.M)) {
                    newPos.x++;
                    moved = true;
                }
                break;

            case 3: // Joueur 4 : TFGH
                if (pressedKeys.contains(KeyCode.T)) {
                    newPos.y--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.G)) {
                    newPos.y++;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.F)) {
                    newPos.x--;
                    moved = true;
                } else if (pressedKeys.contains(KeyCode.H)) {
                    newPos.x++;
                    moved = true;
                }
                break;
        }

        if (moved && canMoveTo(newPos)) {
            player.pos = newPos;
            lastMoveTimes[playerIndex] = currentTime;
        }

        // Placement de bombes
        checkBombPlacement(player, playerIndex);
    }

    private void checkBombPlacement(Player player, int playerIndex) {
        KeyCode bombKey = null;

        switch (playerIndex) {
            case 0: bombKey = KeyCode.E; break;
            case 1: bombKey = KeyCode.ENTER; break;
            case 2: bombKey = KeyCode.P; break;
            case 3: bombKey = KeyCode.Y; break;
        }

        if (pressedKeys.contains(bombKey) && player.bombsRemaining > 0) {
            placeBomb(player, playerIndex + 1);
            pressedKeys.remove(bombKey);
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

    private void placeBomb(Player player, int playerNumber) {
        Position bombPos = new Position(player.pos.x, player.pos.y);

        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                return;
            }
        }

        gameState.bombs.add(new Bomb(bombPos.x, bombPos.y, playerNumber));
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
                // Rendre la bombe au bon joueur
                if (bomb.owner >= 1 && bomb.owner <= 4) {
                    gameState.players[bomb.owner - 1].bombsRemaining++;
                }
            }
        }
    }

    private void explodeBomb(Bomb bomb) {
        int range = gameState.players[bomb.owner - 1].bombRange;

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
            for (int i = 0; i < 4; i++) {
                Player player = gameState.players[i];
                if (player.isAlive && explosion.pos.equals(player.pos)) {
                    player.lives--;
                    if (player.lives <= 0) {
                        player.isAlive = false;
                        checkGameOver();
                    } else {
                        respawnPlayer(player, i);
                    }
                }
            }
        }
    }

    private void respawnPlayer(Player player, int playerIndex) {
        Position[] spawnPositions = {
                new Position(1, 1),                             // Joueur 1
                new Position(GRID_SIZE - 2, GRID_SIZE - 2),     // Joueur 2
                new Position(1, GRID_SIZE - 2),                 // Joueur 3
                new Position(GRID_SIZE - 2, 1)                  // Joueur 4
        };
        player.pos = spawnPositions[playerIndex];
    }

    private void checkGameOver() {
        int alivePlayers = 0;
        int winner = -1;

        for (int i = 0; i < 4; i++) {
            if (gameState.players[i].isAlive) {
                alivePlayers++;
                winner = i + 1;
            }
        }

        if (alivePlayers <= 1) {
            gameOver(winner);
        }
    }

    private void gameOver(int winner) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (winner > 0) {
            System.out.println("Joueur " + winner + " gagne !");
        } else {
            System.out.println("Match nul !");
        }
    }

    private void updateUI() {

        Label[] bombsLabels = {player1BombsLabel, player2BombsLabel, player3BombsLabel, player4BombsLabel};

        for (int i = 0; i < 4; i++) {
            if (bombsLabels[i] != null) {
                bombsLabels[i].setText("Bombes: " + gameState.players[i].bombsRemaining);
            }
        }
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

        // Bombes avec animation de clignotement et couleurs par joueur
        for (Bomb bomb : gameState.bombs) {
            boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;

            Color[] bombColors = {
                    blink ? Color.web("#2196F3") : Color.web("#1976D2"), // Bleu (Joueur 1)
                    blink ? Color.web("#F44336") : Color.web("#D32F2F"), // Rouge (Joueur 2)
                    blink ? Color.web("#4CAF50") : Color.web("#388E3C"), // Vert (Joueur 3)
                    blink ? Color.web("#FF9800") : Color.web("#F57C00")  // Orange (Joueur 4)
            };

            Color bombColor = bombColors[bomb.owner - 1];
            gc.setFill(bombColor);
            gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

            // Mèche
            gc.setFill(Color.web("#FFEB3B"));
            gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
        }

        // Couleurs des joueurs
        Color[] playerColors = {
                Color.web("#2196F3"), // Bleu (Joueur 1)
                Color.web("#F44336"), // Rouge (Joueur 2)
                Color.web("#4CAF50"), // Vert (Joueur 3)
                Color.web("#FF9800")  // Orange (Joueur 4)
        };

        Color[] highlightColors = {
                Color.web("#64B5F6"), // Bleu clair
                Color.web("#EF5350"), // Rouge clair
                Color.web("#81C784"), // Vert clair
                Color.web("#FFB74D")  // Orange clair
        };

        // Affichage des joueurs
        for (int i = 0; i < 4; i++) {
            Player player = gameState.players[i];
            if (player.isAlive) {
                // Corps du joueur
                gc.setFill(playerColors[i]);
                gc.fillOval(player.pos.x * CELL_SIZE + 5, player.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);

                // Highlight
                gc.setFill(highlightColors[i]);
                gc.fillOval(player.pos.x * CELL_SIZE + 8, player.pos.y * CELL_SIZE + 8, 8, 8);

                // Numéro du joueur
                gc.setFill(Color.WHITE);
                gc.fillText(String.valueOf(i + 1), player.pos.x * CELL_SIZE + CELL_SIZE/2 - 3, player.pos.y * CELL_SIZE + CELL_SIZE/2 + 3);
            }
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
        boolean isAlive = true;
        Player(int x, int y) { this.pos = new Position(x, y); }
    }

    static class Bomb {
        Position pos;
        int timer = 120;
        int owner; // 1-4 pour les joueurs
        Bomb(int x, int y, int owner) {
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
        Player[] players = new Player[4];
        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();


        GameState() {
            // Positions de départ aux 4 coins
            players[0] = new Player(1, 1);                      // Coin supérieur gauche
            players[1] = new Player(GRID_SIZE - 2, GRID_SIZE - 2); // Coin inférieur droit
            players[2] = new Player(1, GRID_SIZE - 2);           // Coin inférieur gauche
            players[3] = new Player(GRID_SIZE - 2, 1);           // Coin supérieur droit
            initializeMap();
        }

        private void initializeMap() {
            // Murs de bordure et murs fixes
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (i == 0 || i == GRID_SIZE - 1 || j == 0 || j == GRID_SIZE - 1) {
                        walls.add(new Position(i, j));
                    } else if (i % 2 == 0 && j % 2 == 0) {
                        walls.add(new Position(i, j));
                    }
                }
            }

            // Murs destructibles avec zones de sécurité pour chaque joueur
            Random random = new Random();
            for (int i = 1; i < GRID_SIZE - 1; i++) {
                for (int j = 1; j < GRID_SIZE - 1; j++) {
                    Position pos = new Position(i, j);
                    if (!walls.contains(pos) && !isInSafeZone(i, j) && random.nextDouble() < 0.25) {
                        destructibleWalls.add(pos);
                    }
                }
            }
        }

        private boolean isInSafeZone(int x, int y) {
            // Zones de sécurité 2x2 pour chaque joueur
            return (x <= 2 && y <= 2) ||                           // Joueur 1
                    (x >= GRID_SIZE - 3 && y >= GRID_SIZE - 3) ||    // Joueur 2
                    (x <= 2 && y >= GRID_SIZE - 3) ||                // Joueur 3
                    (x >= GRID_SIZE - 3 && y <= 2);                  // Joueur 4
        }
    }
}