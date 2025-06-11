package maquette.sae2_01;

import java.util.*;
import maquette.sae2_01.*;

public class GameState {
    private static final int GRID_SIZE = 15;

    public Player[] players = new Player[4];
    public List<Bomb> bombs = new ArrayList<>();
    public List<Explosion> explosions = new ArrayList<>();
    public Set<Position> walls = new HashSet<>();
    public Set<Position> destructibleWalls = new HashSet<>();
    public Map<Position, Item> hiddenItems = new HashMap<>();
    public Map<Position, Item> visibleItems = new HashMap<>();
    public int level = 1;

    GameState() {
        // Positions de départ aux 4 coins
        players[0] = new Player(1, 1);                          // Joueur 1
        players[1] = new Player(GRID_SIZE - 2, GRID_SIZE - 2);   // Joueur 2
        players[2] = new Player(1, GRID_SIZE - 2);               // Joueur 3
        players[3] = new Player(GRID_SIZE - 2, 1);               // Joueur 4

        initializeMap();
    }

    // Méthodes d'accès pour compatibilité
    public Player getPlayer1() {
        return players[0];
    }

    public Player getPlayer2() {
        return players[1];
    }

    public Player getPlayer3() {
        return players[2];
    }

    public Player getPlayer4() {
        return players[3];
    }

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
        if (pos.getX() <= 2 && pos.getY() <= 2) return true;

        // Zone de départ joueur 2 (coin bas-droite) - 3x3
        if (pos.getX() >= GRID_SIZE - 3 && pos.getY() >= GRID_SIZE - 3) return true;

        // Zone de départ joueur 3 (coin bas-gauche) - 3x3
        if (pos.getX() <= 2 && pos.getY() >= GRID_SIZE - 3) return true;

        // Zone de départ joueur 4 (coin haut-droite) - 3x3
        if (pos.getX() >= GRID_SIZE - 3 && pos.getY() <= 2) return true;

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

    public int[] getAlivePlayersInfo() {
        int alivePlayers = 0;
        int lastAliveIndex = -1;

        for (int i = 0; i < 4; i++) {
            if (players[i].getisAlive()) {
                alivePlayers++;
                lastAliveIndex = i;
            }
        }

        return new int[]{alivePlayers, lastAliveIndex};
    }

    public void revealHiddenItem(Position pos) {
        Item hiddenItem = hiddenItems.get(pos);
        if (hiddenItem != null) {
            visibleItems.put(pos, hiddenItem);
            hiddenItems.remove(pos);
        }
    }

    public void printMapInfo() {
        System.out.println("=== INFO MAP ===");
        System.out.println("Murs fixes: " + walls.size());
        System.out.println("Murs destructibles: " + destructibleWalls.size());
        System.out.println("Objets cachés: " + hiddenItems.size());
        System.out.println("Objets visibles: " + visibleItems.size());

        // Compter les types d'objets
        Map<ItemType, Integer> itemCount = new HashMap<>();
        for (Item item : hiddenItems.values()) {
            itemCount.put(item.getType(), itemCount.getOrDefault(item.getType(), 0) + 1);
        }
        System.out.println("Répartition des objets:");
        for (Map.Entry<ItemType, Integer> entry : itemCount.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("================");
    }
}