package east.orientation.receiver.local;

public class Common {

    public static final int REQUEST_CODE_SCREEN_CAPTURE = 1;

    public static final int VIEWER_PORT = 28888;

    public static final int DEFAULT_SCREEN_WIDTH = 960;
    public static final int DEFAULT_SCREEN_HEIGHT = 640;
    public static final int DEFAULT_SCREEN_DPI = 160;
    public static final int DEFAULT_VIDEO_BITRATE = 800*1024;
    public static final int DEFAULT_VIDEO_FPS = 30;
    public static final int DEFAULT_I_FRAME_INTERVAL = 1;//5 seconds between I-frames
    public static final long DEFAULT_REPEAT_PREVIOUS_FRAME_AFTER = 1000000 / 15;

    public static final String NOTIFICATION_CHANNEL_ID = "com.east.orientation.NOTIFICATION_CHANNEL_01";

    public static final int NOTIFICATION_STREAMING = 10;
    public static final int NOTIFICATION_CONNECTING_SERVER = 12;
    public static final int NOTIFICATION_NOT_CONNECTED_SERVER = 13;

    public static final String IP = "ip";
    public static final String PORT = "port";

    public static final int CMD_CAST = 1;
    public static final int CMD_NULL = 2;
}
