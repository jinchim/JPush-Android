package com.jinchim.jpush;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jinchim.jpush_sdk.JPushCallback;
import com.jinchim.jpush_sdk.JPushSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JPushSDK.getInstance().init(this);
        JPushSDK.getInstance().register(new JPushCallback() {
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
