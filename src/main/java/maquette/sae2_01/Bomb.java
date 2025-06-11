package maquette.sae2_01;

public class Bomb {
    private Position pos;
    private int timer = 120;
    private int owner; // 1-4 pour les joueurs

    public Bomb(int x, int y, int owner) {
        this.pos = new Position(x, y);
        this.owner = owner;
    }

    public boolean updateTimer() {
        timer--;
        return timer <= 0;
    }

    public Position getPos() {
        return pos;
    }
    public void setPos(Position pos) {
        this.pos = pos;
    }

    public int getOwner() {
        return owner;
    }

    public int getTimer() {
        return timer;
    }
    public void setTimer(int timer) {
        this.timer = timer;
    }
    @Override
    public String toString() {
        return "Bomb{pos=" + pos + ", timer=" + timer + ", owner=" + owner + "}";
    }
}
