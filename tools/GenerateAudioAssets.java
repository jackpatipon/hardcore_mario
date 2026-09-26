package tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Generates sample high-quality 16-bit 44.1kHz Stereo .wav audio files
 * for all game sound effects and background music tracks.
 * Players can replace these .wav files with their own custom audio at any time.
 */
public class GenerateAudioAssets {

    private static final float SAMPLE_RATE = 44100.0f;

    public static void main(String[] args) {
        System.out.println("Generating template .wav audio assets in assets/audio/...");

        new File("assets/audio/sfx").mkdirs();
        new File("assets/audio/music").mkdirs();

        // 1. Sound Effects (SFX)
        writeWav("assets/audio/sfx/shoot_player.wav", generatePlayerShoot());
        writeWav("assets/audio/sfx/shoot_enemy.wav", generateEnemyShoot());
        writeWav("assets/audio/sfx/jump.wav", generateJump());
        writeWav("assets/audio/sfx/hit.wav", generateHit());
        writeWav("assets/audio/sfx/pickup.wav", generatePickup());
        writeWav("assets/audio/sfx/reload.wav", generateReload());
        writeWav("assets/audio/sfx/gameover.wav", generateGameOver());
        writeWav("assets/audio/sfx/victory.wav", generateVictory());

        // 2. Background Music (BGM)
        writeWav("assets/audio/music/bgm_stage1.wav", generateStageBGM(1));
        writeWav("assets/audio/music/bgm_stage2.wav", generateStageBGM(2));
        writeWav("assets/audio/music/bgm_stage3.wav", generateStageBGM(3));
        writeWav("assets/audio/music/bgm_main.wav", generateStageBGM(1));

        System.out.println("[SUCCESS] All .wav audio assets generated successfully!");
    }

    private static byte[] generatePlayerShoot() {
        int durationMs = 220;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double decay = Math.pow(1.0 - t, 2.5);
            // Punchy kick thump (140Hz down to 45Hz) + metallic explosion noise
            double freq = 140.0 * (1.0 - t * 0.7);
            double sin = Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
            double noise = (Math.random() * 2.0 - 1.0);
            double sample = (sin * 0.6 + noise * 0.4) * decay;
            short val = (short) (Math.max(-1.0, Math.min(1.0, sample)) * 28000);
            left[i] = val;
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateEnemyShoot() {
        int durationMs = 180;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double decay = Math.pow(1.0 - t, 3.0);
            // High sharp laser/rifle crack (600Hz down to 120Hz)
            double freq = 600.0 - 450.0 * Math.sqrt(t);
            double wave = Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
            double noise = (Math.random() * 2.0 - 1.0) * 0.3;
            double sample = (wave * 0.7 + noise) * decay;
            short val = (short) (Math.max(-1.0, Math.min(1.0, sample)) * 26000);
            left[i] = (short) (val * 0.85); // Slight stereo pan
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateJump() {
        int durationMs = 150;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double decay = 1.0 - t * 0.6;
            // Smooth upward pitch sweep 160Hz -> 480Hz
            double freq = 160.0 + 320.0 * Math.pow(t, 0.8);
            double sample = Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE) * decay;
            short val = (short) (sample * 24000);
            left[i] = val;
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateHit() {
        int durationMs = 140;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double decay = Math.pow(1.0 - t, 2.0);
            // Heavy impact thud with crunchy noise
            double freq = 90.0 * (1.0 - t * 0.5);
            double sub = Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
            double noise = (Math.random() * 2.0 - 1.0);
            double sample = (sub * 0.65 + noise * 0.35) * decay;
            short val = (short) (Math.max(-1.0, Math.min(1.0, sample)) * 27000);
            left[i] = val;
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generatePickup() {
        int durationMs = 280;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double decay = Math.pow(1.0 - t, 1.2);
            // 3-step bright arpeggio: 587.3Hz (D5) -> 880.0Hz (A5) -> 1174.6Hz (D6)
            double freq = (t < 0.33) ? 587.33 : (t < 0.66 ? 880.00 : 1174.66);
            double sine1 = Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
            double sine2 = Math.sin(4.0 * Math.PI * freq * i / SAMPLE_RATE) * 0.3; // octave shimmer
            double sample = (sine1 + sine2) * decay * 0.8;
            short val = (short) (sample * 24000);
            left[i] = (short) (val * (0.8 + 0.2 * Math.sin(t * Math.PI * 4)));
            right[i] = (short) (val * (0.8 - 0.2 * Math.sin(t * Math.PI * 4)));
        }
        return packStereo16(left, right);
    }

    private static byte[] generateReload() {
        int durationMs = 320;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / totalSamples;
            double sample = 0;

            // Click 1: Magazine eject at t ~ 0.15
            if (t >= 0.10 && t < 0.20) {
                double dt = (t - 0.10) / 0.10;
                double decay = Math.pow(1.0 - dt, 3.0);
                sample += ((Math.random() * 2.0 - 1.0) * 0.6 + Math.sin(2.0 * Math.PI * 1800.0 * i / SAMPLE_RATE) * 0.4) * decay;
            }
            // Click 2: Bolt chamber slide at t ~ 0.60
            if (t >= 0.55 && t < 0.75) {
                double dt = (t - 0.55) / 0.20;
                double decay = Math.pow(1.0 - dt, 2.5);
                sample += ((Math.random() * 2.0 - 1.0) * 0.7 + Math.sin(2.0 * Math.PI * 1400.0 * i / SAMPLE_RATE) * 0.3) * decay;
            }

            short val = (short) (Math.max(-1.0, Math.min(1.0, sample)) * 25000);
            left[i] = val;
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateGameOver() {
        int durationMs = 1200;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        // 4 descending minor notes
        double[] notes = {392.00, 349.23, 311.13, 261.63}; // G4, F4, Eb4, C4
        int noteSamples = totalSamples / 4;

        for (int i = 0; i < totalSamples; i++) {
            int noteIndex = Math.min(3, i / noteSamples);
            int localI = i % noteSamples;
            double dt = (double) localI / noteSamples;
            double decay = Math.pow(1.0 - dt, 1.3);
            double freq = notes[noteIndex];
            double sample = (Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE) * 0.7
                           + Math.sin(4.0 * Math.PI * freq * i / SAMPLE_RATE) * 0.3) * decay;
            short val = (short) (sample * 24000);
            left[i] = val;
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateVictory() {
        int durationMs = 1800;
        int totalSamples = (int) (SAMPLE_RATE * durationMs / 1000.0f);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        // Fanfare: C5, E5, G5, High C6
        double[] notes = {523.25, 659.25, 783.99, 1046.50};
        int noteLen = (int) (SAMPLE_RATE * 0.28);

        for (int i = 0; i < totalSamples; i++) {
            double sample = 0;
            if (i < noteLen * 3) {
                int noteIndex = i / noteLen;
                int localI = i % noteLen;
                double dt = (double) localI / noteLen;
                double decay = Math.pow(1.0 - dt, 1.2);
                double freq = notes[noteIndex];
                sample = (Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE) * 0.7
                        + Math.sin(4.0 * Math.PI * freq * i / SAMPLE_RATE) * 0.3) * decay;
            } else {
                // Final C6 sustained chord
                int localI = i - noteLen * 3;
                int remain = totalSamples - noteLen * 3;
                double dt = (double) localI / remain;
                double decay = Math.pow(1.0 - dt, 1.1);
                double c6 = Math.sin(2.0 * Math.PI * 1046.50 * i / SAMPLE_RATE) * 0.5;
                double g5 = Math.sin(2.0 * Math.PI * 783.99 * i / SAMPLE_RATE) * 0.3;
                double e5 = Math.sin(2.0 * Math.PI * 659.25 * i / SAMPLE_RATE) * 0.2;
                sample = (c6 + g5 + e5) * decay;
            }
            short val = (short) (Math.max(-1.0, Math.min(1.0, sample)) * 25000);
            left[i] = (short) (val * 0.95);
            right[i] = val;
        }
        return packStereo16(left, right);
    }

    private static byte[] generateStageBGM(int stage) {
        // 6.0 seconds loopable rhythmic electronic battle music track
        double durationSec = 6.0;
        int totalSamples = (int) (SAMPLE_RATE * durationSec);
        short[] left = new short[totalSamples];
        short[] right = new short[totalSamples];

        // 130 BPM = ~0.4615 sec per beat, 16 beats total
        double beatSec = 60.0 / 130.0;
        int samplesPerBeat = (int) (SAMPLE_RATE * beatSec);

        // Bassline frequencies per stage
        double[] bassNotes;
        if (stage == 1) {
            bassNotes = new double[]{130.81, 130.81, 155.56, 174.61}; // C3, C3, Eb3, F3
        } else if (stage == 2) {
            bassNotes = new double[]{110.00, 110.00, 123.47, 146.83}; // A2, A2, B2, D3
        } else {
            bassNotes = new double[]{146.83, 146.83, 174.61, 196.00}; // D3, D3, F3, G3
        }

        for (int i = 0; i < totalSamples; i++) {
            double timeSec = (double) i / SAMPLE_RATE;
            int beatIndex = (int) (timeSec / beatSec) % 16;
            double beatFraction = (timeSec % beatSec) / beatSec;

            // 1. Kick Drum on every beat (beats 0, 1, 2, 3...)
            double kick = 0;
            if (beatFraction < 0.25) {
                double kt = beatFraction / 0.25;
                double kFreq = 120.0 * Math.pow(1.0 - kt, 3.0) + 40.0;
                kick = Math.sin(2.0 * Math.PI * kFreq * (beatFraction * beatSec)) * (1.0 - kt) * 0.45;
            }

            // 2. Snare / Clack on beats 1, 3, 5, 7, 9, 11, 13, 15
            double snare = 0;
            if ((beatIndex % 2 == 1) && beatFraction < 0.20) {
                double st = beatFraction / 0.20;
                double noise = (Math.random() * 2.0 - 1.0) * (1.0 - st) * 0.35;
                snare = noise;
            }

            // 3. Hi-Hat rhythm (every eighth note)
            double hatFraction = (timeSec % (beatSec / 2.0)) / (beatSec / 2.0);
            double hat = 0;
            if (hatFraction < 0.08) {
                double ht = hatFraction / 0.08;
                hat = (Math.random() * 2.0 - 1.0) * (1.0 - ht) * 0.15;
            }

            // 4. Synth Bass (16th note rhythm)
            int chordIndex = (beatIndex / 4) % bassNotes.length;
            double bassFreq = bassNotes[chordIndex];
            double bassSubFrac = (timeSec % (beatSec / 4.0)) / (beatSec / 4.0);
            double bassEnv = Math.pow(1.0 - bassSubFrac, 1.8);
            double bass = (Math.sin(2.0 * Math.PI * bassFreq * timeSec) * 0.6
                        + Math.sin(4.0 * Math.PI * bassFreq * timeSec) * 0.25) * bassEnv * 0.35;

            // 5. Arp synth / Lead melodic accents
            double lead = 0;
            if (stage >= 2) {
                double leadFreq = bassFreq * 2.0;
                if (beatIndex % 4 == 2 || beatIndex % 4 == 3) {
                    leadFreq *= 1.5; // fifth
                }
                lead = Math.sin(2.0 * Math.PI * leadFreq * timeSec) * 0.12 * Math.sin(timeSec * Math.PI * 4);
            }

            double totalL = (kick + snare + hat * 0.8 + bass + lead * 0.7);
            double totalR = (kick + snare + hat * 1.2 + bass + lead * 1.1);

            // Subtle loop crossfade at beginning and end for seamless loop
            double fade = 1.0;
            int fadeSamples = (int) (SAMPLE_RATE * 0.02); // 20ms fade
            if (i < fadeSamples) fade = (double) i / fadeSamples;
            if (i > totalSamples - fadeSamples) fade = (double) (totalSamples - i) / fadeSamples;

            totalL *= fade;
            totalR *= fade;

            left[i] = (short) (Math.max(-1.0, Math.min(1.0, totalL)) * 22000);
            right[i] = (short) (Math.max(-1.0, Math.min(1.0, totalR)) * 22000);
        }

        return packStereo16(left, right);
    }

    private static byte[] packStereo16(short[] left, short[] right) {
        byte[] bytes = new byte[left.length * 4];
        for (int i = 0; i < left.length; i++) {
            short l = left[i];
            short r = right[i];
            // Little-endian stereo: L_lo, L_hi, R_lo, R_hi
            bytes[i * 4] = (byte) (l & 0xFF);
            bytes[i * 4 + 1] = (byte) ((l >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) (r & 0xFF);
            bytes[i * 4 + 3] = (byte) ((r >> 8) & 0xFF);
        }
        return bytes;
    }

    private static void writeWav(String outputPath, byte[] audioBytes) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioInputStream ais = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
            File outFile = new File(outputPath);
            outFile.getParentFile().mkdirs();
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
            System.out.println("  + Created: " + outputPath + " (" + (audioBytes.length / 1024) + " KB)");
        } catch (Exception e) {
            System.err.println("Error writing " + outputPath + ": " + e.getMessage());
        }
    }
}
