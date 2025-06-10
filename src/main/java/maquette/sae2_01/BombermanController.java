package maquette.sae2_01;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class BombermanController implements Initializable {
    @FXML
    private Canvas gameCanvas;
    @FXML private Label bombsLabel;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label livesLabel;
    @FXML private Label livesLabel2;
    @FXML private ImageView livesIcon;
    @FXML private ImageView livesIcon2;
    @FXML private Label bonusLabel;
    @FXML private ImageView titre;
    @FXML private Label timerLabel;

    private Timeline gameTimer;
    private Integer startTimeInSeconds = 180; // 3 minutes = 180 secondes

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;

    private GraphicsContext gc;
    private GameState gameState;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    private SoundManager soundManager; // Ajout du SoundManager

    // Images
    private Image brickImage;
    private Image feuImage;
    private Image vitesseImage;
    private Image kickImage;
    private Image bombeImage;
    private Image iconeImage;
    private Image skullImage;
    private Image explosionGifImage;
    private Image fireball;

    // Images d'animation pour les joueurs
    private Image P1H;
    private Image P1B;
    private Image P1G;
    private Image P1D;

    private Image currentP1Image = null;
    private Image currentP2Image = null;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT, IDLE
    }

    private Direction player1Direction = Direction.IDLE;
    private Direction player2Direction = Direction.IDLE;

    private long lastAnimationUpdate = 0;
    private static final long ANIMATION_DELAY = 200_000_000;

    private long lastMoveTimeP1 = 0;
    private long lastMoveTimeP2 = 0;
    private static final long MOVE_DELAY = 150_000_000;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();

        loadImages();
        soundManager = SoundManager.getInstance(); // Initialisation du SoundManager
        soundManager.startBackgroundMusic(); // Démarrer la musique de fond

        gameCanvas.setFocusTraversable(true);

        startGameLoop();
        startGameTimer();
        updateUI();
    }

    private void startGameTimer() {
        timerLabel.setText("Temps restant: " + formatTime(startTimeInSeconds));

        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    startTimeInSeconds--;
                    timerLabel.setText("Temps restant: " + formatTime(startTimeInSeconds));

                    if (startTimeInSeconds <= 0) {
                        gameTimer.stop();
                        gameOver("Temps écoulé! Fin du jeu.");
                    }
                })
        );
        gameTimer.setCycleCount(Timeline.INDEFINITE);
        gameTimer.play();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void loadImages() {
        try {
            brickImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/brique.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image brique.png: " + e.getMessage());
        }

        try {
            kickImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/kick.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image kick.png: " + e.getMessage());
        }

        try {
            feuImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/feu.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image feu.png: " + e.getMessage());
        }

        try {
            vitesseImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/vitesse.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image vitesse.png: " + e.getMessage());
        }

        try {
            bombeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/bombe.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image bombe.png: " + e.getMessage());
        }

        try {
            skullImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/skull.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image skull.png: " + e.getMessage());
        }

        try {
            explosionGifImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/explosion.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image explosion.gif: " + e.getMessage());
        }

        try {
            fireball = new Image(getClass().getResourceAsStream("/maquette/sae2_01/blast.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image explosion.gif: " + e.getMessage());
        }

        try {
            P1B = new Image(getClass().getResourceAsStream("/maquette/sae2_01/P1_bas.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1H = new Image(getClass().getResourceAsStream("/maquette/sae2_01/P1_haut.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1G = new Image(getClass().getResourceAsStream("/maquette/sae2_01/P1_gauche.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1D = new Image(getClass().getResourceAsStream("/maquette/sae2_01/P1_droite.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }

        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/icone.png"));
            if (livesIcon != null) {
                livesIcon.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone.png: " + e.getMessage());
        }
        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/icone2.png"));
            if (livesIcon2 != null) {
                livesIcon2.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone2.png: " + e.getMessage());
        }
        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/titre.png"));
            if (titre != null) {
                titre.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image titre.png: " + e.getMessage());
        }
        currentP1Image = P1B;
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
        checkPlayersHit();
        checkItemPickup();
    }

    private void handleInput(long currentTime) {
        handlePlayerInput(gameState.player1, currentTime, lastMoveTimeP1, 1);
        handlePlayerInput(gameState.player2, currentTime, lastMoveTimeP2, 2);
    }

    private void handlePlayerInput(Player player, long currentTime, long lastMoveTime, int playerNumber) {
        long currentMoveDelay = (long) (MOVE_DELAY / player.speedMultiplier);

        KeyCode upKey, downKey, leftKey, rightKey, bombKey;
        if (playerNumber == 1) {
            upKey = KeyCode.Z;
            downKey = KeyCode.S;
            leftKey = KeyCode.Q;
            rightKey = KeyCode.D;
            bombKey = KeyCode.SPACE;
        } else {
            upKey = KeyCode.UP;
            downKey = KeyCode.DOWN;
            leftKey = KeyCode.LEFT;
            rightKey = KeyCode.RIGHT;
            bombKey = KeyCode.ENTER;
        }

        if (pressedKeys.contains(bombKey) && player.bombsRemaining > 0) {
            boolean canPlaceBomb = true;
            Position bombPos = new Position(player.pos.x, player.pos.y);

            for (Bomb bomb : gameState.bombs) {
                if (bomb.pos.equals(bombPos)) {
                    canPlaceBomb = false;
                    break;
                }
            }

            if (canPlaceBomb) {
                placeBomb(player);
            }
            pressedKeys.remove(bombKey);
        }

        if (currentTime - lastMoveTime < currentMoveDelay) {
            return;
        }

        Position newPos = new Position(player.pos.x, player.pos.y);
        boolean moved = false;
        Direction newDirection = Direction.IDLE;

        if (pressedKeys.contains(upKey)) {
            newPos.y--;
            moved = true;
            newDirection = Direction.UP;
        } else if (pressedKeys.contains(downKey)) {
            newPos.y++;
            moved = true;
            newDirection = Direction.DOWN;
        } else if (pressedKeys.contains(leftKey)) {
            newPos.x--;
            moved = true;
            newDirection = Direction.LEFT;
        } else if (pressedKeys.contains(rightKey)) {
            newPos.x++;
            moved = true;
            newDirection = Direction.RIGHT;
        }

        if (playerNumber == 1) {
            player1Direction = newDirection;
            updatePlayerImage(1, newDirection);
        } else {
            player2Direction = newDirection;
            updatePlayerImage(2, newDirection);
        }

        if (moved && canMoveTo(newPos)) {
            player.pos = newPos;
            if (playerNumber == 1) {
                lastMoveTimeP1 = currentTime;
            } else {
                lastMoveTimeP2 = currentTime;
            }
            soundManager.playSound("walk"); // Jouer le son de pas
        }

        if (player.canKick && moved) {
            KeyCode directionKey = null;
            if (pressedKeys.contains(upKey)) directionKey = upKey;
            else if (pressedKeys.contains(downKey)) directionKey = downKey;
            else if (pressedKeys.contains(leftKey)) directionKey = leftKey;
            else if (pressedKeys.contains(rightKey)) directionKey = rightKey;

            if (directionKey != null) {
                tryKickBomb(player, directionKey);
            }
        }
    }

    private void updatePlayerImage(int playerNumber, Direction direction) {
        if (playerNumber == 1) {
            switch (direction) {
                case UP:
                    currentP1Image = P1H;
                    break;
                case DOWN:
                    currentP1Image = P1B;
                    break;
                case LEFT:
                    currentP1Image = P1G;
                    break;
                case RIGHT:
                    currentP1Image = P1D;
                    break;
                case IDLE:
                    if (currentP1Image == null) {
                        currentP1Image = P1B;
                    }
                    break;
            }
        }
    }

    private void tryKickBomb(Player player, KeyCode direction) {
        if (!player.canKick) return;

        Position kickDirection = getDirectionFromKey(direction);
        if (kickDirection == null) return;

        Position bombPos = new Position(player.pos.x + kickDirection.x, player.pos.y + kickDirection.y);

        Bomb bombToKick = null;
        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                if (!(bomb instanceof KickingBomb)) {
                    bombToKick = bomb;
                }
                break;
            }
        }

        if (bombToKick != null) {
            kickBomb(bombToKick, kickDirection);
            soundManager.playSound("kick"); // Jouer le son de kick
        }
    }

    static class KickingBomb extends Bomb {
        Position direction;
        int kickSpeed = 8;
        int kickTimer = 0;

        KickingBomb(int x, int y, Player owner, Position direction) {
            super(x, y, owner);
            this.direction = direction;
        }

        boolean shouldMove() {
            kickTimer++;
            if (kickTimer >= kickSpeed) {
                kickTimer = 0;
                return true;
            }
            return false;
        }
    }

    private Position getDirectionFromKey(KeyCode key) {
        switch (key) {
            case Z:
            case UP:
                return new Position(0, -1);
            case S:
            case DOWN:
                return new Position(0, 1);
            case Q:
            case LEFT:
                return new Position(-1, 0);
            case D:
            case RIGHT:
                return new Position(1, 0);
            default:
                return null;
        }
    }

    private void kickBomb(Bomb bomb, Position direction) {
        gameState.bombs.remove(bomb);

        KickingBomb kickingBomb = new KickingBomb(bomb.pos.x, bomb.pos.y, bomb.owner, direction);
        kickingBomb.timer = bomb.timer;

        gameState.bombs.add(kickingBomb);
    }

    private boolean canMoveBombTo(Position pos) {
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

        if (pos.equals(gameState.player1.pos) || pos.equals(gameState.player2.pos)) {
            return false;
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
        soundManager.playSound("bomb_place"); // Jouer le son de placement de bombe
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = gameState.bombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.timer--;

            if (bomb instanceof KickingBomb) {
                KickingBomb kickingBomb = (KickingBomb) bomb;
                if (kickingBomb.shouldMove()) {
                    Position newPos = new Position(
                            kickingBomb.pos.x + kickingBomb.direction.x,
                            kickingBomb.pos.y + kickingBomb.direction.y
                    );

                    if (canMoveBombTo(newPos) && !isPlayerAt(newPos)) {
                        kickingBomb.pos = newPos;
                    } else {
                        Bomb stoppedBomb = new Bomb(kickingBomb.pos.x, kickingBomb.pos.y, kickingBomb.owner);
                        stoppedBomb.timer = kickingBomb.timer;

                        bombIterator.remove();
                        gameState.bombs.add(stoppedBomb);
                        continue;
                    }
                }
            }

            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                bombIterator.remove();
                bomb.owner.bombsRemaining++;
            }
        }
    }

    private boolean isPlayerAt(Position pos) {
        return pos.equals(gameState.player1.pos) || pos.equals(gameState.player2.pos);
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

                    Item hiddenItem = gameState.hiddenItems.get(explPos);
                    if (hiddenItem != null) {
                        gameState.visibleItems.put(explPos, hiddenItem);
                        gameState.hiddenItems.remove(explPos);
                    }
                    break;
                }
            }
        }
        soundManager.playSound("explosion"); // Jouer le son d'explosion
    }

    private void updateExplosions() {
        gameState.explosions.removeIf(explosion -> {
            explosion.timer--;
            return explosion.timer <= 0;
        });
    }

    private void checkPlayersHit() {
        long currentTime = System.currentTimeMillis();

        if (gameState.player1.isInvulnerable && currentTime >= gameState.player1.invulnerabilityEndTime) {
            gameState.player1.isInvulnerable = false;
        }
        if (gameState.player2.isInvulnerable && currentTime >= gameState.player2.invulnerabilityEndTime) {
            gameState.player2.isInvulnerable = false;
        }

        for (Explosion explosion : gameState.explosions) {
            if (explosion.pos.equals(gameState.player1.pos) && !gameState.player1.isInvulnerable) {
                gameState.player1.lives--;
                if (gameState.player1.lives <= 0) {
                    gameOver("Joueur 2 gagne !");
                } else {
                    resetPlayerBonuses(gameState.player1);
                    respawnPlayer(gameState.player1, 1);
                    soundManager.playSound("death"); // Jouer le son de mort
                }
            }

            if (explosion.pos.equals(gameState.player2.pos) && !gameState.player2.isInvulnerable) {
                gameState.player2.lives--;
                if (gameState.player2.lives <= 0) {
                    gameOver("Joueur 1 gagne !");
                } else {
                    resetPlayerBonuses(gameState.player2);
                    respawnPlayer(gameState.player2, 2);
                    soundManager.playSound("death"); // Jouer le son de mort
                }
            }
        }
    }

    private void resetPlayerBonuses(Player player) {
        player.bombRange = 2;
        player.speedMultiplier = 1.0;
        player.maxBombs = 3;
        player.bombsRemaining = 3;

        player.feuBonusCount = 0;
        player.vitesseBonusCount = 0;
        player.bombeBonusCount = 0;
        player.kickBonusCount = 0;
        player.canKick = false;
    }

    private void checkItemPickup() {
        checkItemPickupForPlayer(gameState.player1);
        checkItemPickupForPlayer(gameState.player2);
    }

    private void checkItemPickupForPlayer(Player player) {
        Item item = gameState.visibleItems.get(player.pos);
        if (item != null) {
            switch (item.type) {
                case FEU:
                    player.bombRange++;
                    player.feuBonusCount++;
                    player.score += 50;
                    soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case VITESSE:
                    player.speedMultiplier += 0.3;
                    player.vitesseBonusCount++;
                    player.score += 30;
                    soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case BOMBE:
                    player.maxBombs++;
                    player.bombsRemaining++;
                    player.bombeBonusCount++;
                    player.score += 40;
                    soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case SKULL:
                    player.lives--;
                    if (player.lives <= 0) {
                        String winner = (player == gameState.player1) ? "Joueur 2 gagne !" : "Joueur 1 gagne !";
                        gameOver(winner);
                    } else {
                        resetPlayerBonuses(player);
                        int playerNumber = (player == gameState.player1) ? 1 : 2;
                        respawnPlayer(player, playerNumber);
                    }
                    break;
                case KICK:
                    if (player.kickBonusCount < 2) {
                        player.kickBonusCount++;
                        player.canKick = true;
                        player.score += 60;
                        soundManager.playSound("pickup"); // Jouer le son de ramassage
                    }
                    break;
            }

            gameState.visibleItems.remove(player.pos);
        }
    }

    private void respawnPlayer(Player player, int playerNumber) {
        if (playerNumber == 1) {
            player.pos = new Position(1, 1);
        } else {
            player.pos = new Position(GRID_SIZE - 2, GRID_SIZE - 2);
        }

        player.isInvulnerable = true;
        player.invulnerabilityEndTime = System.currentTimeMillis() + 2000;
    }

    private void gameOver(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (message.contains("gagne")) {
            soundManager.playSound("win"); // Jouer le son de victoire
        } else {
            soundManager.playSound("lose"); // Jouer le son de défaite
        }
        soundManager.stopBackgroundMusic(); // Arrêter la musique de fond
        System.out.println("Game Over: " + message);
    }

    private void updateUI() {
        bombsLabel.setText("J1 Bombes: " + gameState.player1.bombsRemaining + "/" + gameState.player1.maxBombs);
        scoreLabel.setText("J1 Score: " + gameState.player1.score + " | J2 Score: " + gameState.player2.score);
        levelLabel.setText("Niveau: " + gameState.level);

        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.player1.lives);
        }

        if (livesLabel2 != null) {
            livesLabel2.setText("x" + gameState.player2.lives);
        }

        if (bonusLabel != null) {
            String bonusText = String.format("J1 - Feu: %d | Vitesse: %d | Bombe: %d | Kick: %d || J2 - Feu: %d | Vitesse: %d | Bombe: %d | Kick: %d",
                    gameState.player1.feuBonusCount,
                    gameState.player1.vitesseBonusCount,
                    gameState.player1.bombeBonusCount,
                    gameState.player1.kickBonusCount,
                    gameState.player2.feuBonusCount,
                    gameState.player2.vitesseBonusCount,
                    gameState.player2.bombeBonusCount,
                    gameState.player2.kickBonusCount);
            bonusLabel.setText(bonusText);
        }
    }

    private void render() {
        gc.setFill(Color.web("#2E7D32"));
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        gc.setStroke(Color.web("#1B5E20"));
        gc.setLineWidth(1);
        for (int i = 0; i <= GRID_SIZE; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, gameCanvas.getHeight());
            gc.strokeLine(0, i * CELL_SIZE, gameCanvas.getWidth(), i * CELL_SIZE);
        }

        for (Map.Entry<Position, Item> entry : gameState.visibleItems.entrySet()) {
            Position pos = entry.getKey();
            Item item = entry.getValue();

            Image itemImage = null;
            switch (item.type) {
                case FEU:
                    itemImage = feuImage;
                    break;
                case VITESSE:
                    itemImage = vitesseImage;
                    break;
                case BOMBE:
                    itemImage = bombeImage;
                    break;
                case SKULL:
                    itemImage = skullImage;
                    break;
                case KICK:
                    itemImage = kickImage;
                    break;
            }

            if (itemImage != null) {
                gc.drawImage(itemImage, pos.x * CELL_SIZE, pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                switch (item.type) {
                    case FEU:
                        gc.setFill(Color.web("#FF4444"));
                        break;
                    case VITESSE:
                        gc.setFill(Color.web("#44FF44"));
                        break;
                    case BOMBE:
                        gc.setFill(Color.web("#4444FF"));
                        break;
                    case SKULL:
                        gc.setFill(Color.web("#8B008B"));
                        break;
                    case KICK:
                        gc.setFill(Color.web("#FFD700"));
                        break;
                }
                gc.fillOval(pos.x * CELL_SIZE + 8, pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            }
        }

        gc.setFill(Color.web("#424242"));
        for (Position wall : gameState.walls) {
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            gc.setFill(Color.web("#616161"));
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, 3);
            gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, 3, CELL_SIZE);
            gc.setFill(Color.web("#424242"));
        }

        for (Position wall : gameState.destructibleWalls) {
            if (brickImage != null) {
                gc.drawImage(brickImage, wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                gc.setFill(Color.web("#8D6E63"));
                gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.web("#A1887F"));
                for (int i = 0; i < 3; i++) {
                    gc.fillRect(wall.x * CELL_SIZE + 5, wall.y * CELL_SIZE + i * 12 + 5, CELL_SIZE - 10, 8);
                }
            }
        }

        for (Explosion explosion : gameState.explosions) {
            if (fireball != null) {
                gc.drawImage(fireball, explosion.pos.x * CELL_SIZE, explosion.pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                double intensity = (double) explosion.timer / 30;
                Color explosionColor = Color.web("#FF5722").interpolate(Color.web("#FFEB3B"), 1 - intensity);
                gc.setFill(explosionColor);

                double size = CELL_SIZE * (0.5 + 0.5 * intensity);
                double offset = (CELL_SIZE - size) / 2;
                gc.fillOval(explosion.pos.x * CELL_SIZE + offset, explosion.pos.y * CELL_SIZE + offset, size, size);
            }
        }

        for (Bomb bomb : gameState.bombs) {
            if (explosionGifImage != null) {
                gc.drawImage(explosionGifImage, bomb.pos.x * CELL_SIZE, bomb.pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                if (bomb instanceof KickingBomb) {
                    gc.setFill(Color.web("#FFD700", 0.3));
                    gc.fillOval(bomb.pos.x * CELL_SIZE + 2, bomb.pos.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                }
            } else {
                boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;
                Color bombColor = blink ? Color.web("#F44336") : Color.web("#212121");

                if (bomb instanceof KickingBomb) {
                    bombColor = blink ? Color.web("#FF9800") : Color.web("#424242");
                }

                gc.setFill(bombColor);
                gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

                gc.setFill(Color.web("#FF9800"));
                gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
            }
        }

        boolean shouldDrawPlayer1 = true;
        if (gameState.player1.isInvulnerable) {
            shouldDrawPlayer1 = (System.currentTimeMillis() / 200) % 2 == 0;
        }

        if (shouldDrawPlayer1) {
            if (currentP1Image != null) {
                gc.drawImage(currentP1Image,
                        gameState.player1.pos.x * CELL_SIZE,
                        gameState.player1.pos.y * CELL_SIZE,
                        CELL_SIZE,
                        CELL_SIZE);
            } else {
                gc.setFill(Color.web("#2196F3"));
                gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 5,
                        gameState.player1.pos.y * CELL_SIZE + 5,
                        CELL_SIZE - 10,
                        CELL_SIZE - 10);
                gc.setFill(Color.web("#64B5F6"));
                gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 8,
                        gameState.player1.pos.y * CELL_SIZE + 8, 8, 8);
            }
        }

        boolean shouldDrawPlayer2 = true;
        if (gameState.player2.isInvulnerable) {
            shouldDrawPlayer2 = (System.currentTimeMillis() / 200) % 2 == 0;
        }

        if (shouldDrawPlayer2) {
            gc.setFill(Color.web("#F44336"));
            gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 5,
                    gameState.player2.pos.y * CELL_SIZE + 5,
                    CELL_SIZE - 10,
                    CELL_SIZE - 10);
            gc.setFill(Color.web("#EF5350"));
            gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 8,
                    gameState.player2.pos.y * CELL_SIZE + 8, 8, 8);
        }
    }

    enum ItemType {
        FEU, VITESSE, BOMBE, SKULL, KICK
    }

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
        int maxBombs = 3;
        int bombRange = 1;
        int lives = 3;
        double speedMultiplier = 1.0;
        int score = 0;

        int feuBonusCount = 0;
        int vitesseBonusCount = 0;
        int bombeBonusCount = 0;
        int kickBonusCount = 0;
        boolean canKick = false;

        boolean isInvulnerable = false;
        long invulnerabilityEndTime = 0;

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

    static class Item {
        ItemType type;
        Item(ItemType type) { this.type = type; }
    }

    static class GameState {
        Player player1;
        Player player2;
        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();
        Map<Position, Item> hiddenItems = new HashMap<>();
        Map<Position, Item> visibleItems = new HashMap<>();
        int level = 1;

        GameState() {
            player1 = new Player(1, 1);
            player2 = new Player(GRID_SIZE - 2, GRID_SIZE - 2);
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

            List<Position> availablePositions = new ArrayList<>();
            Random random = new Random();

            for (int i = 1; i < GRID_SIZE - 1; i++) {
                for (int j = 1; j < GRID_SIZE - 1; j++) {
                    Position pos = new Position(i, j);
                    if (!walls.contains(pos) &&
                            !isStartingArea(pos) &&
                            random.nextDouble() < 0.3) {
                        destructibleWalls.add(pos);
                        availablePositions.add(pos);
                    }
                }
            }

            placeItems(availablePositions, random);
        }

        private boolean isStartingArea(Position pos) {
            if (pos.x <= 2 && pos.y <= 2) return true;
            if (pos.x >= GRID_SIZE - 3 && pos.y >= GRID_SIZE - 3) return true;
            return false;
        }

        private void placeItems(List<Position> availablePositions, Random random) {
            if (availablePositions.size() < 19) {
                System.err.println("Pas assez de murs destructibles pour placer tous les objets");
                return;
            }

            Collections.shuffle(availablePositions, random);

            int index = 0;

            for (int i = 0; i < 4 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.FEU));
            }

            for (int i = 0; i < 4 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.VITESSE));
            }

            for (int i = 0; i < 8 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.BOMBE));
            }

            for (int i = 0; i < 2 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.KICK));
            }

            if (index < availablePositions.size()) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.SKULL));
            }
        }
    }
}
