package com.jinchim.jpush_sdk.live;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 这个是用来监听屏幕亮起和锁定的，通过监听系统广播
 */

public class ScreenBroadcastListener {

    private Context context;

    private ScreenBroadcastReceiver screenReceiver;

    private ScreenStateListener listener;

    public ScreenBroadcastListener(Context context) {
        this.context = context.getApplicationContext();
        screenReceiver = new ScreenBroadcastReceiver();
    }

    public interface ScreenStateListener {

        void onScreenOn();

        void onScreenOff();
    }

    /**
     * 屏幕状态广播接收者
     */
    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                listener.onScreenOn();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                listener.onScreenOff();
            }
        }
    }

    public void registerListener(ScreenStateListener listener) {
        this.listener = listener;
        registerListener();
    }

    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(screenReceiver, filter);
    }

}

