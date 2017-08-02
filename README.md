# 简易推送框架

## 使用步骤

### 引入工程

在对应模块的 build.gradle 中的 dependencies 标签中加入：

``` gradle
compile 'com.jinchim:jpush:1.0.0'
```

### 初始化 SDK

``` java
JPushSDK.getInstance().init(this, new JPushInitCallback() {
    @Override
    public void onSuccess(String clientId) {
        // 这里返回初始化成功后的客户端 ID，用来推送消息的标识
    }

    @Override
    public void onFailed(String msg) {
        // 这里返回初始化失败的信息
    }
});
```

### 注册

``` java
JPushSDK.getInstance().register(new JPushMessageCallback() {
    @Override
    public void onMessage(String msg) {
        // 这里返回收到的消息字符串
    }
});
```

### 注销

``` java
// 注销后不再收到任何消息
JPushSDK.getInstance().unregister();
```

### 推送消息

开放一个接口用来推送消息，不管是客户端还是服务器，都可以进行消息推送，使用 POST 请求：
http://luowenbin.jinchim.com:8888/，需要传入参数：clientId 和 msg


## 注意事项

* 这是本人根据 MQTT 协议开发的 Android 推送方案，轻量、高效，不用作商业用途
* 注册服务采取了一系列保活策略，保证不被系统回收，即使锁屏也可以接收消息
* 作者：金梧
