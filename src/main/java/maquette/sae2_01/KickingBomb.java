package maquette.sae2_01;

public class KickingBomb extends BombermanController.Bomb {
    BombermanController.Position direction;
    int kickSpeed = 8; // Plus le nombre est grand, plus c'est lent
    int kickTimer = 0;

    KickingBomb(int x, int y, int owner, BombermanController.Position direction) {
        super(x, y, owner);
        this.direction = direction;
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
