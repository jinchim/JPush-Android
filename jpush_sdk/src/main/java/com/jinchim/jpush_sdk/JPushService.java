package com.jinchim.jpush_sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.jinchim.jpush_sdk.live.ScreenBroadcastListener;
import com.jinchim.jpush_sdk.live.ScreenManager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;


/**
 * MQTT 协议的服务
 */
public class JPushService extends Service implements IMqttActionListener, MqttCallback {

    private static final String TAG = JPushService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1001;

    private String serverUri = "tcp://luowenbin.jinchim.com:61613";
    private String username = "admin";
    private String password = "password";
    private String clientId;

    private MqttAndroidClient client;
    private MqttConnectOptions options;

    public static void start(Context context, String clientId) {
        Intent intent = new Intent(context, JPushService.class);
        intent.putExtra("clientId", clientId);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, JPushService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // API 18 以下，直接发送 Notification 并将其置为前台
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(NOTIFICATION_ID, new Notification());
        } else {
            // API 18 以上，发送 Notification 并将其置为前台后，启动 JPushForeService
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(NOTIFICATION_ID, builder.build());
            startService(new Intent(this, JPushForeService.class));
        }

        // 启动屏幕管理监听
        ScreenBroadcastListener listener = new ScreenBroadcastListener(this);
        listener.registerListener(new ScreenBroadcastListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                Log.i(TAG, "onScreenOn");
                ScreenManager.getInstance(JPushService.this).finishActivity();
            }

            @Override
            public void onScreenOff() {
                Log.i(TAG, "onScreenOff");
                ScreenManager.getInstance(JPushService.this).startActivity();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clientId = intent.getStringExtra("clientId");
        init();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 断开连接
        disconnect();
    }


    // 灰色保活
    // 这里创建了一个前台服务，然后马上终止，利用 API Level < 25（Android 系统 7.0 以下） 的 bug，当两个前台服务指定同一个 id 时，则不会显示在通知栏上
    public static class JPushForeService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            // 发送与相同的 Notification，然后将其取消并取消自己的前台显示
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(NOTIFICATION_ID, builder.build());
            new Handler().postDelayed(() -> {
                stopForeground(true);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(NOTIFICATION_ID);
                stopSelf();
            }, 100);
        }
    }


    //----------------------push--------------------//

    private void init() {
        if (client != null) {
            return;
        }
        client = new MqttAndroidClient(this, serverUri, clientId, new MemoryPersistence()); // 初始化一个对象，这里指定信息缓存方法为内存缓存
        client.setCallback(this);
        options = new MqttConnectOptions();
        options.setCleanSession(true); // 设置是否清空 session，这里如果设置为 false 表示服务器会保留客户端的连接记录，这里设置为 true 表示每次连接到服务器都以新的身份连接
        options.setUserName(username); // 设置连接用户名
        options.setPassword(password.toCharArray()); // 设置连接密码
        options.setConnectionTimeout(15); // 设置超时时间，单位为秒
        options.setKeepAliveInterval(30); // 设置会话心跳时间，单位为秒，服务器会每隔一定时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        connect();
    }

    private void connect() {
        try {
            Log.i(TAG, "connect start");
            if (client == null) {
                Log.i(TAG, "connect is null");
                return;
            }
            client.connect(options, this, this);
        } catch (MqttException e) {
            Log.i(TAG, "connect error => " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {
            Log.i(TAG, "disconnect start");
            if (client == null) {
                Log.i(TAG, "disconnect is null");
                return;
            }
            client.disconnect();
            Log.i(TAG, "disconnect success");
        } catch (MqttException e) {
            Log.i(TAG, "disconnect error => " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.i(TAG, "connect success");
        try {
            if (client == null) {
                Log.i(TAG, "subscribe is null");
                return;
            }
            client.subscribe(clientId, 1);
        } catch (MqttException e) {
            Log.i(TAG, "subscribe error => " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.i(TAG, "connect failed => " + exception.getMessage());
        // 失败重连
        connect();
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "connectionLost => " + cause.getMessage());
        // 断线重连
        connect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "messageArrived => " + new String(message.getPayload()));
        // 发送一个 EventBus 消息
        EventBus.getDefault().post(new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "deliveryComplete");
    }


}
