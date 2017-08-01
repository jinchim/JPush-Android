package com.jinchim.jpush_sdk.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Administrator on 2017/7/28 0028.
 */

public interface Api {

    String Api_Url = "http://luowenbin.jinchim.com:8888/jpush/";

    @POST("init")
    @FormUrlEncoded
    Call<ApiResponse<String>> init(
            @Field("clientId") String clientId
    );

}
