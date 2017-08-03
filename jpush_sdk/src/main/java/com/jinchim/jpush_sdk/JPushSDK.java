package com.jinchim.jpush_sdk;

import android.content.Context;
import android.util.Log;

import com.jinchim.jpush_sdk.network.Api;
import com.jinchim.jpush_sdk.network.ApiResponse;
import com.jinchim.jpush_sdk.utils.DeviceUtils;
import com.jinchim.jpush_sdk.utils.RetrofitUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/7/27 0027.
 */

public class JPushSDK {

    private static final String TAG = JPushSDK.class.getSimpleName();

    private static JPushSDK instance;

    private Api api;
    private Context context;
    private JPushMessageCallback messageCallback;
    private JPushInitCallback initCallback;

    // 这个值用来记录客户端 ID
    private String clientId;
    // 是否初始化成功
    private boolean isInitSuccess;

    public void init(Context context, JPushInitCallback initCallback) {
        Log.i(TAG, "init start");
        this.context = context;
        this.initCallback = initCallback;
        api = RetrofitUtils.createChemiApi();
        clientId = DeviceUtils.getLocalMacAddressFromIp(context);

        // 网络请求
        api.init(clientId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "server failed");
                    if (JPushSDK.this.initCallback != null) {
                        JPushSDK.this.initCallback.onFailed("server failed");
                    }
                    return;
                }
                ApiResponse apiResponse = response.body();
                if (apiResponse.status == ApiResponse.Status_Success) {
                    isInitSuccess = true;
                    Log.i(TAG, "init success");
                    Log.i(TAG, "response => " + apiResponse.data);
                    if (JPushSDK.this.initCallback != null) {
                        JPushSDK.this.initCallback.onSuccess((String) apiResponse.data);
                    }
                } else {
                    Log.i(TAG, "init failed => " + apiResponse.msg);
                    if (JPushSDK.this.initCallback != null) {
                        JPushSDK.this.initCallback.onFailed(apiResponse.msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.i(TAG, "network error => " + t.getMessage());
            }
        });
    }


    public void register(JPushMessageCallback messageCallback) {
        if (context == null) {
            Log.i(TAG, "context null");
            return;
        }
        if (!isInitSuccess) {
            Log.i(TAG, "init failed");
            return;
        }
        Log.i(TAG, "register");
        // 设置回调
        this.messageCallback = messageCallback;
        // 启动服务
        JPushService.start(context, clientId);
        // 注册 EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    public void unregister() {
        if (context == null) {
            Log.i(TAG, "context null");
            return;
        }
        Log.i(TAG, "unregister");
        // 停止服务
        JPushService.stop(context);
        // 注销 EventBus
        EventBus.getDefault().unregister(this);
    }


    public static JPushSDK getInstance() {
        if (instance == null) {
            synchronized (JPushSDK.class) {
                if (instance == null) {
                    instance = new JPushSDK();
                }
            }
        }
        return instance;
    }


    @Subscribe
    public void event(String msg) {
        if (messageCallback != null) {
            messageCallback.onMessage(msg);
        }
    }

}
