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
    @FXML private Canvas gameCanvas;
    @FXML private Label player1BombsLabel;
    @FXML private Label player2BombsLabel;
    @FXML private Label player3BombsLabel;
    @FXML private Label player4BombsLabel;
    @FXML private Label levelLabel;
    @FXML private Label livesLabel;
    @FXML private Label livesLabel2;
    @FXML private Label livesLabel3;
    @FXML private Label livesLabel4;
    @FXML private ImageView livesIcon;
    @FXML private ImageView livesIcon2;
    @FXML private ImageView livesIcon3;
    @FXML private ImageView livesIcon4;
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
    private static final long MOVE_DELAY_NS = 150_000_000; // 150ms en nanosecondes


    // Contrôle de la vitesse de déplacement pour chaque joueur
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
    private Image P1H; // P1 gif haut
    private Image P1B; // P1 gif bas
    private Image P1G; // P1 gif gauche
    private Image P1D; // P1 gif droite

    // Variables pour gérer l'animation des joueurs
    private Image currentP1Image = null;
    private Image currentP2Image = null;
    private Image currentP3Image = null;
    private Image currentP4Image = null;

    // Énumération pour les directions
    private enum Direction {
        UP , DOWN , LEFT, RIGHT ,IDLE ;
    }


    private long lastAnimationUpdate = 0;
    private static final long ANIMATION_DELAY = 200_000_000;

    private long lastMoveTimeP1 = 0;
    private long lastMoveTimeP2 = 0;
    private static final long MOVE_DELAY = 150_000_000;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();

        // Charger les images
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

        // Charger l'image d'explosion (GIF)
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
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/icone3.png"));
            if (livesIcon3 != null) {
                livesIcon3.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone3.png: " + e.getMessage());
        }
        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/icone4.png"));
            if (livesIcon4 != null) {
                livesIcon4.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone4.png: " + e.getMessage());
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
            updatePlayerImage(playerIndex,newDirection);
            lastMoveTimes[playerIndex] = currentTime;
           // soundManager.playSound("walk");

            // Vérifier le kick de bombe après le mouvement réussi
            if (player.canKick && currentDirectionKey != null) {
                tryKickBomb(player, currentDirectionKey);
            }
        } else if (moved && player.canKick && currentDirectionKey != null) {
            // Si le mouvement est bloqué mais que le joueur peut kicker,
            // essayer de kicker une bombe dans cette direction
            tryKickBomb(player, currentDirectionKey);
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

    private void updatePlayerImage(int playerNumber, Direction direction) {
        if (playerNumber == 0) {
            // Changer l'image du joueur 1 selon la direction
            switch (direction) {
                case UP:
                    currentP1Image = P1H; // Image haut
                    break;
                case DOWN:
                    currentP1Image = P1B; // Image bas
                    break;
                case LEFT:
                    currentP1Image = P1G; // Image gauche
                    break;
                case RIGHT:
                    currentP1Image = P1D; // Image droite
                    break;
                case IDLE:
                    // Garder la dernière direction ou utiliser l'image par défaut (bas)
                    if (currentP1Image == null) {
                        currentP1Image = P1B;
                    }
                    break;
            }
        } else if (playerNumber == 1) {
            // Pour l'instant, le joueur 2 garde le rendu par défaut
            // Tu pourras implémenter ses images plus tard si tu veux
            // currentP2Image = ...;
        }
    }

    private void tryKickBomb(Player player, KeyCode direction) {
        if (!player.canKick) return;

        Position kickDirection = getDirectionFromKey(direction);
        if (kickDirection == null) return;

        Position bombPos = new Position(player.pos.x + kickDirection.x, player.pos.y + kickDirection.y);

        // Trouver la bombe à cette position
        Bomb bombToKick = null;
        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                // Ne pas kicker une bombe qui bouge déjà
                if (!(bomb instanceof KickingBomb)) {
                    bombToKick = bomb;
                }
                break;
            }
        }

        if (bombToKick != null) {
            kickBomb(bombToKick, kickDirection);
//soundManager.playSound("kick"); // Jouer le son de kick
        }
    }

    static class KickingBomb extends Bomb {
        Position direction;
        int kickSpeed = 8; // Plus le nombre est grand, plus c'est lent
        int kickTimer = 0;

        KickingBomb(int x, int y, int owner, Position direction) {
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

    private void placeBomb(Player player, int playerNumber) {
        Position bombPos = new Position(player.pos.x, player.pos.y);

        for (Bomb bomb : gameState.bombs) {
            if (bomb.pos.equals(bombPos)) {
                return;
            }
        }


        gameState.bombs.add(new Bomb(bombPos.x, bombPos.y, playerNumber));
        player.bombsRemaining--;
        //soundManager.playSound("bomb_place"); // Jouer le son de placement de bombe
    }

    private void updateBombs() {
        Iterator<Bomb> bombIterator = gameState.bombs.iterator();

        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.timer--;

            // Gérer les bombes qui bougent
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
                        // On continue avec la boucle suivante pour éviter les problèmes
                        continue;
                    }
                }
            }

            if (bomb.timer <= 0) {
                explodeBomb(bomb);
                bombIterator.remove();
                int originalOwner = bomb.owner;

                // Rendre la bombe au bon joueur
                if (originalOwner >= 1 && originalOwner <= 4) {
                    gameState.players[originalOwner - 1].bombsRemaining++;
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

    private void checkItemPickup() {
        // Vérifier pour tous les joueurs
        for (int i = 0; i < 4; i++) {
            checkItemPickupForPlayer(gameState.players[i], i);
        }
    }



    private void checkItemPickupForPlayer(Player player , int playerIndex) {
        Item item = gameState.visibleItems.get(player.pos);
        if (item != null) {
            // Appliquer l'effet de l'objet
            switch (item.type) {
                case FEU:
                    player.bombRange++;
                    player.feuBonusCount++;
                    player.score += 50;
                    //soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case VITESSE:
                    player.speedMultiplier += 0.3;
                    player.vitesseBonusCount++;
                    player.score += 30;
                    //soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case BOMBE:
                    player.maxBombs++;
                    player.bombsRemaining++;
                    player.bombeBonusCount++;
                    player.score += 40;
                    //soundManager.playSound("pickup"); // Jouer le son de ramassage
                    break;
                case SKULL:
                    // Le skull fait perdre une vie au joueur
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
                    if (player.kickBonusCount < 2) { // Maximum 2 kick items
                        player.kickBonusCount++;
                        player.canKick = true;
                        player.score += 60;
                       // soundManager.playSound("pickup"); // Jouer le son de ramassage
                    }
                    break;
            }

            // Retirer l'objet de la map
            gameState.visibleItems.remove(player.pos);
        }
    }

    private boolean isPlayerAt(Position pos) {
        for (int i = 0; i < gameState.players.length; i++) {
            if (gameState.players[i] != null && pos.equals(gameState.players[i].pos)) {
                return true;
            }
        }
                return false;
    }

    private void gameOver(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (message.contains("gagne")) {
            //soundManager.playSound("win"); // Jouer le son de victoire
        } else {
            //soundManager.playSound("lose"); // Jouer le son de défaite
        }
        soundManager.stopBackgroundMusic(); // Arrêter la musique de fond
        System.out.println("Game Over: " + message);
        // Ici vous pourriez afficher un écran de game over avec le message
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
                case KICK:
                    itemImage = kickImage;
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
                    case KICK:
                        gc.setFill(Color.web("#FFD700"));
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

        // Bombes
        for (Bomb bomb : gameState.bombs) {
            if (explosionGifImage != null) {
                gc.drawImage(explosionGifImage, bomb.pos.x * CELL_SIZE, bomb.pos.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                // Ajouter un effet pour les bombes qui bougent
                if (bomb instanceof KickingBomb) {
                    gc.setFill(Color.web("#FFD700", 0.3)); // Halo doré semi-transparent
                    gc.fillOval(bomb.pos.x * CELL_SIZE + 2, bomb.pos.y * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                }
            } else {
                // Fallback
                boolean blink = bomb.timer < 60 && (bomb.timer / 10) % 2 == 0;
                Color bombColor = blink ? Color.web("#F44336") : Color.web("#212121");

                // Couleur différente pour les bombes qui bougent
                if (bomb instanceof KickingBomb) {
                    bombColor = blink ? Color.web("#FF9800") : Color.web("#424242");
                }

                gc.setFill(bombColor);
                gc.fillOval(bomb.pos.x * CELL_SIZE + 8, bomb.pos.y * CELL_SIZE + 8, CELL_SIZE - 16, CELL_SIZE - 16);

                // Mèche
                gc.setFill(Color.web("#FF9800"));
                gc.fillRect(bomb.pos.x * CELL_SIZE + CELL_SIZE/2 - 1, bomb.pos.y * CELL_SIZE + 5, 2, 8);
            }
        }

        // Rendu des joueurs avec effet d'invulnérabilité
        for (int i = 0; i < gameState.players.length; i++) {
            Player player = gameState.players[i];

            // Déterminer si le joueur doit être affiché (effet de clignotement si invulnérable)
            boolean shouldDrawPlayer = true;
            if (player.isInvulnerable) {
                // Faire clignoter le joueur (visible/invisible toutes les 200ms)
                shouldDrawPlayer = (System.currentTimeMillis() / 200) % 2 == 0;
            }

            if (shouldDrawPlayer) {
                // Utiliser l'image du joueur si disponible
                Image playerImage = getPlayerImage(i); // Méthode pour obtenir l'image selon l'index

                if (playerImage != null) {
                    gc.drawImage(playerImage,
                            player.pos.x * CELL_SIZE,
                            player.pos.y * CELL_SIZE,
                            CELL_SIZE,
                            CELL_SIZE);
                }
            }
        }
    }

    private Image getPlayerImage(int playerIndex) {
        Image playerImage = null;
        switch (playerIndex) {
            case 0: playerImage = currentP1Image; break;
            case 1: playerImage = currentP2Image; break;
            case 2: playerImage = currentP3Image; break;
            case 3: playerImage = currentP4Image; break;
        }
        return playerImage;
    }
    // Énumération pour les types d'objets
    enum ItemType {
        FEU, VITESSE, BOMBE, SKULL, KICK
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
        int maxBombs = 3;
        int bombRange = 2;
        int lives = 3;

        boolean isAlive = true;

        double speedMultiplier = 1.0;
        int score = 0;

        // Compteurs de bonus
        int feuBonusCount = 0;
        int vitesseBonusCount = 0;
        int bombeBonusCount = 0;
        int kickBonusCount = 0;
        boolean canKick = false;

        // Nouvelles variables pour l'invincibilité
        boolean isInvulnerable = false;
        long invulnerabilityEndTime = 0;


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

    static class Item {
        ItemType type;
        Item(ItemType type) { this.type = type; }
    }

    static class GameState {
        Player[] players = new Player[4];
        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();
        Map<Position, Item> hiddenItems = new HashMap<>();
        Map<Position, Item> visibleItems = new HashMap<>();
        int level = 1;

        GameState() {
            // Positions de départ aux 4 coins
            players[0] = new Player(1, 1);                          // Joueur 1
            players[1] = new Player(GRID_SIZE - 2, GRID_SIZE - 2);   // Joueur 2
            players[2] = new Player(1, GRID_SIZE - 2);               // Joueur 3
            players[3] = new Player(GRID_SIZE - 2, 1);               // Joueur 4

            initializeMap();
        }

        // Méthodes d'accès pour compatibilité
        public Player getPlayer1() { return players[0]; }
        public Player getPlayer2() { return players[1]; }
        public Player getPlayer3() { return players[2]; }
        public Player getPlayer4() { return players[3]; }

        // DÉPLACER CES MÉTHODES DANS GameState
        private void initializeMap() {
            // Créer les murs fixes (bordures et colonnes/lignes paires)
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    // Murs de bordure
                    if (i == 0 || i == GRID_SIZE - 1 || j == 0 || j == GRID_SIZE - 1) {
                        walls.add(new Position(i, j));
                    }
                    // Murs internes (grille)
                    else if (i % 2 == 0 && j % 2 == 0) {
                        walls.add(new Position(i, j));
                    }
                }
            }

            // Créer les murs destructibles
            List<Position> availablePositions = new ArrayList<>();
            Random random = new Random();

            for (int i = 1; i < GRID_SIZE - 1; i++) {
                for (int j = 1; j < GRID_SIZE - 1; j++) {
                    Position pos = new Position(i, j);

                    // Vérifier que la position n'est pas un mur fixe et n'est pas dans une zone de départ
                    if (!walls.contains(pos) && !isStartingArea(pos) && random.nextDouble() < 0.3) {
                        destructibleWalls.add(pos);
                        availablePositions.add(pos);
                    }
                }
            }

            // Placer les objets aléatoirement dans les murs destructibles
            placeItems(availablePositions, random);
        }

        private boolean isStartingArea(Position pos) {
            // Zone de départ joueur 1 (coin haut-gauche) - 3x3
            if (pos.x <= 2 && pos.y <= 2) return true;

            // Zone de départ joueur 2 (coin bas-droite) - 3x3
            if (pos.x >= GRID_SIZE - 3 && pos.y >= GRID_SIZE - 3) return true;

            // Zone de départ joueur 3 (coin bas-gauche) - 3x3
            if (pos.x <= 2 && pos.y >= GRID_SIZE - 3) return true;

            // Zone de départ joueur 4 (coin haut-droite) - 3x3
            if (pos.x >= GRID_SIZE - 3 && pos.y <= 2) return true;

            return false;
        }

        private void placeItems(List<Position> availablePositions, Random random) {
            if (availablePositions.size() < 19) {
                System.err.println("Pas assez de murs destructibles pour placer tous les objets");
                System.err.println("Positions disponibles: " + availablePositions.size() + ", requis: 17");
                // Réduire le nombre d'objets si nécessaire
                placeReducedItems(availablePositions, random);
                return;
            }

            Collections.shuffle(availablePositions, random);

            int index = 0;

            // Placer 4 objets feu
            for (int i = 0; i < 4 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.FEU));
            }

            // Placer 4 objets vitesse
            for (int i = 0; i < 4 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.VITESSE));
            }

            // Placer 8 objets bombe
            for (int i = 0; i < 8 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.BOMBE));
            }

            // Placer 2 objets kick - NOUVEAU
            for (int i = 0; i < 2 && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.KICK));
            }

            // Placer 1 skull
            if (index < availablePositions.size()) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.SKULL));
            }
        }

        private void placeReducedItems(List<Position> availablePositions, Random random) {
            Collections.shuffle(availablePositions, random);
            int index = 0;
            int itemsPerType = Math.max(1, availablePositions.size() / 4);

            // Placer des objets proportionnellement au nombre de positions disponibles
            for (int i = 0; i < itemsPerType && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.FEU));
            }
            for (int i = 0; i < itemsPerType && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.VITESSE));
            }
            for (int i = 0; i < itemsPerType && index < availablePositions.size(); i++, index++) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.BOMBE));
            }
            // Placer un skull si il reste de la place
            if (index < availablePositions.size()) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.SKULL));
            }
        }

        // Méthode utilitaire pour déboguer
        public void printMapInfo() {
            System.out.println("=== INFO MAP ===");
            System.out.println("Murs fixes: " + walls.size());
            System.out.println("Murs destructibles: " + destructibleWalls.size());
            System.out.println("Objets cachés: " + hiddenItems.size());
            System.out.println("Objets visibles: " + visibleItems.size());

            // Compter les types d'objets
            Map<ItemType, Integer> itemCount = new HashMap<>();
            for (Item item : hiddenItems.values()) {
                itemCount.put(item.type, itemCount.getOrDefault(item.type, 0) + 1);
            }
            System.out.println("Répartition des objets:");
            for (Map.Entry<ItemType, Integer> entry : itemCount.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("================");
        }
    }
}