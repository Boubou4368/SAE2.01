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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BombermanController2 implements Initializable {
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

    private static final int GRID_SIZE = 15;
    private static final int CELL_SIZE = 40;

    private GraphicsContext gc;
    private GameState gameState;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    // Images
    private Image brickImage;
    private Image feuImage;
    private Image vitesseImage;
    private Image kickImage;
    private Image bombeImage;
    private Image iconeImage;
    private Image skullImage;
    private Image explosionGifImage; // Nouvelle image pour l'explosion
    private Image fireball; // Nouvelle image pour blast

    // Images des drapeaux
    private Image drapeauBlancImage; // J1
    private Image drapeauNoirImage;  // J2
    private Image drapeauBleuImage;  // J3
    private Image drapeauRougeImage; // J4

    private int getPlayerNumber(Player player) {
        if (player == gameState.player1) return 1;
        if (player == gameState.player2) return 2;
        if (player == gameState.player3) return 3;
        if (player == gameState.player4) return 4;
        return 0;
    }

    private Image createColoredFlag(Image baseFlag, Color color) {
        // Méthode pour créer une version colorée du drapeau
        // Tu devras implémenter la logique de recoloration
        return baseFlag; // Placeholder
    }

    // Mode de jeu
    private boolean captureTheFlagMode = true;

    // Images d'animation pour les joueurs
    private Image P1H; // P1 gif haut
    private Image P1B; // P1 gif bas
    private Image P1G; // P1 gif gauche
    private Image P1D; // P1 gif droite

    // Variables pour gérer l'animation des joueurs
    private Image currentP1Image = null;
    private Image currentP2Image = null;

    // Énumération pour les directions
    private enum Direction {
        UP, DOWN, LEFT, RIGHT, IDLE
    }

    // Direction actuelle de chaque joueur
    private Direction player1Direction = Direction.IDLE;
    private Direction player2Direction = Direction.IDLE;

    // Timer pour l'animation (optionnel, pour changer de frame si nécessaire)
    private long lastAnimationUpdate = 0;
    private static final long ANIMATION_DELAY = 200_000_000; // 200ms en nanosecondes

    private long lastMoveTimeP1 = 0;
    private long lastMoveTimeP2 = 0;
    private long lastMoveTimeP3 = 0; // NOUVEAU
    private long lastMoveTimeP4 = 0; // NOUVEAU
    private static final long MOVE_DELAY = 150_000_000; // 150ms en nanosecondes

    public Map<Integer, KeyMapping> keyMappings = chargerTouches("src/main/resources/maquette/sae2_01/Touches.yaml");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = gameCanvas.getGraphicsContext2D();
        gameState = new GameState();

        // Charger les images
        loadImages();

        // Configurer le canvas pour recevoir les événements clavier
        gameCanvas.setFocusTraversable(true);

        startGameLoop();
        updateUI();
    }

    private void loadImages() {
        try {
            brickImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/brique.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image brique.png: " + e.getMessage());
        }

        try {
            kickImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/kick.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image kick.png: " + e.getMessage());
        }

        try {
            feuImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/feu.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image feu.png: " + e.getMessage());
        }

        try {
            vitesseImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/vitesse.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image vitesse.png: " + e.getMessage());
        }

        try {
            bombeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/bombe.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image bombe.png: " + e.getMessage());
        }

        try {
            skullImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/skull.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image skull.png: " + e.getMessage());
        }

        // Charger l'image d'explosion (GIF)
        try {
            explosionGifImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/explosion.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image explosion.gif: " + e.getMessage());
        }

        try {
            fireball = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/blast.png"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image explosion.gif: " + e.getMessage());
        }

        try {
            P1B = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/P1_bas.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1H = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/P1_haut.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1G = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/P1_gauche.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }
        try {
            P1D = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/P1_droite.gif"));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image P1.gif: " + e.getMessage());
        }

        // Charger l'image de base du drapeau
        try {
            Image baseFlag = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/drapeau.png"));
            if (baseFlag == null) {
                System.err.println("Le fichier drapeau.png est introuvable.");}
            // Créer les versions colorées (tu devras implémenter une méthode pour changer les couleurs)
            drapeauBlancImage = createColoredFlag(baseFlag, Color.WHITE);
            drapeauNoirImage = baseFlag; // Garder l'original noir
            drapeauBleuImage = createColoredFlag(baseFlag, Color.BLUE);
            drapeauRougeImage = createColoredFlag(baseFlag, Color.RED);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des drapeaux: " + e.getMessage());
        }

        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/icone.png"));
            if (livesIcon != null) {
                livesIcon.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone.png: " + e.getMessage());
        }
        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/icone2.png"));
            if (livesIcon2 != null) {
                livesIcon2.setImage(iconeImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image icone2.png: " + e.getMessage());
        }
        try {
            iconeImage = new Image(getClass().getResourceAsStream("/maquette/sae2_01/Images/titre.png"));
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
        checkFlagCapture(); // Nouvelle méthode
    }

    private void handleInput(long currentTime) {
        // Gérer les inputs du joueur 1 (ZQSD)
        handlePlayerInput(gameState.player1, currentTime, lastMoveTimeP1, 1);

        // Gérer les inputs du joueur 2 (Flèches)
        handlePlayerInput(gameState.player2, currentTime, lastMoveTimeP2, 2);

        // Gérer les inputs du joueur 3 (TFGH)
        handlePlayerInput(gameState.player3, currentTime, lastMoveTimeP3, 3);

        // Gérer les inputs du joueur 4 (IJKL)
        handlePlayerInput(gameState.player4, currentTime, lastMoveTimeP4, 4);
    }

    private void handlePlayerInput(Player player, long currentTime, long lastMoveTime, int playerNumber) {
        // Permettre aux joueurs éliminés de seulement poser des bombes
        if (player.isEliminated) {
            // Seule la gestion des bombes pour les joueurs éliminés
            // ... code bombe uniquement ...
            return;
        }
        // Calculer le délai de mouvement en fonction de la vitesse du joueur
        long currentMoveDelay = (long) (MOVE_DELAY / player.speedMultiplier);

        // Définir les touches selon le joueur
        KeyCode upKey, downKey, leftKey, rightKey, bombKey;
        switch (playerNumber) {
            case 1: // Joueur 1: ZQSD + Espace
                upKey = KeyCode.Z;
                downKey = KeyCode.S;
                leftKey = KeyCode.Q;
                rightKey = KeyCode.D;
                bombKey = KeyCode.SPACE;
                break;
            case 2: // Joueur 2: Flèches + Entrée
                upKey = KeyCode.UP;
                downKey = KeyCode.DOWN;
                leftKey = KeyCode.LEFT;
                rightKey = KeyCode.RIGHT;
                bombKey = KeyCode.ENTER;
                break;
            case 3: // Joueur 3: TFGH + R
                upKey = KeyCode.T;
                downKey = KeyCode.G;
                leftKey = KeyCode.F;
                rightKey = KeyCode.H;
                bombKey = KeyCode.R;
                break;
            case 4: // Joueur 4: IJKL + U
                upKey = KeyCode.I;
                downKey = KeyCode.K;
                leftKey = KeyCode.J;
                rightKey = KeyCode.L;
                bombKey = KeyCode.U;
                break;
            default:
                return;
        }

        // GESTION DES BOMBES SÉPARÉE - sans délai de mouvement
        if (pressedKeys.contains(bombKey) && player.bombsRemaining > 0) {
            // Vérifier qu'il n'y a pas déjà une bombe à la position du joueur
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
            // Retirer la touche pour éviter le spam de bombes
            pressedKeys.remove(bombKey);
        }

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < currentMoveDelay) {
            return; // Ne pas bouger mais on a déjà géré les bombes ci-dessus
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

        // Mettre à jour la direction et l'image du joueur
        if (playerNumber == 1) {
            player1Direction = newDirection;
            updatePlayerImage(1, newDirection);
        } else {
            player2Direction = newDirection;
            updatePlayerImage(2, newDirection);
        }

        if (moved && canMoveTo(newPos)) {
            player.pos = newPos;
            switch (playerNumber) {
                case 1: lastMoveTimeP1 = currentTime; break;
                case 2: lastMoveTimeP2 = currentTime; break;
                case 3: lastMoveTimeP3 = currentTime; break;
                case 4: lastMoveTimeP4 = currentTime; break;
            }
        }

        // Gestion du kick
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

    private void checkFlagCapture() {
        Player[] players = {gameState.player1, gameState.player2, gameState.player3, gameState.player4};

        for (Player player : players) {
            if (player.isEliminated) continue;

            // Vérifier si le joueur touche un drapeau ennemi
            for (Player otherPlayer : players) {
                if (otherPlayer != player && otherPlayer.hasFlag &&
                        player.pos.equals(otherPlayer.flagPosition)) {

                    // Capturer le drapeau
                    otherPlayer.hasFlag = false;
                    otherPlayer.isEliminated = true;
                    player.flagsCaptured++;
                    player.score += 500;

                    // Vérifier victoire
                    if (player.flagsCaptured >= 3) { // Capturer les 3 autres drapeaux
                        gameOver("Joueur " + getPlayerNumber(player) + " gagne en capturant tous les drapeaux !");
                        return;
                    }
                }
            }
        }
    }

    private void updatePlayerImage(int playerNumber, Direction direction) {
        if (playerNumber == 1) {
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
        } else if (playerNumber == 2) {
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
        }
    }

    static class KickingBomb extends Bomb {
        Position direction;
        int kickSpeed = 8; // Plus le nombre est grand, plus c'est lent
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

        // Vérifier que les joueurs vivants ne se chevauchent pas
        Player[] players = {gameState.player1, gameState.player2, gameState.player3, gameState.player4};
        for (Player player : players) {
            if (!player.isEliminated && pos.equals(player.pos)) {
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

                // Vérifier si c'est un mur destructible
                if (gameState.destructibleWalls.contains(explPos)) {
                    gameState.destructibleWalls.remove(explPos);
                    bomb.owner.score += 10;

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
        if (gameState.player1.isInvulnerable && currentTime >= gameState.player1.invulnerabilityEndTime) {
            gameState.player1.isInvulnerable = false;
        }
        if (gameState.player2.isInvulnerable && currentTime >= gameState.player2.invulnerabilityEndTime) {
            gameState.player2.isInvulnerable = false;
        }

        for (Explosion explosion : gameState.explosions) {
// Vérifier les joueurs 3 et 4 aussi
            Player[] allPlayers = {gameState.player1, gameState.player2, gameState.player3, gameState.player4};

            for (Player player : allPlayers) {
                if (explosion.pos.equals(player.pos) && !player.isInvulnerable && !player.isEliminated) {
                    if (player.hasFlag) {
                        // Perdre le drapeau et être éliminé
                        player.hasFlag = false;
                        player.isEliminated = true;
                    }
                    // Pas de perte de vie, juste élimination
                }
            }
        }
    }

    private void resetPlayerBonuses(Player player) {
        // Réinitialiser tous les bonus aux valeurs par défaut
        player.bombRange = 1;
        player.speedMultiplier = 1.0;
        player.maxBombs = 3;
        player.bombsRemaining = 3;

        // Réinitialiser les compteurs de bonus
        player.feuBonusCount = 0;
        player.vitesseBonusCount = 0;
        player.bombeBonusCount = 0;
        player.kickBonusCount = 0; // NOUVEAU
        player.canKick = false; // NOUVEAU
    }

    private void checkItemPickup() {
        // Vérifier pour le joueur 1
        checkItemPickupForPlayer(gameState.player1);

        // Vérifier pour le joueur 2
        checkItemPickupForPlayer(gameState.player2);
    }

    private void checkItemPickupForPlayer(Player player) {
        Item item = gameState.visibleItems.get(player.pos);
        if (item != null) {
            // Appliquer l'effet de l'objet
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
                    // Le skull fait perdre une vie au joueur
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
                    if (player.kickBonusCount < 2) { // Maximum 2 kick items
                        player.kickBonusCount++;
                        player.canKick = true;
                        player.score += 60;
                    }
                    break;
            }

            // Retirer l'objet de la map
            gameState.visibleItems.remove(player.pos);
        }
    }

    private void respawnPlayer(Player player, int playerNumber) {
        if (playerNumber == 1) {
            player.pos = new Position(1, 1);
        } else {
            player.pos = new Position(GRID_SIZE - 2, GRID_SIZE - 2);
        }

        // Activer l'invincibilité pour 3 secondes
        player.isInvulnerable = true;
        player.invulnerabilityEndTime = System.currentTimeMillis() + 3000; // 3 secondes
    }

    private void gameOver(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Game Over: " + message);
        // Ici vous pourriez afficher un écran de game over avec le message
    }

    private void updateUI() {
        if (bonusLabel != null) {
            bonusLabel.setText("Some text");
        } else {
            System.err.println("bonusLabel est null");
        }
        // UI pour le joueur 1
        bombsLabel.setText("J1 Bombes: " + gameState.player1.bombsRemaining + "/" + gameState.player1.maxBombs);
        scoreLabel.setText("J1 Score: " + gameState.player1.score + " | J2 Score: " + gameState.player2.score);
        levelLabel.setText("Niveau: " + gameState.level);
        // Afficher les infos des 4 joueurs
        scoreLabel.setText(String.format("J1:%d J2:%d J3:%d J4:%d",
                gameState.player1.score, gameState.player2.score,
                gameState.player3.score, gameState.player4.score));

        // Afficher le statut des drapeaux
        String flagStatus = String.format("Drapeaux - J1:%s J2:%s J3:%s J4:%s",
                gameState.player1.hasFlag ? "✓" : "✗",
                gameState.player2.hasFlag ? "✓" : "✗",
                gameState.player3.hasFlag ? "✓" : "✗",
                gameState.player4.hasFlag ? "✓" : "✗");
        bonusLabel.setText(flagStatus);

        // Afficher le nombre de vies pour chaque joueur
        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.player1.lives);
        }

        if (livesLabel2 != null) {
            livesLabel2.setText("x" + gameState.player2.lives);
        }

        // Afficher les bonus actuels pour les deux joueurs
        if (bonusLabel != null) {
            String bonusText = String.format("J1 - Feu: %d | Vitesse: %d | Bombe: %d | Kick: %d || J2 - Feu: %d | Vitesse: %d | Bombe: %d | Kick: %d",
                    gameState.player1.feuBonusCount,
                    gameState.player1.vitesseBonusCount,
                    gameState.player1.bombeBonusCount,
                    gameState.player1.kickBonusCount, // NOUVEAU
                    gameState.player2.feuBonusCount,
                    gameState.player2.vitesseBonusCount,
                    gameState.player2.bombeBonusCount,
                    gameState.player2.kickBonusCount); // NOUVEAU
            bonusLabel.setText(bonusText);
        }
    }

    private void renderAllPlayers() {
        // Joueur 1 - Bleu (avec animations existantes)
        renderPlayer(gameState.player1, 1, Color.web("#2196F3"), Color.web("#64B5F6"));

        // Joueur 2 - Rouge
        renderPlayer(gameState.player2, 2, Color.web("#F44336"), Color.web("#EF5350"));

        // Joueur 3 - Vert
        renderPlayer(gameState.player3, 3, Color.web("#4CAF50"), Color.web("#66BB6A"));

        // Joueur 4 - Jaune
        renderPlayer(gameState.player4, 4, Color.web("#FF9800"), Color.web("#FFB74D"));
    }

    private void renderPlayer(Player player, int playerNumber, Color mainColor, Color accentColor) {
        if (player == null) return;

        // Effet de clignotement si invulnérable
        boolean shouldDraw = true;
        if (player.isInvulnerable) {
            shouldDraw = (System.currentTimeMillis() / 200) % 2 == 0;
        }

        // Effet de transparence si éliminé
        double opacity = player.isEliminated ? 0.5 : 1.0;

        if (shouldDraw) {
            if (playerNumber == 1 && currentP1Image != null) {
                // Utiliser l'animation pour le joueur 1
                gc.setGlobalAlpha(opacity);
                gc.drawImage(currentP1Image,
                        player.pos.x * CELL_SIZE,
                        player.pos.y * CELL_SIZE,
                        CELL_SIZE,
                        CELL_SIZE);
                gc.setGlobalAlpha(1.0);
            } else {
                // Rendu par défaut pour les autres joueurs
                gc.setGlobalAlpha(opacity);
                gc.setFill(mainColor);
                gc.fillOval(player.pos.x * CELL_SIZE + 5,
                        player.pos.y * CELL_SIZE + 5,
                        CELL_SIZE - 10,
                        CELL_SIZE - 10);
                gc.setFill(accentColor);
                gc.fillOval(player.pos.x * CELL_SIZE + 8,
                        player.pos.y * CELL_SIZE + 8, 8, 8);
                gc.setGlobalAlpha(1.0);
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
                        gc.setFill(Color.web("#FFD700")); // Couleur dorée
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

        // Dessiner les drapeaux
        renderFlags();

        // Modifier le rendu des joueurs pour inclure les 4 joueurs
        renderAllPlayers();
    }

    private void renderFlags() {
        Player[] players = {gameState.player1, gameState.player2, gameState.player3, gameState.player4};
        Image[] flagImages = {drapeauBlancImage, drapeauNoirImage, drapeauBleuImage, drapeauRougeImage};

        for (int i = 0; i < players.length; i++) {
            if (players[i].hasFlag && flagImages[i] != null) {
                gc.drawImage(flagImages[i],
                        players[i].flagPosition.x * CELL_SIZE,
                        players[i].flagPosition.y * CELL_SIZE,
                        CELL_SIZE, CELL_SIZE);
            }
        }
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
        int lives = 1;
        double speedMultiplier = 1.0;
        int score = 0;

        // Compteurs de bonus
        int feuBonusCount = 0;
        int vitesseBonusCount = 0;
        int bombeBonusCount = 0;
        int kickBonusCount = 0; // NOUVEAU
        boolean canKick = false; // NOUVEAU

        // Nouvelles variables pour l'invincibilité
        boolean isInvulnerable = false;
        long invulnerabilityEndTime = 0;

        // Nouvelles variables pour le mode CTF
        boolean hasFlag = true;        // Le joueur a-t-il encore son drapeau ?
        boolean isEliminated = false;  // Le joueur est-il éliminé ?
        int flagsCaptured = 0;         // Nombre de drapeaux capturés
        Position flagPosition;         // Position du drapeau du joueur

        Player(int x, int y) {
            this.pos = new Position(x, y);
            this.flagPosition = new Position(x, y); // Drapeau commence à la position du joueur
        }
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
        Player player3;  // Nouveau joueur 3
        Player player4;  // Nouveau joueur 4
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
            player3 = new Player(1, GRID_SIZE - 2);        // Coin bas-gauche
            player4 = new Player(GRID_SIZE - 2, 1);        // Coin haut-droite
            initializeMap();
        }

        private void initializeMap() {
            // Créer les murs fixes
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (i == 0 || i == GRID_SIZE - 1 || j == 0 || j == GRID_SIZE - 1) {
                        walls.add(new Position(i, j));
                    } else if (i % 2 == 0 && j % 2 == 0) {
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
                    if (!walls.contains(pos) &&
                            !isStartingArea(pos) &&
                            random.nextDouble() < 0.3) {
                        destructibleWalls.add(pos);
                        availablePositions.add(pos);
                    }
                }
            }

            // Placer les objets aléatoirement dans les murs destructibles
            placeItems(availablePositions, random);
        }

        private boolean isStartingArea(Position pos) {
            // Zone de départ joueur 1 (coin haut-gauche)
            if (pos.x <= 2 && pos.y <= 2) return true;

            // Zone de départ joueur 2 (coin bas-droite)
            if (pos.x >= GRID_SIZE - 3 && pos.y >= GRID_SIZE - 3) return true;

            // Zone de départ joueur 3 (coin bas-gauche)
            if (pos.x <= 2 && pos.y >= GRID_SIZE - 3) return true;

            // Zone de départ joueur 4 (coin haut-droite)
            if (pos.x >= GRID_SIZE - 3 && pos.y <= 2) return true;

            return false;
        }

        private void placeItems(List<Position> availablePositions, Random random) {
            if (availablePositions.size() < 19) { // Augmenté de 17 à 19
                System.err.println("Pas assez de murs destructibles pour placer tous les objets");
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
    }

    private Map<Integer, KeyMapping> chargerTouches(String cheminFichier) {
        Map<Integer, KeyMapping> keyMappings = new HashMap<>();
        int joueurActuel = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;

                if (ligne.startsWith("J")) {
                    try {
                        joueurActuel = Integer.parseInt(ligne.substring(1, 2)) - 1;
                        keyMappings.put(joueurActuel, new KeyMapping());
                    } catch (NumberFormatException e) {
                        System.err.println("Ligne joueur invalide : " + ligne);
                    }
                } else if (joueurActuel != -1 && ligne.contains(":")) {
                    String[] parts = ligne.split(":", 2);
                    if (parts.length < 2) continue;

                    String direction = parts[0].trim();
                    String toucheTexte = parts[1].trim();

                    if (toucheTexte.isEmpty()) continue;

                    String toucheKeyCode;
                    if (toucheTexte.equalsIgnoreCase("Caps Lock")) {
                        toucheKeyCode = "CAPS"; // Correction spécifique
                    } else {
                        toucheKeyCode = toucheTexte.toUpperCase().replace(" ", "_");
                    }


                    try {
                        KeyCode key = KeyCode.valueOf(toucheKeyCode);
                        KeyMapping mapping = keyMappings.get(joueurActuel);
                        switch (direction) {
                            case "U" -> mapping.up = key;
                            case "D" -> mapping.down = key;
                            case "L" -> mapping.left = key;
                            case "R" -> mapping.right = key;
                            case "B" -> mapping.bomb = key;
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Touche invalide ignorée : '" + toucheTexte + "'");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier de touches : " + e.getMessage());
        }

        return keyMappings;
    }
}