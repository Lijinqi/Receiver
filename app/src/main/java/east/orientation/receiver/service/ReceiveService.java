package east.orientation.receiver.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;
import com.xuhao.android.libsocket.utils.BytesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import east.orientation.receiver.eventbus.ConnectMessage;
import east.orientation.receiver.eventbus.ReceiveMessage;
import east.orientation.receiver.util.ToastUtil;

import static com.xuhao.android.libsocket.sdk.OkSocket.open;
import static east.orientation.receiver.ReceiverApplication.getAppInfo;
import static east.orientation.receiver.eventbus.ConnectMessage.ACTION_CONNECT;
import static east.orientation.receiver.eventbus.ConnectMessage.ACTION_DISCONNECT;

/**
 * Created by ljq on 2018/3/5.
 */

public class ReceiveService extends Service {
    private ConnectionInfo mInfo;

    private OkSocketOptions mOkOptions;

    private SocketActionAdapter adapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        adapter = new SocketActionAdapter() {
            @Override
            public void onSocketIOThreadStart(Context context, String action) {
                super.onSocketIOThreadStart(context, action);
            }

            @Override
            public void onSocketIOThreadShutdown(Context context, String action, Exception e) {
                super.onSocketIOThreadShutdown(context, action, e);
            }

            @Override
            public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
                super.onSocketDisconnection(context, info, action, e);
                ToastUtil.show(context, "- 断开连接 -");
            }

            @Override
            public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
                super.onSocketConnectionSuccess(context, info, action);
                // 连接成功 则开始播放视频
                EventBus.getDefault().post(new ReceiveMessage(ReceiveMessage.START_PLAYER));
            }

            @Override
            public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
                super.onSocketConnectionFailed(context, info, action, e);
                ToastUtil.show(context, "- 连接失败 -");
            }

            @Override
            public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
                super.onSocketReadResponse(context, info, action, data);
                // todo 添加帧数据到队列
                getAppInfo().getPlayQueue().putByte(data.getBodyBytes());

            }

            @Override
            public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
                super.onSocketWriteResponse(context, info, action, data);
            }

            @Override
            public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {
                super.onPulseSend(context, info, data);
            }
        };

        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onConMessageEvent(ConnectMessage message) {
        switch (message.getMessage()) {
            case ACTION_CONNECT:
                connectToServer(message.getIp(), message.getPort());
                break;
            case ACTION_DISCONNECT:

                break;
        }
    }

    private void connectToServer(String ip, int port) {
        mInfo = new ConnectionInfo(ip, port);

        mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setReadSingleTimeBufferBytes(1024)
                .setBackgroundLiveMinute(-1)
                .build();
        getAppInfo().setConnectionManager(open(mInfo, mOkOptions));

        getAppInfo().getConnectionManager().unRegisterReceiver(adapter);
        getAppInfo().getConnectionManager().registerReceiver(adapter);

        if (getAppInfo().getConnectionManager() == null) {
            return;
        }

        if (!getAppInfo().getConnectionManager().isConnect()) {
            getAppInfo().getConnectionManager().connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
