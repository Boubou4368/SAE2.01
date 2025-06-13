package maquette.sae2_01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import maquette.sae2_01.*;

public class GameStateCustom {
    private static final int GRID_SIZE = 15;

    public Player[] players = new Player[4];
    public List<Bomb> bombs = new ArrayList<>();
    public List<Explosion> explosions = new ArrayList<>();
    public Set<Position> walls = new HashSet<>();
    public Set<Position> destructibleWalls = new HashSet<>();
    public Map<Position, Item> hiddenItems = new HashMap<>();
    public Map<Position, Item> visibleItems = new HashMap<>();
    public int level = 1;
    public String[][] customMap = loadLevel(SoloModeController.fichier);
    public static boolean CTF = false;
    public static boolean Item = false;

    GameStateCustom() {
        // Positions de départ aux 4 coins

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
                else if (Objects.equals(customMap[j-1][i-1], "MUR_INDESTRUCTIBLE")) {
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
                if (Objects.equals(customMap[j-1][i-1], "MUR_DESTRUCTIBLE") || Objects.equals(customMap[j-1][i-1], "FEU") || Objects.equals(customMap[j-1][i-1], "BOMBE") || Objects.equals(customMap[j-1][i-1], "VITESSE") || Objects.equals(customMap[j-1][i-1], "KICK") || Objects.equals(customMap[j-1][i-1], "SKULL")) {
                    destructibleWalls.add(pos);
                    availablePositions.add(pos);
                }
            }
        }
        for (int i = 0; i < GRID_SIZE - 2; i++) {
            for (int j = 0; j < GRID_SIZE - 2; j++) {
                Position pos = new Position(i+1, j+1);

                // Vérifier que la position n'est pas un mur fixe et n'est pas dans une zone de départ
                switch (customMap[j][i]) {
                    case "JOUEUR_1":
                        players[0] = new Player(pos.getX(),pos.getY());                          // Joueur 1
                        break;
                    case "JOUEUR_2":
                        players[1] = new Player(pos.getX(),pos.getY());   // Joueur 2
                        break;
                    case "JOUEUR_3":
                        players[2] = new Player(pos.getX(),pos.getY());               // Joueur 3
                        break;
                    case "JOUEUR_4":
                        players[3] = new Player(pos.getX(),pos.getY());               // Joueur 4
                        break;
                }
            }
        }


        // Placer les objets aléatoirement dans les murs destructibles
        if (!Item) placeItems(availablePositions, random);
        else placeItemsCustom();

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
            System.out.println("  " + entry.getKey() + "player: " + entry.getValue());
        }
        System.out.println("================");
    }

    public static String[][] loadLevel(String filename) {
        String[][] levelMap = new String[13][13]; // taille fixe 13x13

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < 13) {
                // découper la ligne par espaces
                String[] cells = line.trim().split("\\s+");

                // remplir la ligne du tableau (en cas d'erreur taille on adapte)
                for (int col = 0; col < Math.min(cells.length, 13); col++) {
                    levelMap[row][col] = cells[col];
                    if (Objects.equals(cells[col], "DRAPEAU1") || Objects.equals(cells[col], "DRAPEAU2") || Objects.equals(cells[col], "DRAPEAU3") || Objects.equals(cells[col], "DRAPEAU4"))
                        CTF = true;

                    if (Objects.equals(cells[col], "BOMBE") || Objects.equals(cells[col], "VITESSE") || Objects.equals(cells[col], "FEU") || Objects.equals(cells[col], "KICK") || Objects.equals(cells[col], "SKULL"))
                        Item = true;

                }
                // Si moins de 13 éléments, remplir le reste par EMPTY
                for (int col = cells.length; col < 13; col++) {
                    levelMap[row][col] = "EMPTY";
                }

                row++;
            }

            // Si moins de 13 lignes, remplir le reste par EMPTY
            for (int r = row; r < 13; r++) {
                for (int c = 0; c < 13; c++) {
                    levelMap[r][c] = "EMPTY";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return levelMap;
    }

    private void placeItemsCustom() {

        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                Position pos = new Position(j + 1, i + 1);
                String c = customMap[i][j].toString();
                switch(c) {
                    case "BOMBE":
                        hiddenItems.put(pos, new Item(ItemType.BOMBE));
                        break;
                    case "VITESSE":
                        hiddenItems.put(pos, new Item(ItemType.VITESSE));
                        break;
                    case "FEU":
                        hiddenItems.put(pos, new Item(ItemType.FEU));
                        break;
                    case "KICK":
                        hiddenItems.put(pos, new Item(ItemType.KICK));
                        break;
                    case "SKULL":
                        hiddenItems.put(pos, new Item(ItemType.SKULL));
                        break;
                }
            }
        }
    }



}