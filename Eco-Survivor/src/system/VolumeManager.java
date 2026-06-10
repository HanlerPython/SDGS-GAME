package system;

public class VolumeManager {
    
    private static int currentVolume = 60;  // 預設 60%
    private static VolumeChangeListener listener;
    
    public interface VolumeChangeListener {
        void onVolumeChanged(int newVolume);
    }
    
    public static void setListener(VolumeChangeListener listener) {
        VolumeManager.listener = listener;
    }
    
    public static int getVolume() {
        return currentVolume;
    }
    
    public static void setVolume(int volume) {
        currentVolume = Math.max(0, Math.min(100, volume));
        System.out.println("【VolumeManager】音量設定為 " + currentVolume + "%");
        
        // 同步音樂音量
        GameAudio.updateVolume(currentVolume);
        
        // 通知監聽器
        if (listener != null) {
            listener.onVolumeChanged(currentVolume);
        }
    }
    
    public static void addVolume(int delta) {
        setVolume(currentVolume + delta);
    }
}