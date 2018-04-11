package east.orientation.receiver.local;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.xuhao.android.libsocket.sdk.connection.IConnectionManager;

import java.util.concurrent.ConcurrentLinkedDeque;

import east.orientation.receiver.rev.PlayQueue;

/**
 * Created by ljq on 2018/1/23.
 */

public class AppInfo {
    private PlayQueue mPlayQueue = new PlayQueue(); // 作为接收端 接收的投屏帧数据缓存
    private ConcurrentLinkedDeque<byte[]> mScreenStream = new ConcurrentLinkedDeque<>();// 作为发送端 存储录屏帧数据的缓存

    private Context mContext;
    private final WifiManager mWifiManager;
    private volatile boolean isPlaying;
    private volatile boolean isActivityRunning;
    private volatile boolean isStreamRunning;
    private volatile boolean isCastServiceOn;
    private IConnectionManager mConnectionManager;

    public AppInfo(Context context){
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public IConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public void setConnectionManager(IConnectionManager connectionManager) {
        mConnectionManager = connectionManager;
    }

    public PlayQueue getPlayQueue() {
        return mPlayQueue;
    }

    public ConcurrentLinkedDeque<byte[]> getScreenStream() {
        return mScreenStream;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setActivityRunning(final boolean activityRunning) {
        isActivityRunning = activityRunning;
    }

    public void setStreamRunning(final boolean streamRunning) {
        isStreamRunning = streamRunning;
        //getMainActivityViewModel().setCast(streamRunning);
    }

    public boolean isWiFiConnected() {
        return mWifiManager.getConnectionInfo().getIpAddress() != 0;
    }

    public boolean isActivityRunning() {
        return isActivityRunning;
    }

    public boolean isStreamRunning() {
        return isStreamRunning;
    }

}
