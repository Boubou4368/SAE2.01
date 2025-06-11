// SoundManager.java - Classe pour gérer tous les sons du jeu
package maquette.sae2_01;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, MediaPlayer> sounds;
    private MediaPlayer backgroundMusic;
    private boolean soundEnabled = true;
    private double volume = 0.5; // Volume par défaut

    private SoundManager() {
        sounds = new HashMap<>();
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        try {
            // Charger la musique de fond
            Media backgroundMusicMedia = new Media(getClass().getResource("/maquette/sae2_01/sounds/BombermanHome.mp3").toString());
            backgroundMusic = new MediaPlayer(backgroundMusicMedia);
            backgroundMusic.setVolume(volume * 0.3); // Musique plus douce
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);

            // Charger les effets sonores
            loadSound("BombermanHome", "/maquette/sae2_01/sounds/BombermanHome.wav");
//            loadSound("explosion", "/maquette/sae2_01/sounds/explosion.wav");
//            loadSound("pickup", "/maquette/sae2_01/sounds/pickup.wav");
//            loadSound("death", "/maquette/sae2_01/sounds/death.wav");
//            loadSound("win", "/maquette/sae2_01/sounds/win.wav");
//            loadSound("lose", "/maquette/sae2_01/sounds/lose.wav");
//            loadSound("kick", "/maquette/sae2_01/sounds/kick.wav");
//            loadSound("walk", "/maquette/sae2_01/sounds/walk.wav");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sons: " + e.getMessage());
        }
    }

    private void loadSound(String soundName, String filePath) {
        try {
            Media media = new Media(getClass().getResource(filePath).toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(volume);
            sounds.put(soundName, player);
        } catch (Exception e) {
            System.err.println("Impossible de charger le son: " + soundName + " - " + e.getMessage());
        }
    }

    public void playSound(String soundName) {
        if (!soundEnabled) return;

        MediaPlayer player = sounds.get(soundName);
        if (player != null) {
            // Arrêter le son s'il joue déjà et le remettre au début
            player.stop();
            player.seek(Duration.ZERO);
            player.play();
        }
    }

    public void startBackgroundMusic() {
        if (soundEnabled && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void pauseBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    public void resumeBackgroundMusic() {
        if (soundEnabled && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));

        // Mettre à jour le volume de tous les sons
        for (MediaPlayer player : sounds.values()) {
            player.setVolume(this.volume);
        }

        if (backgroundMusic != null) {
            backgroundMusic.setVolume(this.volume * 0.3);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        } else {
            startBackgroundMusic();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public double getVolume() {
        return volume;
    }

    public void dispose() {
        // Libérer les ressources
        for (MediaPlayer player : sounds.values()) {
            player.dispose();
        }
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }
}

// Modifications à apporter dans BombermanController.java
// Ajoutez ces lignes au début de votre classe BombermanController :

/*
private SoundManager soundManager;

// Dans la méthode initialize(), ajoutez :
soundManager = SoundManager.getInstance();
soundManager.startBackgroundMusic();

// Dans la méthode placeBomb(), ajoutez :
soundManager.playSound("bomb_place");

// Dans la méthode explodeBomb(), ajoutez :
soundManager.playSound("explosion");

// Dans la méthode checkItemPickupForPlayer(), ajoutez dans chaque case :
switch (item.type) {
    case FEU:
        player.bombRange++;
        player.feuBonusCount++;
        player.score += 50;
        soundManager.playSound("pickup"); // AJOUT
        break;
    case VITESSE:
        player.speedMultiplier += 0.3;
        player.vitesseBonusCount++;
        player.score += 30;
        soundManager.playSound("pickup"); // AJOUT
        break;
    case BOMBE:
        player.maxBombs++;
        player.bombsRemaining++;
        player.bombeBonusCount++;
        player.score += 40;
        soundManager.playSound("pickup"); // AJOUT
        break;
    case SKULL:
        player.lives--;
        soundManager.playSound("death"); // AJOUT
        // ... reste du code
        break;
    case KICK:
        if (player.kickBonusCount < 2) {
            player.kickBonusCount++;
            player.canKick = true;
            player.score += 60;
            soundManager.playSound("pickup"); // AJOUT
        }
        break;
}

// Dans la méthode kickBomb(), ajoutez :
soundManager.playSound("kick");

// Dans la méthode gameOver(), ajoutez :
if (message.contains("gagne")) {
    soundManager.playSound("win");
} else {
    soundManager.playSound("lose");
}
soundManager.stopBackgroundMusic();

// Dans la méthode handlePlayerInput(), pour les sons de pas :
if (moved && canMoveTo(newPos)) {
    player.pos = newPos;
    soundManager.playSound("walk");
    // ... reste du code
}
*/
