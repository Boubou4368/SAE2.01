// Bot.java - Classe principale pour les bots
package maquette.sae2_01;

import java.util.*;

public class Bot extends Player {
    private BotDifficulty difficulty;
    private Random random;
    private long lastActionTime = 0;
    private Position targetPosition;
    private BotState currentState;

    public enum BotDifficulty {
        EASY(0.3, 1000, 0.1),    // 30% bonnes décisions, 1s délai, 10% chance de poser bombe
        MEDIUM(0.6, 700, 0.2),   // 60% bonnes décisions, 0.7s délai, 20% chance de poser bombe
        HARD(0.8, 400, 0.35);    // 80% bonnes décisions, 0.4s délai, 35% chance de poser bombe

        public final double smartnessRatio;
        public final long actionDelay;
        public final double bombChance;

        BotDifficulty(double smartnessRatio, long actionDelay, double bombChance) {
            this.smartnessRatio = smartnessRatio;
            this.actionDelay = actionDelay;
            this.bombChance = bombChance;
        }
    }

    public enum BotState {
        EXPLORING,      // Cherche des bonus/ennemis
        FLEEING,        // Fuit une explosion
        HUNTING,        // Poursuit un joueur
        COLLECTING,
        WALL_BREAKING
    }

    public Bot(int x, int y) {
        super(x, y);
        this.difficulty = BotDifficulty.HARD;
        this.random = new Random();
        this.currentState = BotState.EXPLORING;

    }


    // Méthode principale appelée à chaque frame
    public BotAction getNextAction(GameState gameState) {
        long currentTime = System.currentTimeMillis();

        // Respect action delay based on difficulty
        if (currentTime - lastActionTime < difficulty.actionDelay) {
            return BotAction.NONE;
        }


        // Analyze game state and determine best action
        BotAction action = decideAction(gameState);

        if (action != BotAction.NONE) {
            lastActionTime = currentTime;
        }

        return action;
    }


    private BotAction decideAction(GameState gameState) {
        // 1. Absolute priority: flee if in danger
        if (isInDanger(gameState)) {
            currentState = BotState.FLEEING;
            Position safePos = findSafePosition(gameState);
            if (safePos != null) {
                return getMoveTowardsPosition(safePos, gameState);
            }
        }


        // 3. Smart vs random decision based on difficulty
        if (random.nextDouble() < difficulty.smartnessRatio) {
            return makeSmartDecision(gameState);
        } else {
            return makeRandomDecision(gameState);
        }
    }

    private BotAction makeSmartDecision(GameState gameState) {
        // Look for nearby players to attack
        Player nearestPlayer = findNearestPlayer(gameState);
        if (nearestPlayer != null && getDistanceToPlayer(nearestPlayer) <= 4) {
            currentState = BotState.HUNTING;

            // If close enough and chance to bomb
            if (getDistanceToPlayer(nearestPlayer) <= getBombRange() + 1 &&
                    random.nextDouble() < difficulty.bombChance &&
                    getBombsRemaining() > 0 &&
                    isPositionSafe(getPos(), gameState)) {
                return BotAction.PLACE_BOMB;
            }

            // Otherwise get closer
            return getMoveTowardsPosition(nearestPlayer.getPos(), gameState);
        }

        // Look for visible bonuses
        Position nearestBonus = findNearestBonus(gameState);
        if (nearestBonus != null) {
            currentState = BotState.COLLECTING;
            return getMoveTowardsPosition(nearestBonus, gameState);
        }

        // Look for destructible walls to destroy
        Position nearestWall = findNearestDestructibleWall(gameState);
        if (nearestWall != null && getBombsRemaining() > 0) {
            currentState = BotState.WALL_BREAKING;
            Position bombPos = findBombPositionForWall(nearestWall, gameState);
            if (bombPos != null) {
                if (getPos().equals(bombPos) && isPositionSafe(getPos(), gameState)) {
                    return BotAction.PLACE_BOMB;
                }
                return getMoveTowardsPosition(bombPos, gameState);
            }
        }

        // General exploration
        currentState = BotState.EXPLORING;
        return exploreMap(gameState);
    }

    private BotAction makeRandomDecision(GameState gameState) {
        List<BotAction> possibleActions = new ArrayList<>();

        // Add possible movements
        if (canMove(gameState, Direction.UP)) possibleActions.add(BotAction.MOVE_UP);
        if (canMove(gameState, Direction.DOWN)) possibleActions.add(BotAction.MOVE_DOWN);
        if (canMove(gameState, Direction.LEFT)) possibleActions.add(BotAction.MOVE_LEFT);
        if (canMove(gameState, Direction.RIGHT)) possibleActions.add(BotAction.MOVE_RIGHT);

        // Sometimes place a bomb (only if safe)
        if (getBombsRemaining() > 0 &&
                random.nextDouble() < 0.1 &&
                isPositionSafe(getPos(), gameState)) {
            possibleActions.add(BotAction.PLACE_BOMB);
        }

        if (possibleActions.isEmpty()) {
            return BotAction.NONE;
        }

        return possibleActions.get(random.nextInt(possibleActions.size()));
    }
    private BotAction makeRandomMovement(GameState gameState) {
        List<BotAction> possibleMoves = new ArrayList<>();

        if (canMove(gameState, Direction.UP)) possibleMoves.add(BotAction.MOVE_UP);
        if (canMove(gameState, Direction.DOWN)) possibleMoves.add(BotAction.MOVE_DOWN);
        if (canMove(gameState, Direction.LEFT)) possibleMoves.add(BotAction.MOVE_LEFT);
        if (canMove(gameState, Direction.RIGHT)) possibleMoves.add(BotAction.MOVE_RIGHT);

        if (possibleMoves.isEmpty()) {
            return BotAction.NONE;
        }

        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    private boolean isInDanger(GameState gameState) {
        Position myPos = getPos();

        // Check if in explosion range of a bomb
        for (Bomb bomb : gameState.bombs) {
            if (isInExplosionRange(myPos, bomb, gameState)) {
                return true;
            }
        }

        // Check current explosions
        for (Explosion explosion : gameState.explosions) {
            if (explosion.containsPosition(myPos)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInExplosionRange(Position pos, Bomb bomb, GameState gameState) {
        Position bombPos = bomb.getPos();

        // Get the bomb range from the owner player
        Player bombOwner = null;
        for (Player player : gameState.getPlayers()) {
            if (player != null && gameState.getPlayerIndex(player) == bomb.getOwner()) {
                bombOwner = player;
                break;
            }
        }

        // If we can't find the owner, use default range of 2
        int range = (bombOwner != null) ? bombOwner.getBombRange() : 2;

        // Same horizontal line
        if (pos.getY() == bombPos.getY()) {
            int distance = Math.abs(pos.getX() - bombPos.getX());
            if (distance <= range) {
                return !hasWallBetween(bombPos, pos, gameState);
            }
        }

        // Same vertical line
        if (pos.getX() == bombPos.getX()) {
            int distance = Math.abs(pos.getY() - bombPos.getY());
            if (distance <= range) {
                return !hasWallBetween(bombPos, pos, gameState);
            }
        }

        return false;
    }

    private boolean hasWallBetween(Position start, Position end, GameState gameState) {
        int dx = Integer.compare(end.getX() - start.getX(), 0);
        int dy = Integer.compare(end.getY() - start.getY(), 0);

        Position current = new Position(start.getX() + dx, start.getY() + dy);

        while (!current.equals(end)) {
            if (gameState.walls.contains(current) || gameState.destructibleWalls.contains(current)) {
                return true;
            }
            current = new Position(current.getX() + dx, current.getY() + dy);
        }

        return false;
    }

    private Position findSafePosition(GameState gameState) {
        Position myPos = getPos();
        List<Position> candidates = new ArrayList<>();

        // Search in a radius of 5 squares
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                Position candidate = new Position(myPos.getX() + dx, myPos.getY() + dy);

                if (isPositionSafe(candidate, gameState) &&
                        isPositionWalkable(candidate, gameState)) {
                    candidates.add(candidate);
                }
            }
        }

        // Return the closest safe position
        return candidates.isEmpty() ? null :
                candidates.stream()
                        .min(Comparator.comparingDouble(p -> getManhattanDistance(myPos, p)))
                        .orElse(null);
    }

    private boolean isPositionSafe(Position pos, GameState gameState) {
        // Check that no bomb threatens this position
        for (Bomb bomb : gameState.bombs) {
            if (isInExplosionRange(pos, bomb, gameState)) {
                return false;
            }
        }

        // Check current explosions
        for (Explosion explosion : gameState.explosions) {
            if (explosion.containsPosition(pos)) {
                return false;
            }
        }

        return true;
    }

    private boolean isPositionWalkable(Position pos, GameState gameState) {
        // Check map boundaries (15x15)
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getX() >= 15 || pos.getY() >= 15) {
            return false;
        }

        // Check fixed and destructible walls
        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        // Check that there's no other player or bomb at this position
        for (Player player : gameState.getPlayers()) {
            if (player != this && player.getisAlive() && player.getPos().equals(pos)) {
                return false;
            }
        }

        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(pos)) {
                return false;
            }
        }

        return true;
    }

    private Player findNearestPlayer(GameState gameState) {
        Position myPos = getPos();
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Player player : gameState.getPlayers()) {
            if (player != this && player.getisAlive()) {
                double distance = getManhattanDistance(myPos, player.getPos());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = player;
                }
            }
        }

        return nearest;
    }

    private Position findNearestBonus(GameState gameState) {
        Position myPos = getPos();
        Position nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Position bonusPos : gameState.visibleItems.keySet()) {
            double distance = getManhattanDistance(myPos, bonusPos);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = bonusPos;
            }
        }

        return nearest;
    }

    private Position findNearestDestructibleWall(GameState gameState) {
        Position myPos = getPos();
        Position nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Position wallPos : gameState.destructibleWalls) {
            double distance = getManhattanDistance(myPos, wallPos);
            if (distance < minDistance && distance <= getBombRange() + 3) {
                minDistance = distance;
                nearest = wallPos;
            }
        }

        return nearest;
    }

    private Position findBombPositionForWall(Position wall, GameState gameState) {
        Position myPos = getPos();
        List<Position> candidates = new ArrayList<>();

        // Possible positions to destroy the wall
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int i = 1; i <= getBombRange(); i++) {
                Position candidate = new Position(wall.getX() + dir[0] * i, wall.getY() + dir[1] * i);
                if (isPositionWalkable(candidate, gameState) &&
                        isPositionSafe(candidate, gameState)) {
                    candidates.add(candidate);
                }
            }
        }

        // Return the closest position
        return candidates.stream()
                .min(Comparator.comparingDouble(p -> getManhattanDistance(myPos, p)))
                .orElse(null);
    }

    private double getManhattanDistance(Position p1, Position p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    private double getDistanceToPlayer(Player player) {
        return getManhattanDistance(getPos(), player.getPos());
    }

    private BotAction getMoveTowardsPosition(Position target, GameState gameState) {
        Position myPos = getPos();

        int dx = target.getX() - myPos.getX();
        int dy = target.getY() - myPos.getY();

        // Try priority movement
        BotAction primaryAction = null;
        BotAction secondaryAction = null;

        if (Math.abs(dx) > Math.abs(dy)) {
            primaryAction = dx > 0 ? BotAction.MOVE_RIGHT : BotAction.MOVE_LEFT;
            secondaryAction = dy > 0 ? BotAction.MOVE_DOWN : BotAction.MOVE_UP;
        } else {
            primaryAction = dy > 0 ? BotAction.MOVE_DOWN : BotAction.MOVE_UP;
            secondaryAction = dx > 0 ? BotAction.MOVE_RIGHT : BotAction.MOVE_LEFT;
        }

        // Check if priority movement is possible
        if (canMoveInDirection(primaryAction, gameState)) {
            return primaryAction;
        } else if (canMoveInDirection(secondaryAction, gameState)) {
            return secondaryAction;
        }

        // If no direct movement is possible, try other directions
        BotAction[] allActions = {BotAction.MOVE_UP, BotAction.MOVE_DOWN,
                BotAction.MOVE_LEFT, BotAction.MOVE_RIGHT};
        for (BotAction action : allActions) {
            if (canMoveInDirection(action, gameState)) {
                return action;
            }
        }

        return BotAction.NONE;
    }

    private boolean canMoveInDirection(BotAction action, GameState gameState) {
        Direction dir = botActionToDirection(action);
        return dir != null && canMove(gameState, dir);
    }

    private Direction botActionToDirection(BotAction action) {
        switch (action) {
            case MOVE_UP: return Direction.UP;
            case MOVE_DOWN: return Direction.DOWN;
            case MOVE_LEFT: return Direction.LEFT;
            case MOVE_RIGHT: return Direction.RIGHT;
            default: return null;
        }
    }

    private BotAction exploreMap(GameState gameState) {
        // Simple exploration movement - avoid getting stuck
        List<Direction> directions = Arrays.asList(Direction.UP, Direction.DOWN,
                Direction.LEFT, Direction.RIGHT);
        Collections.shuffle(directions);

        for (Direction dir : directions) {
            if (canMove(gameState, dir)) {
                return directionToBotAction(dir);
            }
        }

        return BotAction.NONE;
    }

    private boolean canMove(GameState gameState, Direction direction) {
        Position myPos = getPos();
        Position newPos = getNewPosition(myPos, direction);
        return isPositionWalkable(newPos, gameState);
    }

    private Position getNewPosition(Position current, Direction direction) {
        switch (direction) {
            case UP: return new Position(current.getX(), current.getY() - 1);
            case DOWN: return new Position(current.getX(), current.getY() + 1);
            case LEFT: return new Position(current.getX() - 1, current.getY());
            case RIGHT: return new Position(current.getX() + 1, current.getY());
            default: return current;
        }
    }

    private BotAction directionToBotAction(Direction direction) {
        switch (direction) {
            case UP: return BotAction.MOVE_UP;
            case DOWN: return BotAction.MOVE_DOWN;
            case LEFT: return BotAction.MOVE_LEFT;
            case RIGHT: return BotAction.MOVE_RIGHT;
            default: return BotAction.NONE;
        }
    }

    // Getters
    public BotDifficulty getDifficulty() { return difficulty; }
    public BotState getCurrentState() { return currentState; }
    public void setDifficulty(BotDifficulty difficulty) { this.difficulty = difficulty; }
}

// BotAction.java - Actions possibles pour le bot
enum BotAction {
    NONE,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    PLACE_BOMB
}

// Direction.java
enum Direction {
    UP, DOWN, LEFT, RIGHT
}

// BotManager.java - Gestionnaire pour plusieurs bots
class BotManager {
    private static final long MOVE_DELAY_NS = 500_000_000L; // 500ms en nanosecondes
    private List<Bot> bots;
    private GameState gameState;
    private long[] lastMoveTimes; // Tableau pour stocker le dernier temps de mouvement de chaque bot

    public BotManager(GameState gameState) {
        this.gameState = gameState;
        this.bots = new ArrayList<>();
        this.lastMoveTimes = new long[4]; // Pour 4 joueurs max
    }

    public void addBot(int x, int y) {
        Bot bot = new Bot(x, y);
        bots.add(bot);

        // Ajouter le bot à la liste des joueurs dans gameState
        // Trouver le premier slot libre
        for (int i = 0; i < gameState.getPlayers().length; i++) {
            if (gameState.getPlayers()[i] == null || !gameState.getPlayers()[i].getisAlive()) {
                gameState.getPlayers()[i] = bot;
                lastMoveTimes[i] = 0; // Initialiser le temps de mouvement
                System.out.println("Bot ajouté au slot " + i);
                break;
            }
        }
    }

    public void updateBots(long currentTime) {
        // Parcourir tous les slots de joueurs pour trouver les bots
        for (int i = 0; i < gameState.getPlayers().length; i++) {
            Player player = gameState.getPlayers()[i];

            if (player != null && player.isBot() && player.getisAlive()) {
                // Vérifier si assez de temps s'est écoulé depuis le dernier mouvement
                if (currentTime - lastMoveTimes[i] > MOVE_DELAY_NS) {
                    Bot bot = (Bot) player;
                    BotAction action = bot.getNextAction(gameState);

                    System.out.println("Bot " + (i + 1) + " - Action: " + action);

                    if (executeAction(bot, action, i)) {
                        lastMoveTimes[i] = currentTime; // Mettre à jour le temps seulement si l'action a réussi
                    }
                }
            }
        }
    }

    private boolean executeAction(Bot bot, BotAction action, int playerIndex) {
        Position currentPos = bot.getPos();
        Position newPos = null;
        boolean actionExecuted = false;

        switch (action) {
            case MOVE_UP:
                newPos = new Position(currentPos.getX(), currentPos.getY() - 1);
                break;
            case MOVE_DOWN:
                newPos = new Position(currentPos.getX(), currentPos.getY() + 1);
                break;
            case MOVE_LEFT:
                newPos = new Position(currentPos.getX() - 1, currentPos.getY());
                break;
            case MOVE_RIGHT:
                newPos = new Position(currentPos.getX() + 1, currentPos.getY());
                break;
            case PLACE_BOMB:
                // Créer une nouvelle bombe à la position du bot
                if (bot.getBombsRemaining() > 0) {
                    Bomb newBomb = new Bomb(currentPos.getX(), currentPos.getY(), bot.getBombRange());
                    gameState.bombs.add(newBomb);
                    bot.setBombsRemaining(bot.getBombsRemaining() - 1);
                    System.out.println("Bot " + (playerIndex + 1) + " place une bombe");
                    actionExecuted = true;
                }
                break;
            case NONE:
                // Ne rien faire mais considérer comme exécuté pour le timing
                actionExecuted = true;
                break;
        }

        // Effectuer le mouvement si possible
        if (newPos != null && isValidMove(newPos, playerIndex)) {
            bot.setPos(newPos);
            System.out.println("Bot " + (playerIndex + 1) + " bouge vers " + newPos.getX() + "," + newPos.getY());

            // Vérifier si le bot ramasse un bonus
            Item item = gameState.visibleItems.get(newPos);
            if (item != null) {
                bot.applyItem(item);
                gameState.visibleItems.remove(newPos);
                System.out.println("Bot " + (playerIndex + 1) + " ramasse un item");
            }
            actionExecuted = true;
        } else if (newPos != null) {
            System.out.println("Bot " + (playerIndex + 1) + " ne peut pas bouger vers " + newPos.getX() + "," + newPos.getY());
        }

        return actionExecuted;
    }

    private boolean isValidMove(Position pos, int currentPlayerIndex) {
        // Vérifier les limites
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getX() >= 15 || pos.getY() >= 15) {
            System.out.println("Mouvement invalide: hors limites");
            return false;
        }

        // Vérifier les murs
        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            System.out.println("Mouvement invalide: mur");
            return false;
        }

        // Vérifier les autres joueurs (exclure le joueur actuel)
        for (int i = 0; i < gameState.getPlayers().length; i++) {
            Player player = gameState.getPlayers()[i];
            if (i != currentPlayerIndex && player != null && player.getisAlive() && player.getPos().equals(pos)) {
                System.out.println("Mouvement invalide: collision avec joueur");
                return false;
            }
        }

        // Vérifier les bombes
        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(pos)) {
                System.out.println("Mouvement invalide: bombe");
                return false;
            }
        }

        return true;
    }

    public List<Player> getAllPlayers() {
        List<Player> allPlayers = new ArrayList<>();
        for (Player player : gameState.getPlayers()) {
            if (player != null) {
                allPlayers.add(player);
            }
        }
        return allPlayers;
    }

    public List<Bot> getBots() {
        return bots;
    }
}