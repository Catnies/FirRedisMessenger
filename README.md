# FirRedisMessenger

## 📌 关于
使用 Pubsub 实现的简单 Redis 消息广播通信库. <br />
你可以轻松快速地创建 Redis数据包 和 监听器 进行简单的通信. <br />

## 📚 插件 API
```kotlin
repositories {
    maven("https://repo.catnies.top/releases/")
}
```
```kotlin
dependencies {
    implementation("top.catnies:firredismessenger:1.0.0")
}
```

## 💻 使用方法
1. 初始化管理器, 创建一个 RedisUri 对象, 然后创建 RedisManager 时还需要传入一个客户端唯一标识符名称：
```Java
// 初始化Redis管理器;
RedisUri redisUri = RedisUri.builder().ip("127.0.0.1").host(6379).build();
RedisManager redisManager = new RedisManager(redisUri, "Lobby");
```

2. 创建一个Redis数据包.
```Java
// 创建数据包;
RedisPacket redisPacket = RedisPacket.of(
        redisManager.getServerId(), // 客户端唯一标识符名称
        "Lobby",    // 接收方, 客户端唯一标识符名称
        "查询人数",      // 消息主题/分类
        "现在有几个玩家在Lobby服务器啊?" // 消息内容
);
```

3. 为数据包设置回调函数和超时回调.
```Java
// 当收到回复消息时触发的回调函数;
redisPacket.withCallback(payload /* 回复内容 */ -> {
    System.out.println("收到了回复消息: 现在有 " + payload + " 个在线玩家喵!");
});
// 当发消息后, 超时未接收到回复消息时触发的回调函数;
redisPacket.withOnTimeout(packet /* 发送的数据包 */ -> {
    System.out.println("超时警告: Lobby 好像没有回复消息喵!");
});
```

4. 发布广播数据包.
```Java
// 广播发布数据包;
redisManager.publish(
        "myChannel",    // 广播的频道
        redisPacket,    // Redis数据包
        10000           // 触发超时回调的时间
);
```

5. 监听数据包.
```Java
// 创建一个实现 RedisListener 接口的监听器类;
public class MyRedisListener implements RedisListener {
    
    // 为方法添加 RedisSubject 注解, 并且入参只有一个 RedisPacket, 即可标记为监听器方法;
    @RedisSubject(
            channel = "myChannel",   // 频道名称
            subject = "查询人数",    // 分类/主题
            priority = 1,       // 监听器权重, 越高越先执行.
            autoSubscribe = true    // 自动订阅, 如果客户端没有订阅channel, 则在注册监听器的时候自动帮忙订阅.
    )
    public void onReceivedEggSubjectMessage(RedisPacket packet) {
        String payload = packet.getPayload();
        System.out.println("收到了消息内容: " + payload);
        // 回复数据包, 创建时需要原始数据包和回复的内容作为入参;
        // 当目标收到回复数据包时, 才会执行回调函数, 具体可参考 3.
        RedisPacket response = RedisPacket.ofResponse(packet, "114");
        response.publish("myChannel");
    }
}
// 使用管理器注册监听器;
RedisListener listener = new MyRedisListener();
redisManager.registerListeners(listener);
// 还可以注销监听器
redisManager.unregisterListeners(listener);
```

6. 其他工具方法.
```Java
// 使用管理器注册监听器;
redisManager.subscribeChannel("otherChannel");
// 关闭Redis
redisManager.shutdown();
```