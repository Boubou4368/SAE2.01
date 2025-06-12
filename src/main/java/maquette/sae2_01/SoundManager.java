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
            Media backgroundMusicMedia = new Media(getClass().getResource("/maquette/sae2_01/sounds/songbomberman1.mp3").toString());
            backgroundMusic = new MediaPlayer(backgroundMusicMedia);
            backgroundMusic.setVolume(volume * 0.3); // Musique plus douce
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);

            // Charger les effets sonores
            loadSound("BombermanHome", "/maquette/sae2_01/sounds/songbomberman1.mp3");
            loadSound("explosion", "/maquette/sae2_01/sounds/sonbombe.wav"); // SON D'EXPLOSION AJOUTÉ
            loadSound("pickup", "/maquette/sae2_01/sounds/songitem.wav"); // SON D'ITEM AJOUTÉ
            loadSound("bomb_place", "/maquette/sae2_01/sounds/songplacebomb.wav"); // SON DE PLACEMENT DE BOMBE AJOUTÉ
            loadSound("death", "/maquette/sae2_01/sounds/death.wav"); // SON DE PLACEMENT DE MORT
            loadSound("kick", "/maquette/sae2_01/sounds/kick.mp3"); // SON DE PLACEMENT DE KICK
            loadSound("win", "/maquette/sae2_01/sounds/winner.mp3"); //SON WINNER
            loadSound("lose", "/maquette/sae2_01/sounds/loose.mp3"); //SON GAME OVER

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

            // Ajuster le volume spécifiquement pour certains sons
            if (soundName.equals("pickup")) {
                player.setVolume(Math.min(1.0, volume * 1.5)); // 50% plus fort pour les items
            } else if (soundName.equals("explosion")) {
                player.setVolume(Math.min(1.0, volume * 1.1)); // 10% plus fort pour les explosions
            } else if (soundName.equals("bomb_place")) {
                player.setVolume(Math.min(1.0, volume * 1.7)); // 70% plus fort pour le placement de bombe
            } else if (soundName.equals("death")) {
                player.setVolume(Math.min(1.0, volume * 3.0)); // 250% plus fort pour la mort
            } else if (soundName.equals("KICK")) {
                player.setVolume(Math.min(1.0, volume * 3.0)); // 250% plus fort pour le kick
            } else if (soundName.equals("lose")) {
                player.setVolume(Math.min(1.0, volume * 1.1)); // 10% musique
            }
            else if (soundName.equals("win")) {
                player.setVolume(Math.min(1.0, volume * 1.1)); // 10% musique
            }else {
                player.setVolume(volume);
            }

            player.play();
        } else {
            System.err.println("Son non trouvé: " + soundName);
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