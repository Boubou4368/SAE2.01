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

    private long lastMoveTimeP1 = 0;
    private long lastMoveTimeP2 = 0;
    private static final long MOVE_DELAY = 150_000_000; // 150ms en nanosecondes

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
        // Gérer les inputs du joueur 1 (ZQSD)
        handlePlayerInput(gameState.player1, currentTime, lastMoveTimeP1, 1);

        // Gérer les inputs du joueur 2 (Flèches)
        handlePlayerInput(gameState.player2, currentTime, lastMoveTimeP2, 2);
    }

    private void handlePlayerInput(Player player, long currentTime, long lastMoveTime, int playerNumber) {
        // Calculer le délai de mouvement en fonction de la vitesse du joueur
        long currentMoveDelay = (long) (MOVE_DELAY / player.speedMultiplier);

        // Définir les touches selon le joueur
        KeyCode upKey, downKey, leftKey, rightKey, bombKey;
        if (playerNumber == 1) {
            // Joueur 1: ZQSD + Espace
            upKey = KeyCode.Z;
            downKey = KeyCode.S;
            leftKey = KeyCode.Q;
            rightKey = KeyCode.D;
            bombKey = KeyCode.SPACE;
        } else {
            // Joueur 2: Flèches + Entrée
            upKey = KeyCode.UP;
            downKey = KeyCode.DOWN;
            leftKey = KeyCode.LEFT;
            rightKey = KeyCode.RIGHT;
            bombKey = KeyCode.ENTER;
        }

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < currentMoveDelay) {
            // On peut quand même placer des bombes même si on ne peut pas bouger
            if (pressedKeys.contains(bombKey) && player.bombsRemaining > 0) {
                placeBomb(player);
                pressedKeys.remove(bombKey);
            }
            return;
        }

        Position newPos = new Position(player.pos.x, player.pos.y);
        boolean moved = false;

        if (pressedKeys.contains(upKey)) {
            newPos.y--;
            moved = true;
        } else if (pressedKeys.contains(downKey)) {
            newPos.y++;
            moved = true;
        } else if (pressedKeys.contains(leftKey)) {
            newPos.x--;
            moved = true;
        } else if (pressedKeys.contains(rightKey)) {
            newPos.x++;
            moved = true;
        }

        if (moved && canMoveTo(newPos)) {
            player.pos = newPos;
            // Mettre à jour le temps du dernier mouvement
            if (playerNumber == 1) {
                lastMoveTimeP1 = currentTime;
            } else {
                lastMoveTimeP2 = currentTime;
            }
        }

        if (pressedKeys.contains(bombKey) && player.bombsRemaining > 0) {
            placeBomb(player);
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
        for (Explosion explosion : gameState.explosions) {
            // Vérifier joueur 1
            if (explosion.pos.equals(gameState.player1.pos)) {
                gameState.player1.lives--;
                if (gameState.player1.lives <= 0) {
                    gameOver("Joueur 2 gagne !");
                } else {
                    resetPlayerBonuses(gameState.player1);
                    respawnPlayer(gameState.player1, 1);
                }
            }

            // Vérifier joueur 2
            if (explosion.pos.equals(gameState.player2.pos)) {
                gameState.player2.lives--;
                if (gameState.player2.lives <= 0) {
                    gameOver("Joueur 1 gagne !");
                } else {
                    resetPlayerBonuses(gameState.player2);
                    respawnPlayer(gameState.player2, 2);
                }
            }
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
    }

    private void gameOver(String message) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        System.out.println("Game Over: " + message);
        // Ici vous pourriez afficher un écran de game over avec le message
    }

    private void updateUI() {
        // UI pour le joueur 1
        bombsLabel.setText("J1 Bombes: " + gameState.player1.bombsRemaining + "/" + gameState.player1.maxBombs);
        scoreLabel.setText("J1 Score: " + gameState.player1.score + " | J2 Score: " + gameState.player2.score);
        levelLabel.setText("Niveau: " + gameState.level);

        // Afficher le nombre de vies pour chaque joueur
        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.player1.lives);
        }

        if (livesLabel2 != null) {
            livesLabel2.setText("x" + gameState.player2.lives);
        }

        // Afficher les bonus actuels pour les deux joueurs
        if (bonusLabel != null) {
            String bonusText = String.format("J1 - Feu: %d | Vitesse: %d | Bombe: %d || J2 - Feu: %d | Vitesse: %d | Bombe: %d",
                    gameState.player1.feuBonusCount,
                    gameState.player1.vitesseBonusCount,
                    gameState.player1.bombeBonusCount,
                    gameState.player2.feuBonusCount,
                    gameState.player2.vitesseBonusCount,
                    gameState.player2.bombeBonusCount);
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

        // Joueur 1 (Bleu)
        gc.setFill(Color.web("#2196F3"));
        gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 5, gameState.player1.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        gc.setFill(Color.web("#64B5F6"));
        gc.fillOval(gameState.player1.pos.x * CELL_SIZE + 8, gameState.player1.pos.y * CELL_SIZE + 8, 8, 8);

        // Joueur 2 (Rouge)
        gc.setFill(Color.web("#F44336"));
        gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 5, gameState.player2.pos.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        gc.setFill(Color.web("#EF5350"));
        gc.fillOval(gameState.player2.pos.x * CELL_SIZE + 8, gameState.player2.pos.y * CELL_SIZE + 8, 8, 8);
    }

    // Énumération pour les types d'objets
    enum ItemType {
        FEU, VITESSE, BOMBE, SKULL
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
        double speedMultiplier = 1.0;
        int score = 0;

        // Compteurs de bonus
        int feuBonusCount = 0;
        int vitesseBonusCount = 0;
        int bombeBonusCount = 0;

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

            return false;
        }

        private void placeItems(List<Position> availablePositions, Random random) {
            if (availablePositions.size() < 17) {
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

            // Placer 1 skull
            if (index < availablePositions.size()) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.SKULL));
            }
        }
    }
}