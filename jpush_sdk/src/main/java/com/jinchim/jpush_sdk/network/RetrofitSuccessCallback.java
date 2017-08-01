package com.jinchim.jpush_sdk.network;

/**
 * 网络请求的成功回调接口（基于Retrofit封装）
 */

public interface RetrofitSuccessCallback<T> {

    void onResponse(T data);

}
