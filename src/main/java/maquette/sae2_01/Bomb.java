package maquette.sae2_01;

public class Bomb {
    public Position pos;
    public int timer = 120;
    public int owner; // 1-4 pour les joueurs

    public Bomb(int x, int y, int owner) {
        this.pos = new Position(x, y);
        this.owner = owner;
    }

    public boolean updateTimer() {
        timer--;
        return timer <= 0;
    }

    @Override
    public String toString() {
        return "Bomb{pos=" + pos + ", timer=" + timer + ", owner=" + owner + "}";
    }
}
