package com.jinchim.jpush_sdk.live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/**
 * 这个页面是只有一个像素的透明页面，在单独的进程里面，现已废弃
 */

public class LiveActivity extends Activity {

    private static final String TAG = LiveActivity.class.getSimpleName();

    public static void toLiveActivity(Context context) {
        Intent intent = new Intent(context, LiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        Window window = getWindow();
        window.setGravity(Gravity.START | Gravity.TOP);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = 1;
        attributes.height = 1;
        attributes.x = 0;
        attributes.y = 0;
        window.setAttributes(attributes);

        ScreenManager.getInstance(this).setActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
