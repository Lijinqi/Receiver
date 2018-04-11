package east.orientation.receiver.eventbus;

/**
 * Created by ljq on 2018/2/1.
 */

public class ReceiveMessage {
    public static final String START_PLAYER = "START_PLAYER";
    public static final String STOP_PLAYER = "STOP_PLAYER";

    private final String message;

    public ReceiveMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
