package east.orientation.receiver.eventbus;

/**
 * Created by ljq on 2018/3/5.
 */

public class ConnectMessage {
    public static final String ACTION_CONNECT = "MESSAGE_ACTION_TCP_CONNECT";
    public static final String ACTION_DISCONNECT = "MESSAGE_ACTION_TCP_DISCONNECT";

    private final String message;
    private String ip;
    private int port;

    public ConnectMessage(final String message, String ip, int port) {
        this.message = message;
        this.ip = ip;
        this.port = port;
    }

    public String getMessage() {
        return message;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
