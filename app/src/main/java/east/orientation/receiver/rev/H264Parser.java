package east.orientation.receiver.rev;

import android.media.MediaCodec;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by ljq on 2018/1/17.
 */

public class H264Parser {
    private static final String TAG = "H264Parser";
    private MediaCodec mMediaCodec;
    private PlayQueue mPlayQueue;

    private DecodeThread mDecodeThread;
    //private Object mLock = new Object();
    private volatile boolean isRunning;

    public H264Parser(MediaCodec codec,PlayQueue playQueue){
        mMediaCodec = codec;
        mPlayQueue = playQueue;
    }

    public class DecodeThread extends Thread{

        @Override
        public void run() {
            while (!isInterrupted()){
                if(!isRunning) break;
                byte[] frame = mPlayQueue.takeByte();
                if (frame == null) {
                    SystemClock.sleep(1);
                    continue;
                }
                try {
                    decodeLoop(frame);
                } catch (Exception e) {
                    Log.e("DecodeThread", "Exception" + e.toString());
                }
            }
        }
    }

    public void start(){
        //synchronized (mLock){
            mDecodeThread = new DecodeThread();
            mDecodeThread.start();
            isRunning = true;
        //}
    }

    public void stop(){
        //synchronized (mLock){
            isRunning = false;
            mDecodeThread.interrupt();
        //}
    }

    private void decodeLoop(byte[] buff) {
        Log.e(TAG,"decode loop");
        boolean mStopFlag = false;
        //存放目标文件的数据
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的据数大小
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        long startMs = System.currentTimeMillis();
        long timeoutUs = 0; //微秒
        byte[] marker0 = new byte[]{0, 0, 0, 1};
        byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
        byte[] streamBuffer = null;
        streamBuffer = buff;
        int bytes_cnt = 0;
        while (mStopFlag == false) {
            bytes_cnt = streamBuffer.length;
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame;
            }

            int startIndex = 0;
            int remaining = bytes_cnt;
            while (true) {
                if (remaining == 0 || startIndex >= remaining) {
                    break;
                }
                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
                if (nextFrameStart == -1) {
                    nextFrameStart = remaining;
                } else {
                }

                int inIndex = mMediaCodec.dequeueInputBuffer(timeoutUs);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = inputBuffers[inIndex];
                    byteBuffer.clear();
                    byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                    mMediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                    startIndex = nextFrameStart;
                } else {
                    continue;
                }

                int outIndex = mMediaCodec.dequeueOutputBuffer(info, timeoutUs);
                if (outIndex >= 0) {
                    //帧控制是不在这种情况下工作，因为没有PTS H264是可用的
//                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
//                        try {
//                            Thread.sleep(100);
//                            Log.e(TAG, "sleep 100");
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    SystemClock.sleep(1);
//                    Log.e(TAG, "decode a frame time =" + (System.currentTimeMillis() - startMs));
                    boolean doRender = (info.size != 0);
                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                    mMediaCodec.releaseOutputBuffer(outIndex, doRender);

                } else {
                }
            }
            mStopFlag = true;
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        return buf;
    }

    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }

        return -1;  // Not found
    }

    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }
}
