package com.hardcoremario.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

/**
 * SoundManager handles all sound effects (SFX) and background music (BGM).
 * Loads real .wav audio files from assets/audio/sfx/ and assets/audio/music/.
 * Supports pre-cached clip pooling for zero latency, volume controls,
 * stage music looping, and fallback synthesis if files are missing.
 */
public class SoundManager {

    private static SoundManager instance;

    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private float sfxVolume = 0.85f;    // 0.0 to 1.0
    private float musicVolume = 0.70f;  // 0.0 to 1.0

    // SFX Clip Pools: soundKey -> ClipPool
    private final Map<String, ClipPool> sfxPools = new HashMap<>();

    // Background Music (BGM)
    private Clip currentMusicClip = null;
    private String currentMusicPath = null;
    private long musicPausePosition = 0;

    private SoundManager() {
        initAudio();
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Initializes and preloads all sound effects and prepares audio engine.
     */
    public synchronized void initAudio() {
        // Preload Sound Effects (SFX) with multi-channel clip pools
        loadSFX("shoot_player", "assets/audio/sfx/shoot_player.wav", 4);
        loadSFX("shoot_enemy",  "assets/audio/sfx/shoot_enemy.wav", 3);
        loadSFX("jump",         "assets/audio/sfx/jump.wav", 3);
        loadSFX("hit",          "assets/audio/sfx/hit.wav", 4);
        loadSFX("pickup",       "assets/audio/sfx/pickup.wav", 2);
        loadSFX("reload",       "assets/audio/sfx/reload.wav", 2);
        loadSFX("gameover",     "assets/audio/sfx/gameover.wav", 1);
        loadSFX("victory",      "assets/audio/sfx/victory.wav", 1);
    }

    /**
     * Reloads all audio files from disk (useful if player replaced files while running).
     */
    public synchronized void reloadAudio() {
        stopMusic();
        for (ClipPool pool : sfxPools.values()) {
            pool.close();
        }
        sfxPools.clear();
        initAudio();
    }

    private void loadSFX(String key, String filePath, int poolSize) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (filePath.endsWith(".wav")) {
                File mp3File = new File(filePath.substring(0, filePath.length() - 4) + ".mp3");
                if (mp3File.exists()) file = mp3File;
            }
        }
        if (file.exists()) {
            try {
                file = ensurePcmWav(file);
                ClipPool pool = new ClipPool(file, poolSize);
                sfxPools.put(key, pool);
            } catch (Exception e) {
                System.err.println("Warning: Could not load SFX " + filePath + ": " + e.getMessage());
            }
        }
    }

    private void playSFX(String key) {
        if (!soundEnabled) return;
        ClipPool pool = sfxPools.get(key);
        if (pool != null) {
            pool.playNext(sfxVolume);
        }
    }

    // ==========================================
    // Sound Effects Triggers
    // ==========================================

    public void playPlayerShoot() {
        if (sfxPools.containsKey("shoot_player")) {
            playSFX("shoot_player");
        } else {
            fallbackSynthPlayerShoot();
        }
    }

    public void playEnemyShoot() {
        if (sfxPools.containsKey("shoot_enemy")) {
            playSFX("shoot_enemy");
        } else {
            fallbackSynthEnemyShoot();
        }
    }

    public void playJump() {
        if (sfxPools.containsKey("jump")) {
            playSFX("jump");
        } else {
            fallbackSynthJump();
        }
    }

    public void playHit() {
        if (sfxPools.containsKey("hit")) {
            playSFX("hit");
        } else {
            fallbackSynthHit();
        }
    }

    public void playPickup() {
        if (sfxPools.containsKey("pickup")) {
            playSFX("pickup");
        } else {
            fallbackSynthPickup();
        }
    }

    public void playReload() {
        if (sfxPools.containsKey("reload")) {
            playSFX("reload");
        } else {
            fallbackSynthReload();
        }
    }

    public void playVictory() {
        stopMusic();
        if (sfxPools.containsKey("victory")) {
            playSFX("victory");
        } else {
            fallbackSynthVictory();
        }
    }

    public void playGameOver() {
        stopMusic();
        if (sfxPools.containsKey("gameover")) {
            playSFX("gameover");
        } else {
            fallbackSynthGameOver();
        }
    }

    // ==========================================
    // Background Music (BGM)
    // ==========================================

    /**
     * Plays background music corresponding to the stage number.
     * Looks for assets/audio/music/bgm_stage<N>.wav -> bgm_stage.wav -> bgm_main.wav.
     */
    public synchronized void playStageMusic(int stage) {
        if (!musicEnabled) return;

        String[] candidates = {
            "assets/audio/music/bgm_stage" + stage + ".wav",
            "assets/audio/music/bgm_stage" + stage + ".mp3",
            "assets/audio/music/bgm_stage.wav",
            "assets/audio/music/bgm_stage.mp3",
            "assets/audio/music/bgm_main.wav",
            "assets/audio/music/bgm_main.mp3"
        };

        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                playMusicFile(f.getPath(), true);
                return;
            }
        }
    }

    /**
     * Plays a specific music file with optional continuous looping.
     */
    public synchronized void playMusicFile(String path, boolean loop) {
        if (!musicEnabled) return;

        // If the exact music is already playing, do nothing
        if (currentMusicClip != null && currentMusicClip.isRunning() && path.equals(currentMusicPath)) {
            return;
        }

        stopMusic();

        File file = new File(path);
        if (!file.exists()) {
            if (path.endsWith(".wav")) {
                File mp3File = new File(path.substring(0, path.length() - 4) + ".mp3");
                if (mp3File.exists()) file = mp3File;
            }
        }
        if (!file.exists()) return;

        file = ensurePcmWav(file);

        try {
            AudioInputStream in = getPcmStream(file);
            AudioFormat format = in.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(in);
            in.close();

            applyGain(clip, musicVolume);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }

            this.currentMusicClip = clip;
            this.currentMusicPath = path;
            this.musicPausePosition = 0;
        } catch (Exception e) {
            System.err.println("Warning: Could not play music " + path + ": " + e.getMessage());
        }
    }

    public synchronized void stopMusic() {
        if (currentMusicClip != null) {
            try {
                currentMusicClip.stop();
                currentMusicClip.close();
            } catch (Exception ignored) {}
            currentMusicClip = null;
            currentMusicPath = null;
            musicPausePosition = 0;
        }
    }

    public synchronized void pauseMusic() {
        if (currentMusicClip != null && currentMusicClip.isRunning()) {
            musicPausePosition = currentMusicClip.getMicrosecondPosition();
            currentMusicClip.stop();
        }
    }

    public synchronized void resumeMusic() {
        if (musicEnabled && currentMusicClip != null && !currentMusicClip.isRunning()) {
            currentMusicClip.setMicrosecondPosition(musicPausePosition);
            currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // ==========================================
    // Settings & Volume Controls
    // ==========================================

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentMusicClip != null && currentMusicClip.isOpen()) {
            applyGain(currentMusicClip, musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    private static void applyGain(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                if (volume <= 0.0001f) {
                    gainControl.setValue(min);
                } else {
                    // Convert linear volume (0.0 to 1.0) to decibels dB
                    float dB = (float) (Math.log10(volume) * 20.0);
                    gainControl.setValue(Math.max(min, Math.min(max, dB)));
                }
            }
        } catch (Exception ignored) {}
    }

    private static AudioInputStream getPcmStream(File file) throws Exception {
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioFormat baseFormat = in.getFormat();
        if (baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && baseFormat.getSampleSizeInBits() == 16) {
            return in;
        }
        AudioFormat decodedFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.getSampleRate(),
            16,
            baseFormat.getChannels(),
            baseFormat.getChannels() * 2,
            baseFormat.getSampleRate(),
            false
        );
        return AudioSystem.getAudioInputStream(decodedFormat, in);
    }

    private static boolean isMp3(File file) {
        if (file.getName().toLowerCase().endsWith(".mp3")) return true;
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] header = new byte[3];
            int read = fis.read(header);
            if (read >= 3) {
                // "ID3" tag in MP3
                if (header[0] == 'I' && header[1] == 'D' && header[2] == '3') return true;
                // MPEG Frame sync 0xFF 0xFB, 0xFA, 0xF3, etc.
                if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static File ensurePcmWav(File file) {
        if (!isMp3(file)) return file;

        // Auto-transcode MP3 to cached WAV
        File cachedWav = new File(file.getParentFile(), "." + file.getName() + ".cached.wav");
        if (!cachedWav.exists() || cachedWav.lastModified() < file.lastModified()) {
            try {
                System.out.println("[SoundManager] Auto-decoding MP3 stream in: " + file.getName() + "...");
                javazoom.jl.converter.Converter conv = new javazoom.jl.converter.Converter();
                conv.convert(file.getAbsolutePath(), cachedWav.getAbsolutePath());
                System.out.println("[SoundManager] Decoded successfully -> " + cachedWav.getName());
            } catch (Throwable e) {
                System.err.println("[SoundManager] MP3 decoding failed: " + e.getMessage());
                return file;
            }
        }
        return cachedWav;
    }

    // ==========================================
    // ClipPool Helper Class for Zero-Latency SFX
    // ==========================================

    private static class ClipPool {
        private final Clip[] clips;
        private int nextIndex = 0;

        public ClipPool(File file, int size) throws Exception {
            this.clips = new Clip[Math.max(1, size)];
            byte[] audioBytes;
            AudioFormat format;

            try (AudioInputStream in = getPcmStream(file)) {
                format = in.getFormat();
                int frameSize = format.getFrameSize();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    baos.write(buf, 0, r);
                }
                audioBytes = baos.toByteArray();
            }

            for (int i = 0; i < clips.length; i++) {
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(format, audioBytes, 0, audioBytes.length);
                clips[i] = clip;
            }
        }

        public synchronized void playNext(float volume) {
            Clip clip = clips[nextIndex];
            nextIndex = (nextIndex + 1) % clips.length;

            if (clip != null && clip.isOpen()) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                applyGain(clip, volume);
                clip.setFramePosition(0);
                clip.start();
            }
        }

        public synchronized void close() {
            for (Clip clip : clips) {
                if (clip != null) {
                    try {
                        clip.stop();
                        clip.close();
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    // ==========================================
    // Procedural Fallback Synthesis (Only used if WAV files are missing)
    // ==========================================

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

    private void fallbackSynthPlayerShoot() {
        int sampleRate = 22050, durationMs = 90;
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

    private void fallbackSynthEnemyShoot() {
        int sampleRate = 22050, durationMs = 80;
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

    private void fallbackSynthJump() {
        int sampleRate = 22050, durationMs = 120;
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

    private void fallbackSynthHit() {
        int sampleRate = 22050, durationMs = 70;
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

    private void fallbackSynthPickup() {
        int sampleRate = 22050, durationMs = 150;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / numSamples;
            double freq = t < 0.5 ? 587.33 : 880.0;
            double sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate);
            buffer[i] = (byte) (sample * (1.0 - t) * 85.0);
        }
        playTone(buffer, new AudioFormat(sampleRate, 8, 1, true, false));
    }

    private void fallbackSynthReload() {
        int sampleRate = 22050, durationMs = 180;
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

    private void fallbackSynthVictory() {
        new Thread(() -> {
            playToneNote(523.25, 120);
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(659.25, 120);
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(783.99, 120);
            try { Thread.sleep(130); } catch (Exception ignored) {}
            playToneNote(1046.50, 300);
        }).start();
    }

    private void fallbackSynthGameOver() {
        new Thread(() -> {
            playToneNote(392.00, 150);
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(349.23, 150);
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(329.63, 150);
            try { Thread.sleep(160); } catch (Exception ignored) {}
            playToneNote(261.63, 400);
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
