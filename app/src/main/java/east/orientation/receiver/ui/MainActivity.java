package east.orientation.receiver.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xuhao.android.libsocket.sdk.ConnectionInfo;
import com.xuhao.android.libsocket.sdk.OkSocketOptions;
import com.xuhao.android.libsocket.sdk.SocketActionAdapter;
import com.xuhao.android.libsocket.sdk.bean.IPulseSendable;
import com.xuhao.android.libsocket.sdk.bean.ISendable;
import com.xuhao.android.libsocket.sdk.bean.OriginalData;
import com.xuhao.android.libsocket.sdk.connection.IConnectionManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import east.orientation.receiver.eventbus.ConnectMessage;
import east.orientation.receiver.eventbus.ReceiveMessage;

import east.orientation.receiver.tcp.Customer;
import east.orientation.receiver.R;
import east.orientation.receiver.local.Common;
import east.orientation.receiver.tcp.SocketTransceiver;
import east.orientation.receiver.tcp.TcpClient;
import east.orientation.receiver.util.ToastUtil;

import static com.xuhao.android.libsocket.sdk.OkSocket.open;
import static east.orientation.receiver.ReceiverApplication.getAppInfo;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int UPDATE_VIEW_SEARCHING = 0;
    private static final int UPDATE_VIEW_SEARCH_FINISH = 1;
    private static final int CONNECTED_SERVER = 2;


    private Button mBtnConnServer;
    AlertDialog.Builder mDialog;

    private Customer mCustomer;
    private List<Customer.DeviceBean> mDeviceList = new ArrayList<>();
    private String[] mDevices;

    private ConnectionInfo mInfo;
    private IConnectionManager mManager;
    private OkSocketOptions mOkOptions;
    private SocketActionAdapter adapter;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VIEW_SEARCHING:
                    Log.e(TAG, "正在搜索..."); // 主要用于在UI上展示正在搜索
                    break;
                case UPDATE_VIEW_SEARCH_FINISH:
                    Log.e(TAG, "搜索结束。"); // 结束UI上的正在搜索
                    showCaster();
                    break;
                case CONNECTED_SERVER:
                    Toast.makeText(MainActivity.this, "connect " + ((SocketTransceiver) msg.obj).getInetAddress().getHostAddress(), Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initListener();
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void init() {
        mDialog = new AlertDialog.Builder(MainActivity.this);
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
    }

    private void initListener() {
        mBtnConnServer = findViewById(R.id.btn_connect_server);
        mBtnConnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomer();
            }
        });
    }

    private void showCaster() {
        mDialog.setTitle(R.string.available_server);
        mDialog.setItems(mDevices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo 连接服务
//                mInfo = new ConnectionInfo(mDeviceList.get(which).getIp(), 28888);
//
//                mOkOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
//                        .build();
//                getAppInfo().setConnectionManager(open(mInfo, mOkOptions));
//
//                getAppInfo().getConnectionManager().registerReceiver(adapter);
//
//                if (getAppInfo().getConnectionManager() == null) {
//                    return;
//                }
//
//                if (!getAppInfo().getConnectionManager().isConnect()) {
//                    getAppInfo().getConnectionManager().connect();
//                } else {
//                    getAppInfo().getConnectionManager().disConnect();
//                }

                EventBus.getDefault().post(new ConnectMessage(ConnectMessage.ACTION_CONNECT, mDeviceList.get(which).getIp(), 28888));


                dialog.dismiss();
            }
        });
        mDialog.setPositiveButton(R.string.refresh, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCustomer();
            }
        });
        mDialog.show();
    }

    private void getCustomer() {
        if (mCustomer != null) {
            mCustomer.stop();
        }
        mCustomer = new Customer() {
            @Override
            public void onSearchStart() {
                mHandler.sendEmptyMessage(UPDATE_VIEW_SEARCHING);
            }

            @Override
            public void onSearchFinish(Set deviceSet) {
                mDeviceList.clear();
                mDeviceList.addAll(deviceSet);
                DeviceBean deviceBean = new DeviceBean();
                deviceBean.setIp("172.25.120.215");
                deviceBean.setName("172.25.120.215");
                deviceBean.setPort(28888);
                mDeviceList.add(deviceBean);
                mDevices = new String[mDeviceList.size()];
                for (int i = 0; i < mDeviceList.size(); i++) {
                    mDevices[i] = mDeviceList.get(i).getName();
                }
                mHandler.sendEmptyMessage(UPDATE_VIEW_SEARCH_FINISH); // 在UI上更新设备列表
            }
        };
        mCustomer.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReceiveMessage message) {
        switch (message.getMessage()) {
            case ReceiveMessage.START_PLAYER:
                Log.e(TAG, "start play");
                getAppInfo().getPlayQueue().clear();

                startActivity(new Intent(MainActivity.this, VideoActivity.class));
                break;
            case ReceiveMessage.STOP_PLAYER:

                break;
        }
    }
}
