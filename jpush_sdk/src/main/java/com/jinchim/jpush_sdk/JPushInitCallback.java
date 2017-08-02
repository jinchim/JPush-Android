package com.jinchim.jpush_sdk;

/**
 * Created by Administrator on 2017/7/31 0031.
 */

public interface JPushInitCallback {

    void onSuccess(String clientId);

    void onFailed(String msg);

}
