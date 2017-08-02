package com.jinchim.jpush;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jinchim.jpush_sdk.JPushInitCallback;
import com.jinchim.jpush_sdk.JPushMessageCallback;
import com.jinchim.jpush_sdk.JPushSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JPushSDK.getInstance().init(this, new JPushInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Log.i("jinchim", "onSuccess => " + clientId);
            }

            @Override
            public void onFailed(String msg) {
                Log.i("jinchim", "onFailed => " + msg);
            }
        });
        JPushSDK.getInstance().register(new JPushMessageCallback() {
            @Override
            public void onMessage(String msg) {
                Log.i("jinchim", "onMessage => " + msg);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        JPushSDK.getInstance().unregister();
    }
}
