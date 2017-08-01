package com.jinchim.jpush_sdk.utils;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jinchim.jpush_sdk.network.Api;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitUtils框架的工具类
 */
public class RetrofitUtils {

    public static Api createChemiApi() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(logInterceptor())
                .build();
        Gson gson = new GsonBuilder().create();
        Retrofit restAdapter = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .baseUrl(Api.Api_Url)
                .build();
        return restAdapter.create(Api.class);
    }

    private static Interceptor logInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String postContent = "";
                if (request.method().equals("POST")) {
                    //if (TextUtils.equals(request.body().contentType().subtype(), "x-www-form-urlencoded")) { //非上传图片都使用@FormUrlEncoded 即application/x-www-form-urlencoded
                    Request copy = request.newBuilder().build();
                    Buffer buffer = new Buffer();
                    copy.body().writeTo(buffer);
                    postContent = buffer.readUtf8();
                    //}
                }
                Log.i("TNetworkRequest[" + request.method() + "]", request.url().toString() + " " + postContent);
                //Log.i("TNetworkRequest", request.headers().toString());

                long tmStart = SystemClock.currentThreadTimeMillis();
                Response response = chain.proceed(request);
                MediaType mediaType = response.body().contentType();
                byte[] bytes = response.body().bytes();

                long tmDelay = SystemClock.currentThreadTimeMillis() - tmStart;
                if (tmDelay < 3000) {
                    //去掉注释，打开模拟网络慢的情况，3000为打圈圈的时间
                    //SystemClock.sleep(3000 - tmDelay);
                }

                String url = request.url().toString();
                String urlKey = url.substring(url.lastIndexOf("/") + 1, url.contains("?") ? url.indexOf("?") : url.length());
                String content = new String(bytes, "UTF-8");
                Log.i("TNetworkResponse[" + urlKey + " " + response.code() + "]", content);

                return response.newBuilder().body(ResponseBody.create(mediaType, bytes)).build();
            }
        };
    }


    /**
     * <pre>
     * 定义方法： @Body MultipartBody params
     * 可传参数：
     *     map.put("int", 10);
     *     map.put("String", "value");
     *     map.put("String[]", new String[]{});
     *     map.put("File", new File("c:\\upload.jpg"));
     *     map.put("Files[]", new ArrayList(){new File("c:\\upload.jpg"), new File("c:\\upload.jpg")});
     *     注意http要求数组类型在key后面加[]符号</pre>
     */
    public static MultipartBody formData(Map<String, Object> params) {
        MultipartBody.Builder build = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (params != null) {
            for (String key : params.keySet()) {
                Object value = params.get(key);
                if (value != null) {
                    //list
                    if (value instanceof List) {
                        List list = (List) value;
                        for (Object item : list) {
                            if (item != null) {
                                //file|string
                                if (item instanceof File) {
                                    File file = (File) item;
                                    build.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                                } else {
                                    build.addFormDataPart(key, item.toString());
                                }
                            }
                        }
                    }
                    //object[]
                    else if (value.getClass().isArray()) {
                        for (int i = 0; i < Array.getLength(value); i++) {
                            Object item = Array.get(value, i);
                            if (item != null) {
                                //file|string
                                if (item instanceof File) {
                                    File file = (File) item;
                                    build.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                                } else {
                                    build.addFormDataPart(key, item.toString());
                                }
                            }
                        }
                    }
                    //file|string
                    else if (value instanceof File) {
                        File file = (File) value;
                        build.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                    } else {
                        build.addFormDataPart(key, value.toString());
                    }
                }
            }
        }
        return build.build();
    }

}
