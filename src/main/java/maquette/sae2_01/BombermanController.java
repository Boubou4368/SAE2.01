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
    @FXML
    private Label player1BombsLabel;
    @FXML
    private Label player2BombsLabel;
    @FXML
    private Label player3BombsLabel;
    @FXML
    private Label player4BombsLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private Label livesLabel;
    @FXML
    private Label livesLabel2;
    @FXML
    private Label livesLabel3;
    @FXML
    private Label livesLabel4;
    @FXML
    private ImageView livesIcon;
    @FXML
    private ImageView livesIcon2;
    @FXML
    private ImageView livesIcon3;
    @FXML
    private ImageView livesIcon4;
    @FXML
    private Label bonusLabel;
    @FXML
    private ImageView titre;
    @FXML
    private Label timerLabel;

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final long MOVE_DELAY_NS = 150_000_000;
    private static final long MOVE_DELAY = 150_000_000;
    private static final long ANIMATION_DELAY = 200_000_000;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT, IDLE;
    }

    private Timeline gameTimer;
    private Integer startTimeInSeconds = 180; // 3 minutes = 180 secondes
    private GraphicsContext gc;
    private GameState gameState;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;
    private long[] lastMoveTimes = new long[4];

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

    //joueur1
    private Image P1H, P1B, P1G, P1D;
    private Image currentP1Image = null;

    //joueur2
    private Image P2H, P2B, P2G, P2D;
    private Image currentP2Image = null;

    //joueur3
    private Image P3H, P3B, P3G, P3D;
    private Image currentP3Image = null;

    //joueur4
    private Image P4H, P4B, P4G, P4D;
    private Image currentP4Image = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();

        loadImages();
        gameCanvas.setFocusTraversable(true);
        soundManager = SoundManager.getInstance();
        soundManager.startBackgroundMusic();

        startGameLoop();
        startGameTimer();
        updateUI();
    }

    private void loadImages() {
        loadGameImages();
        loadPlayerImages();
        loadPlayerIcons();
        loadTitleImage();
    }

    private void loadGameImages() {
        brickImage = loadImage("/maquette/sae2_01/brique.png");
        kickImage = loadImage("/maquette/sae2_01/kick.png");
        feuImage = loadImage("/maquette/sae2_01/feu.png");
        vitesseImage = loadImage("/maquette/sae2_01/vitesse.png");
        bombeImage = loadImage("/maquette/sae2_01/bombe.png");
        skullImage = loadImage("/maquette/sae2_01/skull.png");
        explosionGifImage = loadImage("/maquette/sae2_01/explosion.gif");
        fireball = loadImage("/maquette/sae2_01/blast.png");
    }

    private void loadPlayerImages() {
        // Joueur 1
        P1B = loadImage("/maquette/sae2_01/P1_bas.gif");
        P1H = loadImage("/maquette/sae2_01/P1_haut.gif");
        P1G = loadImage("/maquette/sae2_01/P1_gauche.gif");
        P1D = loadImage("/maquette/sae2_01/P1_droite.gif");
        currentP1Image = P1B;

        // Joueur 2
        P2B = loadImage("/maquette/sae2_01/P2_bas.gif");
        P2H = loadImage("/maquette/sae2_01/P2_haut.gif");
        P2G = loadImage("/maquette/sae2_01/P2_gauche.gif");
        P2D = loadImage("/maquette/sae2_01/P2_droite.gif");
        currentP2Image = P2B;

        // Joueur 3
        P3B = loadImage("/maquette/sae2_01/P3_bas.gif");
        P3H = loadImage("/maquette/sae2_01/P3_haut.gif");
        P3G = loadImage("/maquette/sae2_01/P3_gauche.gif");
        P3D = loadImage("/maquette/sae2_01/P3_droite.gif");
        currentP3Image = P3B;

        // Joueur 4
        P4B = loadImage("/maquette/sae2_01/P4_bas.gif");
        P4H = loadImage("/maquette/sae2_01/P4_haut.gif");
        P4G = loadImage("/maquette/sae2_01/P4_gauche.gif");
        P4D = loadImage("/maquette/sae2_01/P4_droite.gif");
        currentP4Image = P4B;
    }

    private void loadPlayerIcons() {
        String[] iconFiles = {"icone.png", "icone2.png", "icone3.png", "icone4.png"};
        ImageView[] icons = {livesIcon, livesIcon2, livesIcon3, livesIcon4};

        for (int i = 0; i < iconFiles.length; i++) {
            iconeImage = loadImage("/maquette/sae2_01/" + iconFiles[i]);
            if (icons[i] != null && iconeImage != null) {
                icons[i].setImage(iconeImage);
            }
        }
    }

    private void loadTitleImage() {
        iconeImage = loadImage("/maquette/sae2_01/titre.png");
        if (titre != null && iconeImage != null) {
            titre.setImage(iconeImage);
        }
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image " + path + ": " + e.getMessage());
            return null;
        }
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
        for (int i = 0; i < 4; i++) {
            handlePlayerInput(currentTime, gameState.players[i], i);
        }
    }

    private void handlePlayerInput(long currentTime, Player player, int playerIndex) {
        if (!player.isAlive) return;

        long lastMoveTime = lastMoveTimes[playerIndex];

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < MOVE_DELAY_NS) {
            // On peut quand même placer des bombes même si on ne peut pas bouger
            checkBombPlacement(player, playerIndex);
            return;
        }

        Position newPos = new Position(player.pos.x, player.pos.y);
        boolean moved = false;

        Direction newDirection = Direction.IDLE;
        KeyCode currentDirectionKey = null;

        // Contrôles spécifiques à chaque joueur
        switch (playerIndex) {
            case 0: // Joueur 1 : ZQSD
                if (pressedKeys.contains(KeyCode.Z)) {
                    newPos.y--;
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.S)) {
                    newPos.y++;
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.Q)) {
                    newPos.x--;
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.D)) {
                    newPos.x++;
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 1: // Joueur 2 : Flèches
                if (pressedKeys.contains(KeyCode.UP)) {
                    newPos.y--;
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    newPos.y++;
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    newPos.x--;
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    newPos.x++;
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 2: // Joueur 3 : OKLM
                if (pressedKeys.contains(KeyCode.O)) {
                    newPos.y--;
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.L)) {
                    newPos.y++;
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.K)) {
                    newPos.x--;
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.M)) {
                    newPos.x++;
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 3: // Joueur 4 : TFGH
                if (pressedKeys.contains(KeyCode.T)) {
                    newPos.y--;
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.G)) {
                    newPos.y++;
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.F)) {
                    newPos.x--;
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.H)) {
                    newPos.x++;
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;
        }

        if (moved && canMoveTo(newPos)) {
            player.pos = newPos;
            updatePlayerImage(playerIndex, newDirection);

            lastMoveTimes[playerIndex] = currentTime;
            soundManager.playSound("walk");

            // Vérifier le kick de bombe après le mouvement réussi
            if (player.canKick && currentDirectionKey != null) {
                tryKickBomb(player, currentDirectionKey);
            } else if (moved && player.canKick && currentDirectionKey != null) {
                // Si le mouvement est bloqué mais que le joueur peut kicker,
                // essayer de kicker une bombe dans cette direction
                tryKickBomb(player, currentDirectionKey);
            }
        }

        // Placement de bombes
        checkBombPlacement(player, playerIndex);
    }

    private void checkBombPlacement(Player player, int playerIndex) {
        KeyCode bombKey = null;

        switch (playerIndex) {
            case 0:
                bombKey = KeyCode.E;
                break;
            case 1:
                bombKey = KeyCode.ENTER;
                break;
            case 2:
                bombKey = KeyCode.P;
                break;
            case 3:
                bombKey = KeyCode.Y;
                break;
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

        // Vérifier que les joueurs ne se chevauchent pas
        for (Player player : gameState.players) {
            if (player.isAlive && pos.equals(player.pos)) {
                return false;
            }
        }

        return true;
    }

    private void updatePlayerImage(int playerNumber, Direction direction) {
        Image[] playerImages = getPlayerImageArray(playerNumber);
        if (playerImages == null) return;

        Image newImage = null;
        switch (direction) {
            case UP:
                newImage = playerImages[0];
                break;
            case DOWN:
                newImage = playerImages[1];
                break;
            case LEFT:
                newImage = playerImages[2];
                break;
            case RIGHT:
                newImage = playerImages[3];
                break;
            case IDLE:
                newImage = getCurrentPlayerImage(playerNumber);
                if (newImage == null) newImage = playerImages[1]; // Default to DOWN
                break;
        }

        setCurrentPlayerImage(playerNumber, newImage);
    }

    private Image[] getPlayerImageArray(int playerNumber) {
        switch (playerNumber) {
            case 0:
                return new Image[]{P1H, P1B, P1G, P1D};
            case 1:
                return new Image[]{P2H, P2B, P2G, P2D};
            case 2:
                return new Image[]{P3H, P3B, P3G, P3D};
            case 3:
                return new Image[]{P4H, P4B, P4G, P4D};
            default:
                return null;
        }
    }

    private Image getCurrentPlayerImage(int playerNumber) {
        switch (playerNumber) {
            case 0:
                return currentP1Image;
            case 1:
                return currentP2Image;
            case 2:
                return currentP3Image;
            case 3:
                return currentP4Image;
            default:
                return null;
        }
    }

    private void setCurrentPlayerImage(int playerNumber, Image image) {
        switch (playerNumber) {
            case 0:
                currentP1Image = image;
                break;
            case 1:
                currentP2Image = image;
                break;
            case 2:
                currentP3Image = image;
                break;
            case 3:
                currentP4Image = image;
                break;
        }
    }

    private Image getPlayerImage(int playerIndex) {
        return getCurrentPlayerImage(playerIndex);
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
        soundManager.playSound("bomb_place"); // Jouer le son de placement de bombe
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = gameState.bombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();

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
                        // La bombe s'arrête, on la convertit en bombe normale
                        Bomb stoppedBomb = new Bomb(kickingBomb.pos.x, kickingBomb.pos.y, kickingBomb.owner);
                        stoppedBomb.timer = kickingBomb.timer;

                        bombIterator.remove();
                        gameState.bombs.add(stoppedBomb);
                        continue;
                    }
                }
            }

            if (bomb.updateTimer()) {
                explodeBomb(bomb);
                bombIterator.remove();

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

                // Vérifier si c'est un mur destructible
                if (gameState.destructibleWalls.contains(explPos)) {
                    gameState.destructibleWalls.remove(explPos);


                    // Révéler l'objet s'il y en a un
                    Item hiddenItem = gameState.hiddenItems.get(explPos);
                    if (hiddenItem != null) {
                        gameState.visibleItems.put(explPos, hiddenItem);
                        gameState.hiddenItems.remove(explPos);
                    }
                    break;
                }
            }
        }
        //soundManager.playSound("explosion"); // Jouer le son d'explosion
    }

    private void tryKickBomb(Player player, KeyCode direction) {
        if (!player.canKick || direction == null) return;

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
        // Supprimer l'ancienne bombe de la liste
        gameState.bombs.remove(bomb);

        // Créer une nouvelle bombe qui bouge
        KickingBomb kickingBomb = new KickingBomb(bomb.pos.x, bomb.pos.y, bomb.owner, direction);
        kickingBomb.timer = bomb.timer; // Conserver le timer existant

        gameState.bombs.add(kickingBomb);
    }

    private boolean canMoveBombTo(Position pos) {
        if (pos.x < 0 || pos.x >= GRID_SIZE || pos.y < 0 || pos.y >= GRID_SIZE) {
            return false;
        }

        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        // Vérifier qu'il n'y a pas déjà une bombe à cette position
        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(pos)) {
                return false;
            }
        }

        return true;
    }

    private void updateExplosions() {
        gameState.explosions.removeIf(explosion -> {
            explosion.timer--;
            return explosion.timer <= 0;
        });
    }

    private void checkPlayersHit() {
        long currentTime = System.currentTimeMillis();

        // Vérifier si l'invincibilité est terminée
        if (gameState.players[0].isInvulnerable && currentTime >= gameState.players[0].invulnerabilityEndTime) {
            gameState.players[0].isInvulnerable = false;
        }
        if (gameState.players[1].isInvulnerable && currentTime >= gameState.players[1].invulnerabilityEndTime) {
            gameState.players[1].isInvulnerable = false;
        }
        if (gameState.players[2].isInvulnerable && currentTime >= gameState.players[2].invulnerabilityEndTime) {
            gameState.players[2].isInvulnerable = false;
        }
        if (gameState.players[3].isInvulnerable && currentTime >= gameState.players[3].invulnerabilityEndTime) {
            gameState.players[3].isInvulnerable = false;
        }

        for (Explosion explosion : gameState.explosions) {
            for (int i = 0; i < 4; i++) {
                Player player = gameState.players[i];
                if (player.isAlive && explosion.pos.equals(player.pos) && !gameState.players[i].isInvulnerable) {
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
        player.isInvulnerable = true;
        player.invulnerabilityEndTime = System.currentTimeMillis() + 3000; // 3 secondes
    }

    private void checkItemPickup() {
        // Vérifier pour tous les joueurs
        for (int i = 0; i < 4; i++) {
            checkItemPickupForPlayer(gameState.players[i], i);
        }
    }

    private void checkItemPickupForPlayer(Player player, int playerIndex) {
        Item item = gameState.visibleItems.get(player.pos);
        if (item != null) {
            applyItemEffect(player, item, playerIndex);
            gameState.visibleItems.remove(player.pos);
        }
    }

    private void applyItemEffect(Player player, Item item, int playerIndex) {
        switch (item.type) {
            case FEU:
                player.bombRange++;
                player.feuBonusCount++;
                player.score += 50;
                break;
            case VITESSE:
                player.speedMultiplier += 0.3;
                player.vitesseBonusCount++;
                player.score += 30;
                break;
            case BOMBE:
                player.maxBombs++;
                player.bombsRemaining++;
                player.bombeBonusCount++;
                player.score += 40;
                break;
            case SKULL:
                player.lives--;
                if (player.lives <= 0) {
                    player.isAlive = false;
                    checkGameOver();
                } else {
                    resetPlayerBonuses(player);
                    respawnPlayer(player, playerIndex);
                }
                break;
            case KICK:
                if (player.kickBonusCount < 2) {
                    player.kickBonusCount++;
                    player.canKick = true;
                    player.score += 60;
                }
                break;
        }
    }

    private void resetPlayerBonuses(Player player) {
        // Réinitialiser tous les bonus aux valeurs par défaut
        player.bombRange = 2;
        player.speedMultiplier = 1.0;
        player.maxBombs = 3;
        player.bombsRemaining = 3;

        // Réinitialiser les compteurs de bonus
        player.feuBonusCount = 0;
        player.vitesseBonusCount = 0;
        player.bombeBonusCount = 0;
        player.kickBonusCount = 0;
        player.canKick = false;
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

    private void gameOver(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
        soundManager.stopBackgroundMusic();
        System.out.println("Game Over: " + message);
    }

    private boolean isPlayerAt(Position pos) {
        for (int i = 0; i < gameState.players.length; i++) {
            if (gameState.players[i] != null && pos.equals(gameState.players[i].pos)) {
                return true;
            }
        }
        return false;
    }

    private void updateUI() {
        // Mise à jour des labels de bombes pour tous les joueurs
        Label[] bombsLabels = {player1BombsLabel, player2BombsLabel, player3BombsLabel, player4BombsLabel};

        for (int i = 0; i < 4; i++) {
            if (bombsLabels[i] != null) {
                bombsLabels[i].setText("Bombes: " + gameState.players[i].bombsRemaining);
            }
        }


        if (levelLabel != null) {
            levelLabel.setText("Niveau: " + gameState.level);
        }

        // Affichage des vies
        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.players[0].lives);
        }

        if (livesLabel2 != null) {
            livesLabel2.setText("x" + gameState.players[1].lives);
        }
        if (livesLabel3 != null) {
            livesLabel3.setText("x" + gameState.players[2].lives);
        }
        if (livesLabel4 != null) {
            livesLabel4.setText("x" + gameState.players[3].lives);
        }

        // Affichage des bonus
        if (bonusLabel != null) {
            String bonusText = String.format("J1 - Feu: %d | Vitesse: %d | Bombe: %d || J2 - Feu: %d | Vitesse: %d | Bombe: %d",
                    gameState.players[0].feuBonusCount,
                    gameState.players[0].vitesseBonusCount,
                    gameState.players[0].bombeBonusCount,
                    gameState.players[1].feuBonusCount,
                    gameState.players[1].vitesseBonusCount,
                    gameState.players[1].bombeBonusCount);
            bonusLabel.setText(bonusText);
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

        // Objets visibles
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
            }

            if (itemImage != null) {
                gc.drawImage(itemImage, pos.x * CELL_SIZE, pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec des couleurs
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
                }
                gc.fillOval(pos.x * CELL_SIZE + 8, pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            }
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

        // Explosions - Utiliser le GIF d'explosion
        for (Explosion explosion : gameState.explosions) {
            if (fireball != null) {
                // Utiliser l'image GIF d'explosion
                gc.drawImage(fireball, explosion.pos.x * CELL_SIZE, explosion.pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec animation de couleur
                double intensity = (double) explosion.timer / 30;
                Color explosionColor = Color.web("#FF5722").interpolate(Color.web("#FFEB3B"), 1 - intensity);
                gc.setFill(explosionColor);

                double size = CELL_SIZE * (0.5 + 0.5 * intensity);
                double offset = (CELL_SIZE - size) / 2;
                gc.fillOval(explosion.pos.x * CELL_SIZE + offset, explosion.pos.y * CELL_SIZE + offset, size, size);
            }
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

        // Bombes - Remplacées par l'image de bombe statique
        for (Bomb bomb : gameState.bombs) {
            if (explosionGifImage != null) {
                // Utiliser l'image de bombe statique
                gc.drawImage(explosionGifImage, bomb.pos.x * CELL_SIZE, bomb.pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec animation clignotante (code original)
                boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;
                gc.setFill(blink ? Color.web("#F44336") : Color.web("#212121"));
                gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

                // Mèche
                gc.setFill(Color.web("#FF9800"));
                gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
            }
        }

    }
}