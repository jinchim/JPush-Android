package com.jinchim.jpush_sdk.network;

/**
 * 网络请求的失败回调接口（基于Retrofit封装）
 */

public interface RetrofitFailedCallback {

    void onError(Throwable e);

}
