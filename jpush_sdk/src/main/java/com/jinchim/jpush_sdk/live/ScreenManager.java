package com.jinchim.jpush_sdk.live;

import android.content.Context;

/**
 * 屏幕亮起和锁定的管理类
 */

public class ScreenManager {

    private Context context;
    private LiveActivity liveActivity;

    public static ScreenManager instance;

    public static ScreenManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ScreenManager.class) {
                if (instance == null) {
                    instance = new ScreenManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private ScreenManager(Context context) {
        this.context = context;
    }

    public void setActivity(LiveActivity liveActivity) {
        this.liveActivity = liveActivity;
    }

    public void startActivity() {
        LiveActivity.toLiveActivity(context);
    }

    public void finishActivity() {
        if (liveActivity != null) {
            liveActivity.finish();
        }
    }

}
