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
        COLLECTING      // Va vers un bonus
    }

    public Bot(int x, int y) {
        super(x, y);
        this.random = new Random();
        this.currentState = BotState.EXPLORING;
    }


    // Méthode principale appelée à chaque frame
    public BotAction getNextAction(GameState gameState) {
        long currentTime = System.currentTimeMillis();

        // Respecter le délai entre les actions selon la difficulté
        if (currentTime - lastActionTime < difficulty.actionDelay) {
            return BotAction.NONE;
        }

        // Analyser l'état du jeu et déterminer la meilleure action
        BotAction action = decideAction(gameState);

        if (action != BotAction.NONE) {
            lastActionTime = currentTime;
        }

        return action;
    }

    private BotAction decideAction(GameState gameState) {
        // 1. Priorité absolue : fuir si en danger
        if (isInDanger(gameState)) {
            currentState = BotState.FLEEING;
            Position safePos = findSafePosition(gameState);
            if (safePos != null) {
                return getMoveTowardsPosition(safePos, gameState);
            }
        }

        // 2. Décision intelligente vs aléatoire selon la difficulté
        if (random.nextDouble() < difficulty.smartnessRatio) {
            return makeSmartDecision(gameState);
        } else {
            return makeRandomDecision(gameState);
        }
    }

    private BotAction makeSmartDecision(GameState gameState) {
        // Chercher des joueurs à proximité pour les attaquer
        Player nearestPlayer = findNearestPlayer(gameState);
        if (nearestPlayer != null && getDistanceToPlayer(nearestPlayer) <= 3) {
            currentState = BotState.HUNTING;

            // Si assez proche et chance de bomber
            if (getDistanceToPlayer(nearestPlayer) <=  getBombRange() + 1 &&
                    random.nextDouble() < difficulty.bombChance &&
                    getBombsRemaining() > 0) {
                return BotAction.PLACE_BOMB;
            }

            // Sinon se rapprocher
            return getMoveTowardsPosition(nearestPlayer.getPos(), gameState);
        }

        // Chercher des bonus visibles
        Position nearestBonus = findNearestBonus(gameState);
        if (nearestBonus != null) {
            currentState = BotState.COLLECTING;
            return getMoveTowardsPosition(nearestBonus, gameState);
        }

        // Chercher des murs destructibles à détruire
        Position nearestWall = findNearestDestructibleWall(gameState);
        if (nearestWall != null && getBombsRemaining() > 0) {
            Position bombPos = findBombPositionForWall(nearestWall, gameState);
            if (bombPos != null) {
                if (getPos().equals(bombPos)) {
                    return BotAction.PLACE_BOMB;
                }
                return getMoveTowardsPosition(bombPos, gameState);
            }
        }

        // Exploration générale
        currentState = BotState.EXPLORING;
        return exploreMap(gameState);
    }

    private BotAction makeRandomDecision(GameState gameState) {
        List<BotAction> possibleActions = new ArrayList<>();

        // Ajouter les mouvements possibles
        if (canMove(gameState, Direction.UP)) possibleActions.add(BotAction.MOVE_UP);
        if (canMove(gameState, Direction.DOWN)) possibleActions.add(BotAction.MOVE_DOWN);
        if (canMove(gameState, Direction.LEFT)) possibleActions.add(BotAction.MOVE_LEFT);
        if (canMove(gameState, Direction.RIGHT)) possibleActions.add(BotAction.MOVE_RIGHT);

        // Parfois poser une bombe
        if (getBombsRemaining() > 0 && random.nextDouble() < 0.1) {
            possibleActions.add(BotAction.PLACE_BOMB);
        }

        if (possibleActions.isEmpty()) {
            return BotAction.NONE;
        }

        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    private boolean isInDanger(GameState gameState) {
        Position myPos = getPos();

        // Vérifier si dans la zone d'explosion d'une bombe
        for (Bomb bomb : gameState.bombs) {
            if (isInExplosionRange(myPos, bomb, gameState)) {
                return true;
            }
        }

        // Vérifier les explosions actuelles
        for (Explosion explosion : gameState.explosions) {
            if (explosion.containsPosition(myPos)) {
                return true;
            }
        }

        return false;
    }

    private boolean isInExplosionRange(Position pos, Bomb bomb, GameState gameState) {
        Position bombPos = bomb.getPos();
        int range = bomb.getOwner();

        // Même ligne horizontale
        if (pos.getY() == bombPos.getY()) {
            int distance = Math.abs(pos.getX() - bombPos.getX());
            if (distance <= range) {
                // Vérifier qu'il n'y a pas de mur entre la bombe et la position
                return !hasWallBetween(bombPos, pos, gameState);
            }
        }

        // Même ligne verticale
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

        // Chercher dans un rayon de 4 cases
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                Position candidate = new Position(myPos.getX() + dx, myPos.getY() + dy);

                if (isPositionSafe(candidate, gameState) &&
                        isPositionWalkable(candidate, gameState)) {
                    candidates.add(candidate);
                }
            }
        }

        // Retourner la position safe la plus proche
        return candidates.isEmpty() ? null :
                candidates.stream()
                        .min(Comparator.comparingDouble(p -> getDistance(myPos, p)))
                        .orElse(null);
    }

    private boolean isPositionSafe(Position pos, GameState gameState) {
        // Vérifier qu'aucune bombe ne menace cette position
        for (Bomb bomb : gameState.bombs) {
            if (isInExplosionRange(pos, bomb, gameState)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPositionWalkable(Position pos, GameState gameState) {
        // Vérifier les limites de la carte (15x15)
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getX() >= 15 || pos.getY() >= 15) {
            return false;
        }

        // Vérifier les murs fixes et destructibles
        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        // Vérifier qu'il n'y a pas d'autre joueur ou bombe à cette position
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
                double distance = getDistance(myPos, player.getPos());
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
            double distance = getDistance(myPos, bonusPos);
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
            double distance = getDistance(myPos, wallPos);
            if (distance < minDistance && distance <= getBombRange() + 2) {
                minDistance = distance;
                nearest = wallPos;
            }
        }

        return nearest;
    }

    private Position findBombPositionForWall(Position wall, GameState gameState) {
        Position myPos = getPos();
        List<Position> candidates = new ArrayList<>();

        // Positions possibles pour détruire le mur
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            for (int i = 1; i <= getBombRange(); i++) {
                Position candidate = new Position(wall.getX() + dir[0] * i, wall.getY() + dir[1] * i);
                if (isPositionWalkable(candidate, gameState)) {
                    candidates.add(candidate);
                }
            }
        }

        // Retourner la position la plus proche
        return candidates.stream()
                .min(Comparator.comparingDouble(p -> getDistance(myPos, p)))
                .orElse(null);
    }

    private double getDistance(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) +
                Math.pow(p1.getY() - p2.getY(), 2));
    }

    private double getDistanceToPlayer(Player player) {
        return getDistance(getPos(), player.getPos());
    }

    private BotAction getMoveTowardsPosition(Position target, GameState gameState) {
        Position myPos = getPos();

        int dx = target.getX() - myPos.getX();
        int dy = target.getY() - myPos.getY();

        // Essayer le mouvement prioritaire
        BotAction primaryAction = null;
        BotAction secondaryAction = null;

        if (Math.abs(dx) > Math.abs(dy)) {
            primaryAction = dx > 0 ? BotAction.MOVE_RIGHT : BotAction.MOVE_LEFT;
            secondaryAction = dy > 0 ? BotAction.MOVE_DOWN : BotAction.MOVE_UP;
        } else {
            primaryAction = dy > 0 ? BotAction.MOVE_DOWN : BotAction.MOVE_UP;
            secondaryAction = dx > 0 ? BotAction.MOVE_RIGHT : BotAction.MOVE_LEFT;
        }

        // Vérifier si le mouvement prioritaire est possible
        if (canMoveInDirection(primaryAction, gameState)) {
            return primaryAction;
        } else if (canMoveInDirection(secondaryAction, gameState)) {
            return secondaryAction;
        }

        // Si aucun mouvement direct n'est possible, essayer les autres directions
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
        // Mouvement d'exploration simple - éviter de rester bloqué
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
    private List<Bot> bots;
    private GameState gameState;

    public BotManager(GameState gameState) {
        this.gameState = gameState;
        this.bots = new ArrayList<>();
    }

    public void addBot(int x, int y ) {
        Bot bot = new Bot(x, y);
        bots.add(bot);

        // Ajouter le bot à la liste des joueurs dans gameState
        // Trouver le premier slot libre
        for (int i = 0; i < gameState.getPlayers().length; i++) {
            if (gameState.getPlayers()[i] == null || !gameState.getPlayers()[i].getisAlive()) {
                gameState.getPlayers()[i] = bot;
                break;
            }
        }
    }

    public void updateBots(long currentTime) {
        for (Bot bot : bots) {
            if (bot.getisAlive()) {
                BotAction action = bot.getNextAction(gameState);
                executeAction(bot, action);
            }
        }
    }

    private void executeAction(Bot bot, BotAction action) {
        Position currentPos = bot.getPos();
        Position newPos = null;

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
                    Bomb newBomb = new Bomb(currentPos.getX(),currentPos.getY(), bot.getBombRange());
                    gameState.bombs.add(newBomb);
                    bot.setBombsRemaining(bot.getBombsRemaining() - 1);
                }
                break;
            case NONE:
                // Ne rien faire
                break;
        }

        // Effectuer le mouvement si possible
        if (newPos != null && isValidMove(newPos)) {
            bot.setPos(newPos);

            // Vérifier si le bot ramasse un bonus
            Item item = gameState.visibleItems.get(newPos);
            if (item != null) {
                bot.applyItem(item);
                gameState.visibleItems.remove(newPos);
            }
        }
    }

    private boolean isValidMove(Position pos) {
        // Vérifier les limites
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getX() >= 15 || pos.getY() >= 15) {
            return false;
        }

        // Vérifier les murs
        if (gameState.walls.contains(pos) || gameState.destructibleWalls.contains(pos)) {
            return false;
        }

        // Vérifier les autres joueurs
        for (Player player : gameState.getPlayers()) {
            if (player != null && player.getisAlive() && player.getPos().equals(pos)) {
                return false;
            }
        }

        // Vérifier les bombes
        for (Bomb bomb : gameState.bombs) {
            if (bomb.getPos().equals(pos)) {
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

    public List<Bot> getBots() { return bots; }
}