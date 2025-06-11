package maquette.sae2_01;

public class KickingBomb extends Bomb {
    private Position direction;
    private int kickSpeed = 8; // Plus le nombre est grand, plus c'est lent
    private int kickTimer = 0;

    public KickingBomb(int x, int y, int owner, Position direction) {
        super(x, y, owner);
        this.direction = direction;
    }

    public Position getDirection() {
        return direction;
    }

    public int getKickTimer(){
        return kickTimer;
    }
    public void setKickTimer(int kickTimer){
        this.kickTimer = kickTimer;
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
