package east.orientation.receiver.ui;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

import east.orientation.receiver.R;
import east.orientation.receiver.local.Common;
import east.orientation.receiver.rev.H264Parser;
import east.orientation.receiver.rev.PlayQueue;

import static east.orientation.receiver.ReceiverApplication.getAppInfo;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String DEFAULT_IP_ADDRESS = "192.168.0.111";
    public static final int DEFAULT_PORT = 28888;
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder;
    private H264Parser mH264Parser;
    private MediaCodec mCodec;

    private static final int VIDEO_WIDTH = 960;
    private static final int VIDEO_HEIGHT = 540;
    private int FrameRate = 60;
    private Boolean UseSPSandPPS = false;
    private PlayQueue mPlayQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initSurface();
    }

    private void initSurface(){
        mPlayQueue = getAppInfo().getPlayQueue();
        mSurfaceView = findViewById(R.id.sf_show);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.e(TAG,"SurfaceView surfaceCreated");
                try {
                    //通过多媒体格式名创建一个可用的解码器
                    mCodec = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //初始化编码器
                final MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, Common.DEFAULT_SCREEN_WIDTH, Common.DEFAULT_SCREEN_HEIGHT);

                //设置帧率
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Common.DEFAULT_VIDEO_FPS);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Common.DEFAULT_I_FRAME_INTERVAL);
//                // -----------------当画面静止时,重复最后一帧--------------------------------------------------------
//                // you can see this to know more
//                // https://stackoverflow.com/questions/36578660/android-mediaformatkey-repeat-previous-frame-after-setting
//                mediaFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER,  Common.DEFAULT_REPEAT_PREVIOUS_FRAME_AFTER);
//                mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
//                mediaFormat.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

                //https://developer.android.com/reference/android/media/MediaFormat.html#KEY_MAX_INPUT_SIZE
                //设置配置参数，参数介绍 ：
                // format	如果为解码器，此处表示输入数据的格式；如果为编码器，此处表示输出数据的格式。
                //surface	指定一个surface，可用作decode的输出渲染。
                //crypto	如果需要给媒体数据加密，此处指定一个crypto类.
                //   flags	如果正在配置的对象是用作编码器，此处加上CONFIGURE_FLAG_ENCODE 标签。
                mCodec.configure(mediaFormat, holder.getSurface(), null, 0);
                mCodec.start();

                // todo Parser start
                startDecode();
                getAppInfo().setPlaying(true);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.e(TAG,"SurfaceView surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG,"SurfaceView surfaceDestroyed");
                getAppInfo().setPlaying(false);
            }
        });
    }

    private void startDecode(){
        mH264Parser = new H264Parser(mCodec,getAppInfo().getPlayQueue());
        mH264Parser.start();
    }

    private void stop(){
        if (mH264Parser != null){
            mH264Parser.stop();
        }
        if (mCodec != null){
            mCodec.release();
            mCodec.stop();
        }
        if (mPlayQueue != null){
            mPlayQueue.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }
}
