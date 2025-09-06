package top.catnies.firredismessenger;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import top.catnies.firredismessenger.api.RedisListener;
import top.catnies.firredismessenger.api.RedisSubject;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedisManager {
    public static final long DEFAULT_CALLBACK_TIMEOUT_MS = 10000; // 默认10秒超时
    public static final String ALL_RECEIVERS = "*"; // 表示消息发送给所有接收方

    @Getter private static RedisManager instance;
    @Getter private final RedisUri connectUri;

    // 通过 Listener 实现类继承的所有监听器
    private final Map<RedisListener, Set<RedisMessageRouter.SubjectHandler>> registeredListeners = new ConcurrentHashMap<>();

    /* 链接信息 */
    @Getter private final String serverId; // 服务器唯一ID
    @Getter private RedisClient redisClient; // Redis 客户端
    @Getter private StatefulRedisConnection<String, String> connection; // 普通连接
    @Getter private StatefulRedisPubSubConnection<String, String> pubSubConnection; // 发布订阅连接
    @Getter private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet(); // 已订阅的频道集合

    /* 关联对象 */
    @Getter private RedisCallback callbackManager;
    @Getter private RedisMessageRouter messageRouter;

    public RedisManager(RedisUri connectUri, String serverId) {
        instance = this;
        this.serverId = serverId;
        this.connectUri = connectUri;
        this.connect();
    }

    // 链接 Redis 数据库
    private void connect() {
        try {
            // 创建 Redis 链接
            redisClient = RedisClient.create(connectUri.redisUri());
            connection = redisClient.connect();
            pubSubConnection = redisClient.connectPubSub();
            // 初始化回调管理器
            callbackManager = new RedisCallback();
            // 创建路由类
            messageRouter = new RedisMessageRouter(callbackManager);
            if (pubSubConnection != null) {
                pubSubConnection.addListener(new RedisPubSubAdapter<>() {
                    @Override
                    public void message(String channel, String message) {
                        messageRouter.handleMessage(channel, message);
                    }
                });
            }
            System.out.println("Redis connection established.");
        } catch (Exception e) {
            System.err.println("Error in Redis connection: " + e.getMessage());
        }
    }

    /**
     * 注册对象中所有带 @RedisListener 的方法
     */
    public void registerListeners(RedisListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RedisSubject.class)) {
                registerMethod(listener, method);
            }
        }
    }

    /**
     * 取消注册给定对象的所有 handler
     */
    public void unregisterListeners(RedisListener listener) {
        Set<RedisMessageRouter.SubjectHandler> handlers = registeredListeners.remove(listener);
        if (handlers == null || handlers.isEmpty()) return;

        RedisManager redisManager = RedisManager.getInstance();
        handlers.forEach(handler -> {
            redisManager.getMessageRouter().unregisterHandler(handler);
        });
    }

    /**
     * 订阅消息频道
     * @param channel 目标频道
     */
    public void subscribeChannel(String channel) {
        if (subscribedChannels.add(channel)) {
            pubSubConnection.async().subscribe(channel);
        }
    }

    /**
     * 取消订阅消息频道
     * @param channel 目标频道
     */
    public void unsubscribeChannel(String channel) {
        if (subscribedChannels.remove(channel)) {
            pubSubConnection.async().unsubscribe(channel);
        }
    }

    /**
     * 是否已经订阅某个频道
     */
    public boolean isSubscribed(String channel) {
        return subscribedChannels.contains(channel);
    }

    /**
     * 发布 RedisPacket 数据包
     * @param channel 目标频道
     * @param packet 数据包
     * @param timeoutMs 超时时间, 如果在超时时间内收到回复则执行数据包内的回调, 否则执行数据包内的超时回调;
     */
    public void publish(@NotNull String channel, @NotNull RedisPacket packet, long timeoutMs) {
        packet.setChannel(channel); // 确保消息包含频道信息
        callbackManager.registerCallbackTask(packet, timeoutMs); // 注册数据包内的回调
        String message = packet.toJson(); // 序列化消息
        pubSubConnection.async().publish(channel, message); // 发布消息
    }

    public void publish(String channel, RedisPacket packet) {
        publish(channel, packet, RedisManager.DEFAULT_CALLBACK_TIMEOUT_MS);
    }


    /**
     * 将类内的带有 @RedisSubject 的方法进行注册
     * @param listener 监听器
     * @param method 方法对象
     */
    private void registerMethod(RedisListener listener, Method method) {
        RedisSubject annotation = method.getAnnotation(RedisSubject.class);
        if (!method.getParameterTypes()[0].equals(RedisPacket.class)) {
            throw new IllegalArgumentException("RedisListener 方法的第一个参数必须是 RedisPacket !");
        }

        RedisManager redisManager = RedisManager.getInstance();

        // 解析方法
        String subject = annotation.subject();
        String channel = annotation.channel();
        int priority = annotation.priority();
        boolean autoSubscribe = annotation.autoSubscribe();

        // lambda方式调用目标方法（处理并发和NPE）
        Consumer<RedisPacket> handler = packet -> {
            try {
                method.setAccessible(true);
                method.invoke(listener, packet);
            } catch (Exception e) {
                System.err.println("Error in Redis message handler: " + e.getMessage());
            }
        };

        // 创建处理器对象, 然后注册
        RedisMessageRouter.SubjectHandler subjectHandler = new RedisMessageRouter.SubjectHandler(channel, subject, handler, priority);
        redisManager.getMessageRouter().registerHandler(subjectHandler);

        // 自动订阅频道
        if (autoSubscribe && !redisManager.isSubscribed(channel)) {
            redisManager.subscribeChannel(channel);
        }

        registeredListeners.computeIfAbsent(listener, k -> new LinkedHashSet<>()).add(subjectHandler);
    }


    /**
     * 关闭数据库链接
     */
    public void shutdown() {
        if (callbackManager != null) callbackManager.shutdown();

        if (pubSubConnection != null) pubSubConnection.close();
        if (connection != null) connection.close();
        if (redisClient != null) redisClient.shutdown();
    }

}
