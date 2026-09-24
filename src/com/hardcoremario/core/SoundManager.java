package com.hardcoremario.core;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.*;

/**
 * Built-in audio synthesizer using standard Java Sound API (javax.sound.sampled).
 * Generates crisp retro sound effects purely in memory without external audio files.
 */
public class SoundManager {
    private static SoundManager instance;
    private boolean soundEnabled = true;

    private SoundManager() {}

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    private void playTone(final byte[] pcmData, final AudioFormat format) {
        if (!soundEnabled) return;
        new Thread(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                line.write(pcmData, 0, pcmData.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }).start();
    }

    public void playPlayerShoot() {
        // Crisp gunshot with rapid noise decay
        int sampleRate = 22050;
        int durationMs = 90;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double decay = 1.0 - ((double) i / numSamples);
            double noise = (Math.random() * 2.0 - 1.0) * 0.7;
            double tone = Math.sin(2.0 * Math.PI * (350.0 * decay) * i / sampleRate) * 0.3;
            buffer[i] = (byte) ((noise + tone) * decay * 120.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playEnemyShoot() {
        // Laser-style zap
        int sampleRate = 22050;
        int durationMs = 80;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double freq = 700.0 - 500.0 * t;
            double sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate);
            buffer[i] = (byte) (sample * (1.0 - t) * 90.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playJump() {
        // Upward pitch sweep
        int sampleRate = 22050;
        int durationMs = 120;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double freq = 200.0 + 350.0 * t;
            double sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate);
            buffer[i] = (byte) (sample * (1.0 - t * 0.5) * 80.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playHit() {
        // Dull thud
        int sampleRate = 22050;
        int durationMs = 70;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double noise = (Math.random() * 2.0 - 1.0) * 0.5;
            double tone = Math.sin(2.0 * Math.PI * 120.0 * i / sampleRate) * 0.5;
            buffer[i] = (byte) ((noise + tone) * (1.0 - t) * 100.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playPickup() {
        // Upward arpeggio chime
        int sampleRate = 22050;
        int durationMs = 150;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double freq = t < 0.5 ? 587.33 : 880.0; // D5 to A5
            double sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate);
            buffer[i] = (byte) (sample * (1.0 - t) * 85.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playReload() {
        // Mechanical click click
        int sampleRate = 22050;
        int durationMs = 180;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double burst = (t < 0.2 || (t > 0.4 && t < 0.6)) ? 1.0 : 0.05;
            double noise = (Math.random() * 2.0 - 1.0) * burst;
            buffer[i] = (byte) (noise * 80.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    public void playVictory() {
        // Fanfare chord progression
        new Thread(() -> {
            playToneNote(523.25, 120); // C5
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(659.25, 120); // E5
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(783.99, 120); // G5
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(1046.50, 300); // C6
        }).start();
    }

    public void playGameOver() {
        // Sad downward tone
        new Thread(() -> {
            playToneNote(392.00, 150); // G4
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(349.23, 150); // F4
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(329.63, 150); // E4
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(261.63, 400); // C4
        }).start();
    }

    private void playToneNote(double freq, int durationMs) {
        int sampleRate = 22050;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate);
            buffer[i] = (byte) (sample * (1.0 - t * 0.3) * 90.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }
}
