package maquette.sae2_01;

public class Player {
    public Position pos;
    public int bombsRemaining = 3;
    public int maxBombs = 3;
    public int bombRange = 2;
    public int lives = 3;
    boolean isAlive = true;
    double speedMultiplier = 1.0;
    int score = 0;

    // Compteurs de bonus
    int feuBonusCount = 0;
    int vitesseBonusCount = 0;
    int bombeBonusCount = 0;
    int kickBonusCount = 0;
    boolean canKick = false;

    // Nouvelles variables pour l'invincibilité
    boolean isInvulnerable = false;
    long invulnerabilityEndTime = 0;

    public Player(int x, int y) { this.pos = new Position(x, y); }

    public void resetBonuses() {
        this.bombRange = 2;
        this.speedMultiplier = 1.0;
        this.maxBombs = 3;
        this.bombsRemaining = 3;
        this.feuBonusCount = 0;
        this.vitesseBonusCount = 0;
        this.bombeBonusCount = 0;
    }

    public void applyItem(Item item) {
        switch (item.type) {
            case FEU:
                this.bombRange++;
                this.feuBonusCount++;
                this.score += 50;
                break;
            case VITESSE:
                this.speedMultiplier += 0.3;
                this.vitesseBonusCount++;
                this.score += 30;
                break;
            case BOMBE:
                this.maxBombs++;
                this.bombsRemaining++;
                this.bombeBonusCount++;
                this.score += 40;
                break;
            case SKULL:
                this.lives--;
                if (this.lives > 0) {
                    resetBonuses();
                }
                break;
        }
    }

    @Override
    public String toString() {
        return "Player{pos=" + pos + ", lives=" + lives + ", isAlive=" + isAlive +
                ", bombsRemaining=" + bombsRemaining + ", score=" + score + "}";
    }
}
