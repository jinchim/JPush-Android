package com.jinchim.jpush_sdk.network;

import com.google.gson.annotations.SerializedName;

/**
 * 网络请求接口的返回数据结构
 */

public class ApiResponse<T> {

    public static final int Status_Success = 200;
    public static final int Status_Failed = 201;

    @SerializedName("status") public int status;
    @SerializedName("msg") public String msg;
    @SerializedName("data") public T data;

}
