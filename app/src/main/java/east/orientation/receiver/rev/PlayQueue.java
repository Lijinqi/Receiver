package east.orientation.receiver.rev;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

public class PlayQueue {
    private ArrayBlockingQueue<byte[]> mPlayQueue;
    private String TAG = "PlayQueue";
    private static final int NORMAL_FRAME_BUFFER_SIZE = 80; //缓存区大小

    public PlayQueue() {
        mPlayQueue = new ArrayBlockingQueue<>(NORMAL_FRAME_BUFFER_SIZE, true);
    }


    public byte[] takeByte() {
        try {
            if (mPlayQueue.size() >= 50) {
                Log.e(TAG, "too much frame in PlayQueue" + mPlayQueue.size());
            }
            return mPlayQueue.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "take bytes exception" + e.toString());
            return null;
        }
    }

    public void putByte(byte[] bytes) {
        try {
            mPlayQueue.put(bytes);
        } catch (InterruptedException e) {
            Log.e(TAG, "put bytes exception" + e.toString());
        }
    }

    public void clear() {
        if (mPlayQueue != null) {
            mPlayQueue.clear();
        }
    }

}
