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
    private Image bombeImage;
    private Image iconeImage;
    private Image skullImage; // Nouvelle image pour le skull

    private long lastMoveTime = 0;
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
            System.err.println("Erreur lors du chargement de l'image icone.png: " + e.getMessage());
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
        checkPlayerHit();
        checkItemPickup();
    }

    private void handleInput(long currentTime) {
        // Calculer le délai de mouvement en fonction de la vitesse du joueur
        long currentMoveDelay = (long) (MOVE_DELAY / gameState.player.speedMultiplier);

        // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
        if (currentTime - lastMoveTime < currentMoveDelay) {
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

                // Vérifier si c'est un mur destructible
                if (gameState.destructibleWalls.contains(explPos)) {
                    gameState.destructibleWalls.remove(explPos);
                    gameState.score += 10;

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

    private void checkPlayerHit() {
        for (Explosion explosion : gameState.explosions) {
            if (explosion.pos.equals(gameState.player.pos)) {
                gameState.player.lives--;
                if (gameState.player.lives <= 0) {
                    gameOver();
                } else {
                    resetPlayerBonuses(); // Réinitialiser les bonus quand le joueur meurt
                    respawnPlayer();
                }
                break;
            }
        }
    }

    private void resetPlayerBonuses() {
        // Réinitialiser tous les bonus aux valeurs par défaut
        gameState.player.bombRange = 2;
        gameState.player.speedMultiplier = 1.0;
        gameState.player.maxBombs = 3;
        gameState.player.bombsRemaining = 3;

        // Réinitialiser les compteurs de bonus
        gameState.player.feuBonusCount = 0;
        gameState.player.vitesseBonusCount = 0;
        gameState.player.bombeBonusCount = 0;
    }

    private void checkItemPickup() {
        Item item = gameState.visibleItems.get(gameState.player.pos);
        if (item != null) {
            // Appliquer l'effet de l'objet
            switch (item.type) {
                case FEU:
                    gameState.player.bombRange++;
                    gameState.player.feuBonusCount++;
                    gameState.score += 50;
                    break;
                case VITESSE:
                    gameState.player.speedMultiplier += 0.3;
                    gameState.player.vitesseBonusCount++;
                    gameState.score += 30;
                    break;
                case BOMBE:
                    gameState.player.maxBombs++;
                    gameState.player.bombsRemaining++;
                    gameState.player.bombeBonusCount++;
                    gameState.score += 40;
                    break;
                case SKULL:
                    // Le skull fait perdre une vie au joueur
                    gameState.player.lives--;
                    if (gameState.player.lives <= 0) {
                        gameOver();
                    } else {
                        resetPlayerBonuses(); // Réinitialiser les bonus quand le joueur meurt
                        respawnPlayer();
                    }
                    break;
            }

            // Retirer l'objet de la map
            gameState.visibleItems.remove(gameState.player.pos);
        }
    }

    private void respawnPlayer() {
        gameState.player.pos = new Position(1, 1);
    }

    private void gameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        // Ici vous pourriez afficher un écran de game over
    }

    private void updateUI() {
        bombsLabel.setText("Bombes: " + gameState.player.bombsRemaining + "/" + gameState.player.maxBombs);
        scoreLabel.setText("Score: " + gameState.score);
        levelLabel.setText("Niveau: " + gameState.level);

        // Afficher le nombre de vies avec l'icône
        if (livesLabel != null) {
            livesLabel.setText("x" + gameState.player.lives);
        }

        // Afficher les bonus actuels
        if (bonusLabel != null) {
            String bonusText = String.format("Bonus - Feu: %d | Vitesse: %d | Bombe: %d",
                    gameState.player.feuBonusCount,
                    gameState.player.vitesseBonusCount,
                    gameState.player.bombeBonusCount);
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

        // Objets visibles (dessiner avant les murs pour qu'ils soient en arrière-plan)
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
                // Fallback avec des couleurs si les images ne sont pas disponibles
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
                        gc.setFill(Color.web("#8B008B")); // Violet foncé pour le skull
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

        // Murs destructibles avec image ou couleur de fallback
        for (Position wall : gameState.destructibleWalls) {
            if (brickImage != null) {
                // Utiliser l'image de brique
                gc.drawImage(brickImage,
                        wall.x * CELL_SIZE, wall.y * CELL_SIZE,
                        CELL_SIZE, CELL_SIZE);
            } else {
                // Fallback avec la couleur originale si l'image n'est pas disponible
                gc.setFill(Color.web("#8D6E63"));
                gc.fillRect(wall.x * CELL_SIZE, wall.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                // Texture brique
                gc.setFill(Color.web("#A1887F"));
                for (int i = 0; i < 3; i++) {
                    gc.fillRect(wall.x * CELL_SIZE + 5, wall.y * CELL_SIZE + i * 12 + 5, CELL_SIZE - 10, 8);
                }
            }
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

    // Énumération pour les types d'objets
    enum ItemType {
        FEU, VITESSE, BOMBE, SKULL // Ajout du type SKULL
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

        // Compteurs de bonus
        int feuBonusCount = 0;
        int vitesseBonusCount = 0;
        int bombeBonusCount = 0;

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

    static class Item {
        ItemType type;
        Item(ItemType type) { this.type = type; }
    }

    static class GameState {
        Player player;
        List<Bomb> bombs = new ArrayList<>();
        List<Explosion> explosions = new ArrayList<>();
        Set<Position> walls = new HashSet<>();
        Set<Position> destructibleWalls = new HashSet<>();
        Map<Position, Item> hiddenItems = new HashMap<>(); // Objets cachés dans les murs
        Map<Position, Item> visibleItems = new HashMap<>(); // Objets visibles sur le terrain
        int score = 0;
        int level = 1;

        GameState() {
            player = new Player(1, 1);
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
                    if (!walls.contains(new Position(i, j)) &&
                            !(i <= 2 && j <= 2) &&
                            random.nextDouble() < 0.3) {
                        Position pos = new Position(i, j);
                        destructibleWalls.add(pos);
                        availablePositions.add(pos);
                    }
                }
            }

            // Placer les objets aléatoirement dans les murs destructibles
            placeItems(availablePositions, random);
        }

        private void placeItems(List<Position> availablePositions, Random random) {
            if (availablePositions.size() < 17) { // 4 + 4 + 8 + 1 = 17 objets minimum
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

            // Placer 1 skull (objet mortel)
            if (index < availablePositions.size()) {
                hiddenItems.put(availablePositions.get(index), new Item(ItemType.SKULL));
            }
        }
    }
}