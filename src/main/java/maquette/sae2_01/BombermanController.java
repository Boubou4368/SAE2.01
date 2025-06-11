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

        gameCanvas.setFocusTraversable(true);
        soundManager = SoundManager.getInstance();
        soundManager.startBackgroundMusic();
        loadImages();

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
        if (!player.getisAlive()) return;

        long lastMoveTime = lastMoveTimes[playerIndex];

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < MOVE_DELAY_NS) {
            // On peut quand même placer des bombes même si on ne peut pas bouger
            checkBombPlacement(player, playerIndex);
            return;
        }

        Position newPos = new Position(player.getPos().getX(), player.getPos().getY());
        boolean moved = false;

        Direction newDirection = Direction.IDLE;
        KeyCode currentDirectionKey = null;

        // Contrôles spécifiques à chaque joueur
        switch (playerIndex) {
            case 0: // Joueur 1 : ZQSD
                if (pressedKeys.contains(KeyCode.Z)) {
                    newPos.setY(player.getPos().getY() - 1);
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.S)) {
                    newPos.setY(player.getPos().getY() + 1);
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.Q)) {
                    newPos.setX(player.getPos().getX() - 1);
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.D)) {
                    newPos.setX(player.getPos().getX() + 1);
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 1: // Joueur 2 : Flèches
                if (pressedKeys.contains(KeyCode.UP)) {
                    newPos.setY(player.getPos().getY() - 1);
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    newPos.setY(player.getPos().getY() + 1);
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    newPos.setX(player.getPos().getX() - 1);
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    newPos.setX(player.getPos().getX() + 1);
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 2: // Joueur 3 : OKLM
                if (pressedKeys.contains(KeyCode.O)) {
                    newPos.setY(player.getPos().getY() - 1);
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.L)) {
                    newPos.setY(player.getPos().getY() + 1);
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.K)) {
                    newPos.setX(player.getPos().getX() - 1);
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.M)) {
                    newPos.setX(player.getPos().getX() + 1);
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;

            case 3: // Joueur 4 : TFGH
                if (pressedKeys.contains(KeyCode.T)) {
                    newPos.setY(player.getPos().getY() - 1);
                    moved = true;
                    newDirection = Direction.UP;
                } else if (pressedKeys.contains(KeyCode.G)) {
                    newPos.setY(player.getPos().getY() + 1);
                    moved = true;
                    newDirection = Direction.DOWN;
                } else if (pressedKeys.contains(KeyCode.F)) {
                    newPos.setX(player.getPos().getX() - 1);
                    moved = true;
                    newDirection = Direction.LEFT;
                } else if (pressedKeys.contains(KeyCode.H)) {
                    newPos.setX(player.getPos().getX() + 1);
                    moved = true;
                    newDirection = Direction.RIGHT;
                }
                break;
        }

        if (moved && canMoveTo(newPos)) {
            player.setPos(newPos);
            updatePlayerImage(playerIndex, newDirection);

            lastMoveTimes[playerIndex] = currentTime;
            soundManager.playSound("walk");

            // Vérifier le kick de bombe après le mouvement réussi
            if (player.getcanKick() && currentDirectionKey != null) {
                tryKickBomb(player, currentDirectionKey);
            } else if (moved && player.getcanKick() && currentDirectionKey != null) {
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

        if (pressedKeys.contains(bombKey) && player.getBombsRemaining() > 0) {
            placeBomb(player, playerIndex + 1);
            pressedKeys.remove(bombKey);
        }
    }

    private boolean canMoveTo(Position pos) {
        if (pos.getX() < 0 || pos.getX() >= GRID_SIZE || pos.getY() < 0 || pos.getY() >= GRID_SIZE) {
            return false;
        }

        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(pos)) {
                return false;
            }
        }

        // Vérifier que les joueurs ne se chevauchent pas
        for (Player player : gameState.players) {
            if (player.getisAlive() && pos.equals(player.getPos())) {
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
        Position bombPos = new Position(player.getPos().getX(), player.getPos().getY());

        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(bombPos)) {
                return;
            }
        }


        gameState.bombs.add(new Bomb(bombPos.getX(), bombPos.getY(), playerNumber));
        player.setBombsRemaining(player.getBombsRemaining() - 1);
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
                            kickingBomb.getPos().getX() + kickingBomb.getDirection().getX(),
                            kickingBomb.getPos().getY() + kickingBomb.getDirection().getY()
                    );

                    if (canMoveBombTo(newPos) && !isPlayerAt(newPos)) {
                        kickingBomb.setPos(newPos);
                    } else {
                        // La bombe s'arrête, on la convertit en bombe normale
                        Bomb stoppedBomb = new Bomb(kickingBomb.getPos().getX(), kickingBomb.getPos().getY(), kickingBomb.getOwner());
                        stoppedBomb.setTimer(kickingBomb.getTimer());

                        bombIterator.remove();
                        gameState.bombs.add(stoppedBomb);
                        continue;
                    }
                }
            }

            if (bomb.updateTimer()) {
                explodeBomb(bomb);
                bombIterator.remove();

                if (bomb.getOwner() >= 1 && bomb.getOwner() <= 4) {
                    gameState.players[bomb.getOwner() - 1].setBombsRemaining(gameState.players[bomb.getOwner() - 1].getBombsRemaining()+1);
                }
            }
        }
    }


    private void explodeBomb(Bomb bomb) {
        int range = gameState.players[bomb.getOwner() - 1].getBombRange();

        gameState.explosions.add(new Explosion(bomb.getPos().getX(), bomb.getPos().getY()));

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int i = 1; i <= range; i++) {
                int newX = bomb.getPos().getX() + dir[0] * i;
                int newY = bomb.getPos().getY() + dir[1] * i;
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
        if (!player.getcanKick() || direction == null) return;

        Position kickDirection = getDirectionFromKey(direction);
        if (kickDirection == null) return;

        Position bombPos = new Position(player.getPos().getX() + kickDirection.getX(), player.getPos().getY() + kickDirection.getY());

        Bomb bombToKick = null;
        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(bombPos)) {
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
        KickingBomb kickingBomb = new KickingBomb(bomb.getPos().getX(), bomb.getPos().getY(), bomb.getOwner(), direction);
        kickingBomb.setKickTimer(bomb.getTimer());

        gameState.bombs.add(kickingBomb);
    }

    private boolean canMoveBombTo(Position pos) {
        if (pos.getX() < 0 || pos.getX() >= GRID_SIZE || pos.getY() < 0 || pos.getY() >= GRID_SIZE) {
            return false;
        }

        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        // Vérifier qu'il n'y a pas déjà une bombe à cette position
        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(pos)) {
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

        for (int i = 0; i < 4; i++) {
            Player player = gameState.players[i];
            if (player.getIsInvulnerable() && currentTime >= player.getInvulnerabilityEndTime()) {
                player.setIsInvulnerable(false);
                player.setBlinking(false); // Arrêter le clignotement
            }
        }
        for (Explosion explosion : gameState.explosions) {
            for (int i = 0; i < 4; i++) {
                Player player = gameState.players[i];

                if (player.getisAlive() &&
                        explosion.pos.equals(player.getPos()) &&
                        !player.getIsInvulnerable()) {

                    // Réduire les vies
                    player.setLives(player.getLives() - 1);

                    // Vérifier si le joueur est mort
                    if (player.getLives() <= 0) {
                        // Le joueur meurt
                        player.setisAlive(false);
                        player.setIsInvulnerable(false);
                        player.setBlinking(false);

                        // Jouer un son de mort si vous en avez un
                        // soundManager.playSound("player_death");

                        System.out.println("Joueur " + (i + 1) + " est mort !");

                        // Vérifier si le jeu est terminé
                        checkGameOver();
                    } else {
                        // Le joueur perd une vie mais survit - le faire réapparaître
                        respawnPlayer(player, i);

                        // Jouer un son de dégât si vous en avez un
                        // soundManager.playSound("player_hurt");

                        System.out.println("Joueur " + (i + 1) + " a perdu une vie. Vies restantes: " + player.getLives());
                    }

                    // Important : sortir de la boucle pour éviter les multiples hits
                    break;
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
        player.setPos(spawnPositions[playerIndex]);
        player.setIsInvulnerable(true);
        player.setInvulnerabilityEndTime(System.currentTimeMillis() + 3000);
        player.setBlinking(true);
    }

    private void checkItemPickup() {
        // Vérifier pour tous les joueurs
        for (int i = 0; i < 4; i++) {
            checkItemPickupForPlayer(gameState.players[i], i);
        }
    }

    private void checkItemPickupForPlayer(Player player, int playerIndex) {
        Item item = gameState.visibleItems.get(player.getPos());
        if (item != null) {
            applyItemEffect(player, item, playerIndex);
            gameState.visibleItems.remove(player.getPos());
        }
    }

    private void applyItemEffect(Player player, Item item, int playerIndex) {
        switch (item.getType()) {
            case FEU:
                player.setBombRange(player.getBombRange()+1);
                player.setFeuBonusCount(player.getFeuBonusCount()+1);
                break;
            case VITESSE:
                player.setSpeedMultiplier(player.getSpeedMultiplier()+0.5);
                player.setVitesseBonusCount(player.getVitesseBonusCount()+1);
                break;
            case BOMBE:
                player.setMaxBombs(player.getMaxBombs()+1);
                player.setBombsRemaining(player.getBombsRemaining()+1);
                player.setBombeBonusCount(player.getBombeBonusCount()+1);
                break;
            case SKULL:
                player.setLives(player.getLives()-1);
                if (player.getLives() <= 0) {
                    player.setisAlive(false);
                    checkGameOver();
                } else {
                    resetPlayerBonuses(player);
                    respawnPlayer(player, playerIndex);
                }
                break;
            case KICK:
                if (player.getKickBonusCount() < 2) {
                    player.setKickBonusCount(player.getKickBonusCount()+1);
                    player.setcanKick(true);
                }
                break;
        }
    }

    private void resetPlayerBonuses(Player player) {
        // Réinitialiser tous les bonus aux valeurs par défaut
        player.setBombRange(2);
        player.setSpeedMultiplier(1.0);
        player.setMaxBombs(3);
        player.setBombsRemaining(3);

        // Réinitialiser les compteurs de bonus
        player.setFeuBonusCount(0);
        player.setKickBonusCount(0);
        player.setBombeBonusCount(0);
        player.setVitesseBonusCount(0);
        player.setcanKick(false);
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
            if (gameState.players[i].getisAlive()) {
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
            if (gameState.players[i] != null && pos.equals(gameState.players[i].getPos())) {
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
                bombsLabels[i].setText("Bombes: " + gameState.players[i].getBombsRemaining());
            }
        }


        if (levelLabel != null) {
            levelLabel.setText("Niveau: " + gameState.level);
        }

        // Affichage des vies
        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.players[0].getLives());
        }

        if (livesLabel2 != null) {
            livesLabel2.setText("x" + gameState.players[1].getLives());
        }
        if (livesLabel3 != null) {
            livesLabel3.setText("x" + gameState.players[2].getLives());
        }
        if (livesLabel4 != null) {
            livesLabel4.setText("x" + gameState.players[3].getLives());
        }

        // Affichage des bonus
        if (bonusLabel != null) {
            String bonusText = String.format("J1 - Feu: %d | Vitesse: %d | Bombe: %d || J2 - Feu: %d | Vitesse: %d | Bombe: %d",
                    gameState.players[0].getFeuBonusCount(),
                    gameState.players[0].getVitesseBonusCount(),
                    gameState.players[0].getBombeBonusCount(),
                    gameState.players[1].getFeuBonusCount(),
                    gameState.players[1].getVitesseBonusCount(),
                    gameState.players[1].getBombeBonusCount());
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
            switch (item.getType()) {
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
                gc.drawImage(itemImage, pos.getX() * CELL_SIZE, pos.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec des couleurs
                switch (item.getType()) {
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
                gc.fillOval(pos.getX() * CELL_SIZE + 8, pos.getY() * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);
            }
        }

        // Murs indestructibles
        gc.setFill(Color.web("#424242"));
        for (Position wall : gameState.walls) {
            gc.fillRect(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            // Effet 3D
            gc.setFill(Color.web("#616161"));
            gc.fillRect(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE, CELL_SIZE, 3);
            gc.fillRect(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE, 3, CELL_SIZE);
            gc.setFill(Color.web("#424242"));
        }

        // Murs destructibles
        for (Position wall : gameState.destructibleWalls) {
            if (brickImage != null) {
                gc.drawImage(brickImage, wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                gc.setFill(Color.web("#8D6E63"));
                gc.fillRect(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                gc.setFill(Color.web("#A1887F"));
                for (int i = 0; i < 3; i++) {
                    gc.fillRect(wall.getX() * CELL_SIZE + 5, wall.getY() * CELL_SIZE + i * 12 + 5, CELL_SIZE - 10, 8);
                }
            }
        }

        // Explosions - Utiliser le GIF d'explosion
        for (Explosion explosion : gameState.explosions) {
            if (fireball != null) {
                // Utiliser l'image GIF d'explosion
                gc.drawImage(fireball, explosion.pos.getX() * CELL_SIZE, explosion.pos.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec animation de couleur
                double intensity = (double) explosion.timer / 30;
                Color explosionColor = Color.web("#FF5722").interpolate(Color.web("#FFEB3B"), 1 - intensity);
                gc.setFill(explosionColor);

                double size = CELL_SIZE * (0.5 + 0.5 * intensity);
                double offset = (CELL_SIZE - size) / 2;
                gc.fillOval(explosion.pos.getX() * CELL_SIZE + offset, explosion.pos.getY() * CELL_SIZE + offset, size, size);
            }
        }


        Color[] highlightColors = {
                Color.web("#64B5F6"), // Bleu clair
                Color.web("#EF5350"), // Rouge clair
                Color.web("#81C784"), // Vert clair
                Color.web("#FFB74D")  // Orange clair
        };

        // Affichage des joueurs
        for (int i = 0; i < 4; i++) {
            Player player = gameState.players[i];
            if (player.getisAlive()) {
                Image playerImage = getPlayerImage(i);

                if (playerImage != null) {
                    // Effet de clignotement si le joueur est invulnérable
                    if (player.getIsInvulnerable()) {
                        long currentTime = System.currentTimeMillis();
                        boolean shouldShow = (currentTime / 200) % 2 == 0; // Clignotement toutes les 200ms

                        if (shouldShow) {
                            // Afficher l'image GIF du joueur normalement
                            gc.drawImage(playerImage,
                                    player.getPos().getX() * CELL_SIZE,
                                    player.getPos().getY() * CELL_SIZE,
                                    CELL_SIZE,
                                    CELL_SIZE);
                        }
                        // Si shouldShow est false, on ne dessine rien (effet de clignotement)
                    } else {
                        // Joueur normal (pas invulnérable), affichage normal
                        gc.drawImage(playerImage,
                                player.getPos().getX() * CELL_SIZE,
                                player.getPos().getY() * CELL_SIZE,
                                CELL_SIZE,
                                CELL_SIZE);
                    }
                }
            }
        }

        // Bombes - Remplacées par l'image de bombe statique
        for (Bomb bomb : gameState.bombs) {
            if (explosionGifImage != null) {
                // Utiliser l'image de bombe statique
                gc.drawImage(explosionGifImage, bomb.getPos().getX() * CELL_SIZE, bomb.getPos().getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec animation clignotante (code original)
                boolean blink = bomb.getTimer() < 60 && (bomb.getTimer() / 10) % 2 == 0;
                gc.setFill(blink ? Color.web("#F44336") : Color.web("#212121"));
                gc.fillOval(bomb.getPos().getX() * CELL_SIZE + 8, bomb.getPos().getY() * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

                // Mèche
                gc.setFill(Color.web("#FF9800"));
                gc.fillRect(bomb.getPos().getX() * CELL_SIZE + CELL_SIZE/2 - 1, bomb.getPos().getY() * CELL_SIZE + 5, 2, 8);
            }
        }

    }
}