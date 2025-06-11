package maquette.sae2_01;

public class Explosion {
    public Position pos;
    public int timer = 30;

    public Explosion(int x, int y) { this.pos = new Position(x, y); }

    public boolean updateTimer() {
        timer--;
        return timer <= 0;
    }

    public double getIntensity() {
        return (double) timer / 30;
    }

    @Override
    public String toString() {
        return "Explosion{pos=" + pos + ", timer=" + timer + "}";
    }

    public boolean containsPosition(Position myPos) {
        return pos.equals(myPos);
    }
}
