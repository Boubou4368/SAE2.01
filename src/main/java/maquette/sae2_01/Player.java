package maquette.sae2_01;

public class Player {
    private Position pos;
    private int bombsRemaining = 3;
    private int maxBombs = 3;
    private int bombRange = 2;
    private int lives = 3;
    private boolean isAlive = true;
    private double speedMultiplier = 1.0;

    // Compteurs de bonus
    private int feuBonusCount = 0;
    private int vitesseBonusCount = 0;
    private int bombeBonusCount = 0;
    private int kickBonusCount = 0;
    private boolean canKick = false;

    // Nouvelles variables pour l'invincibilité
    private boolean isInvulnerable = false;
    private long invulnerabilityEndTime = 0;
    private boolean isBlinking = false;
    private boolean isBot = false;


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
        switch (item.getType()) {
            case FEU:
                this.bombRange++;
                this.feuBonusCount++;
                break;
            case VITESSE:
                this.speedMultiplier += 0.3;
                this.vitesseBonusCount++;
                break;
            case BOMBE:
                this.maxBombs++;
                this.bombsRemaining++;
                this.bombeBonusCount++;
                break;
            case SKULL:
                this.lives--;
                if (this.lives > 0) {
                    resetBonuses();
                }
                break;
        }
    }

    public boolean getisAlive() { return isAlive; }

    public void setisAlive(boolean isAlive) { this.isAlive = isAlive; }
    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public boolean getcanKick() { return canKick; }
    public void setcanKick(boolean canKick) { this.canKick = canKick; }

    public int getBombsRemaining(){
        return bombsRemaining;
    }
    public void setBombsRemaining(int Bombremaining){
        this.bombsRemaining = Bombremaining;
    }

    public int getBombRange(){
        return bombRange;
    }
    public void setBombRange(int bombRange) {
        this.bombRange = bombRange;
    }

    public int getFeuBonusCount(){
        return feuBonusCount;
    }
    public void setFeuBonusCount(int feuBonusCount){
        this.feuBonusCount = feuBonusCount;
    }

    public double getSpeedMultiplier(){
        return speedMultiplier;
    }
    public void setSpeedMultiplier(double speedMultiplier){
        this.speedMultiplier = speedMultiplier;
    }

    public int getVitesseBonusCount(){
        return vitesseBonusCount;
    }
    public void setVitesseBonusCount(int vitesseBonusCount){
        this.vitesseBonusCount = vitesseBonusCount;
    }

    public int getMaxBombs(){
        return maxBombs;
    }
    public void setMaxBombs(int maxBomb){
        this.maxBombs = maxBomb;
    }

    public int getBombeBonusCount(){
        return bombeBonusCount;
    }
    public void setBombeBonusCount(int bombeBonusCount){}

    public int getKickBonusCount(){
        return kickBonusCount;
    }
    public void setKickBonusCount(int kickBonusCount){
        this.kickBonusCount = kickBonusCount;
    }

    public boolean getIsInvulnerable() {
        return isInvulnerable;
    }
    public void setIsInvulnerable(boolean isInvulnerable) {
        this.isInvulnerable = isInvulnerable;
    }
    public long getInvulnerabilityEndTime() {
        return invulnerabilityEndTime;
    }
    public void setInvulnerabilityEndTime(long invulnerabilityEndTime) {
        this.invulnerabilityEndTime = invulnerabilityEndTime;
    }

    public boolean isBlinking() { return isBlinking; }
    public void setBlinking(boolean blinking) { this.isBlinking = blinking; }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        this.isBot = bot;
    }
    @Override
    public String toString() {
        return "Player{pos=" + pos + ", lives=" + lives + ", isAlive=" + isAlive +
                ", bombsRemaining=" + bombsRemaining + "}";
    }
}
