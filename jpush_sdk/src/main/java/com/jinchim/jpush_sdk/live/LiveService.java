package com.jinchim.jpush_sdk.live;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.jinchim.jpush_sdk.R;


/**
 * 用于启动和关闭 LiveActivity，在单独的进程里面，用于减轻应用负荷
 */
public class LiveService extends Service {

    private static final String TAG = LiveService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1002;

    public static void start(Context context) {
        Intent intent = new Intent(context, LiveService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, LiveService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // API 18 以下，直接发送 Notification 并将其置为前台
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(NOTIFICATION_ID, new Notification());
        } else {
            // API 18 以上，发送 Notification 并将其置为前台后，启动 LiveForeService
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(NOTIFICATION_ID, builder.build());
            startService(new Intent(this, LiveForeService.class));
        }

        // 启动屏幕管理监听
        ScreenBroadcastListener listener = new ScreenBroadcastListener(this);
        listener.registerListener(new ScreenBroadcastListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                Log.i(TAG, "onScreenOn");
                ScreenManager.getInstance(LiveService.this).finishActivity();
            }

            @Override
            public void onScreenOff() {
                Log.i(TAG, "onScreenOff");
                ScreenManager.getInstance(LiveService.this).startActivity();
            }
        });

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // 灰色保活
    // 这里创建了一个前台服务，然后马上终止，利用 API Level < 25（Android 系统 7.0 以下） 的 bug，当两个前台服务指定同一个 id 时，则不会显示在通知栏上
    public static class LiveForeService extends Service {

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

}
