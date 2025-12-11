// src/main/Sound.java
package main;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Sound {
    private Clip clip;
    private FloatControl volumeControl;

    // Sound effect indices
    public static final int MOVE = 0;
    public static final int CAPTURE = 1;
    public static final int CASTLE = 2;
    public static final int CHECK = 3;
    public static final int GAME_START = 4;
    public static final int GAME_END = 5;
    public static final int PROMOTE = 6;
    public static final int ILLEGAL = 7;
    public static final int MAGIC=8;
    public static final int SLIDE1=9;
    public static final int SLIDE2=10;
    public static final int SLIDE3=11;

    private static final String[] soundFiles = {
            "/SoundEffect/move-self.wav",      // Normal move
            "/soundEffect/capture.wav",   // Capture piece
            "/SoundEffect/bruh.wav",
            "/SoundEffect/bruh.wav",
            "/SoundEffect/bruh.wav",
            "/SoundEffect/bruh.wav",
            "/SoundEffect/bruh.wav",
            "/SoundEffect/bruh.wav",
            "/SoundEffect/Magic.wav",
            "/SoundEffect/Slide/slide1.wav",
            "/SoundEffect/Slide/slide2.wav",
            "/SoundEffect/Slide/slide3.wav",

            /*"/sound/castle.wav",    // Castling
            "/sound/check.wav",     // Check
            "/sound/start.wav",     // Game start
            "/sound/end.wav",       // Game end
            "/sound/promote.wav",   // Promotion
            "/sound/illegal.wav"    // Illegal move*/
    };

    public Sound() {
        // Constructor - clips are loaded on demand
    }

    /**
     * Sets the sound file to be played
     * @param index The sound effect index (use static constants)
     */
    public void setFile(int index) {
        try {
            if (index < 0 || index >= soundFiles.length) {
                return;
            }

            URL url = getClass().getResource(soundFiles[index]);
            if (url == null) {
                System.err.println("Sound file not found: " + soundFiles[index]);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Get volume control if available
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + soundFiles[index]);
        } catch (IOException e) {
            System.err.println("Error loading sound: " + soundFiles[index]);
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable for: " + soundFiles[index]);
        }
    }

    /**
     * Plays the loaded sound effect
     */
    public void play() {
        if (clip == null) return;

        clip.setFramePosition(0);
        clip.start();
    }

    /**
     * Loops the sound continuously
     */
    public void loop() {
        if (clip == null) return;

        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Stops the currently playing sound
     */
    public void stop() {
        if (clip == null) return;

        clip.stop();
    }

    /**
     * Sets the volume of the sound
     * @param volume Value between 0.0 (silent) and 1.0 (max volume)
     */
    public void setVolume(float volume) {
        if (volumeControl == null) return;

        // Allow volume up to 2.0 (200%)
        volume = Math.max(0.0f, Math.min(2.0f, volume));

        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();

        // Map 0.0-2.0 to min-max range with amplification
        float gain;
        if (volume <= 1.0f) {
            // Normal range: 0.0 to 1.0 maps to min to 0dB
            gain = min + ((0 - min) * volume);
        } else {
            // Amplified range: 1.0 to 2.0 maps to 0dB to max
            gain = (volume - 1.0f) * max;
        }

        volumeControl.setValue(gain);
    }

    /**
     * Closes and releases resources
     */
    public void close() {
        if (clip != null) {
            clip.close();
        }
    }

    /**
     * Quick method to play a sound effect once
     * @param index The sound effect to play
     */
    public static void playSound(int index) {
        Sound sound = new Sound();
        sound.setFile(index);
        sound.play();
    }
}