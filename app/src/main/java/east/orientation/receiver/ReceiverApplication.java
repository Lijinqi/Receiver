package east.orientation.receiver;

import android.app.Application;
import android.content.Intent;

import com.xuhao.android.libsocket.sdk.OkSocket;

import east.orientation.receiver.local.AppInfo;
import east.orientation.receiver.service.ReceiveService;

/**
 * Created by ljq on 2018/1/23.
 */

public class ReceiverApplication extends Application {

    private static ReceiverApplication sAppInstance;

    private AppInfo mAppInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        // 初始化OkSocket
        OkSocket.initialize(this, false);
        mAppInfo = new AppInfo(this);

        startService(new Intent(this, ReceiveService.class));
    }

    public static AppInfo getAppInfo() {
        return sAppInstance.mAppInfo;
    }


}
