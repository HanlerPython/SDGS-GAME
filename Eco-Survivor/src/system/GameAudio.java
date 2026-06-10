package system;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameAudio {
    
    private static Clip backgroundMusic;
    private static FloatControl volumeControl;
    
    // 🔥 修改：改為儲存多個 Clip 實例（支援疊加播放）
    private static List<Clip> activeSounds = new ArrayList<>();
    
    public static void playBackgroundMusic(String filePath, int volumePercent) {
        stopBackgroundMusic();
        
        try {
            javax.sound.sampled.AudioInputStream audioStream = null;
            
            URL url = GameAudio.class.getClassLoader().getResource(filePath);
            if (url != null) {
                audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            } else {
                File file = new File("res/" + filePath);
                if (!file.exists()) {
                    System.err.println("【GameAudio】找不到音樂檔案: " + filePath);
                    return;
                }
                audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            }
            
            backgroundMusic = javax.sound.sampled.AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            
            updateVolume(volumePercent);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            
            System.out.println("【GameAudio】背景音樂播放中: " + filePath);
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("【GameAudio】不支援的音訊格式: " + filePath);
        } catch (IOException e) {
            System.err.println("【GameAudio】讀取音樂檔案失敗: " + filePath);
        } catch (LineUnavailableException e) {
            System.err.println("【GameAudio】音訊線路不可用");
        }
    }
    
    /**
     * 🔥 播放音效（支援重複播放、疊加）
     * 每次呼叫都會建立新的 Clip 實例，可以同時播放多個
     */
    public static void playSound(String soundFileName) {
        playSound(soundFileName, VolumeManager.getVolume());
    }
    
    public static void playSound(String soundFileName, int volumePercent) {
        try {
            // 🔥 每次都建立新的 Clip（不共用，才能疊加）
            URL url = GameAudio.class.getClassLoader().getResource(soundFileName);
            javax.sound.sampled.AudioInputStream audioStream = null;
            
            if (url != null) {
                audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            } else {
                File file = new File("res/" + soundFileName);
                if (!file.exists()) {
                    System.err.println("【GameAudio】找不到音效檔案: " + soundFileName);
                    return;
                }
                audioStream = javax.sound.sampled.AudioSystem.getAudioInputStream(file);
            }
            
            Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioStream);
            
            // 設定音量
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB;
                if (volumePercent <= 0) {
                    dB = -80.0f;
                } else {
                    dB = -40.0f * (1.0f - volumePercent / 100.0f);
                    dB = Math.max(-80.0f, Math.min(0f, dB));
                }
                gainControl.setValue(dB);
            }
            
            // 添加到活躍列表
            activeSounds.add(clip);
            
            // 播放完成後自動清理
            clip.addLineListener(event -> {
                if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close();
                    activeSounds.remove(clip);
                }
            });
            
            clip.start();
            
        } catch (Exception e) {
            System.err.println("【GameAudio】播放音效失敗: " + soundFileName);
        }
    }
    
    /**
     * 🔥 停止所有正在播放的音效（可選）
     */
    public static void stopAllSounds() {
        for (Clip clip : activeSounds) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
        }
        activeSounds.clear();
    }
    
    public static void updateVolume(int volumePercent) {
        if (backgroundMusic == null) return;
        
        float dB;
        if (volumePercent <= 0) {
            dB = -80.0f;
        } else {
            dB = -40.0f * (1.0f - volumePercent / 100.0f);
            dB = Math.max(-80.0f, Math.min(0f, dB));
        }
        
        try {
            volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(dB);
            
            if (backgroundMusic.isRunning()) {
                boolean wasPlaying = backgroundMusic.isRunning();
                long position = backgroundMusic.getMicrosecondPosition();
                backgroundMusic.stop();
                backgroundMusic.setMicrosecondPosition(position);
                if (wasPlaying) {
                    backgroundMusic.start();
                }
            }
            
        } catch (IllegalArgumentException e) {
            // 不支援音量控制
        }
        
        // 🔥 同時更新所有正在播放的音效音量
        for (Clip clip : activeSounds) {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(dB);
                } catch (IllegalArgumentException e) {
                    // 忽略
                }
            }
        }
    }
    
    public static void updateVolumeImmediate(int volumePercent) {
        if (backgroundMusic == null) return;
        
        float dB;
        if (volumePercent <= 0) {
            dB = -80.0f;
        } else {
            dB = -40.0f * (1.0f - volumePercent / 100.0f);
            dB = Math.max(-80.0f, Math.min(0f, dB));
        }
        
        try {
            volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(dB);
            
            new Thread(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {}
                
                if (backgroundMusic != null && !backgroundMusic.isRunning()) {
                    backgroundMusic.start();
                }
            }).start();
            
        } catch (IllegalArgumentException e) {
            // 不支援音量控制
        }
    }
    
    public static void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    public static void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning()) {
            backgroundMusic.start();
        }
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }
}