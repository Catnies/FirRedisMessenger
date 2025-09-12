package top.catnies.firredismessenger.pubsub;

import org.jetbrains.annotations.NotNull;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class RedisPubSubRouter {

    // [频道: [消息Class -> [主题: [处理器(自带权重)] ]]
    private final Map<String, Map<Class<? extends IRedisPacket>, Map<String, CopyOnWriteArrayList<HandlerWrapper<?>>>>> handlers = new ConcurrentHashMap<>();
    // 接收消息处理的线程池
    private final ExecutorService dispatchExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 处理器包装类
     * @param channel 处理的频道
     * @param packetType 处理的包类型
     * @param subject 处理的主题
     * @param handler 处理器回调逻辑
     * @param priority 权重, 越大的越先处理
     */
    private record HandlerWrapper<T extends IRedisPacket>(
            String channel,
            Class<T> packetType,
            String subject,
            Consumer<T> handler,
            int priority
    ) { }

    /**
     * 注册消息处理器
     * @param channel 处理的频道
     * @param packetType 处理的包类型
     * @param subject 处理的主题
     * @param handler 处理器回调逻辑
     * @param priority 权重, 越大的越先处理
     */
    public <T extends IRedisPacket> void registerHandler(
            String channel,
            Class<T> packetType,
            String subject,
            Consumer<T> handler,
            int priority
    ) {
        handlers.computeIfAbsent(channel, c -> new ConcurrentHashMap<>())
                .computeIfAbsent(packetType, t -> new ConcurrentHashMap<>())
                .computeIfAbsent(subject, s -> new CopyOnWriteArrayList<>())
                .add(new HandlerWrapper<>(channel, packetType, subject, handler, priority));
        // 排序: 优先级高的在前
        handlers.get(channel).get(packetType).get(subject)
                .sort((a, b) -> Integer.compare(b.priority, a.priority));
    }

    /**
     * 注销处理器
     * @param channel 处理的频道
     * @param packetType 处理的包类型
     * @param subject 处理的主题
     * @param handler 处理器回调逻辑
     */
    public <T extends IRedisPacket> void unregisterHandler(
            String channel,
            Class<T> packetType,
            String subject,
            Consumer<T> handler
    ) {
        Map<Class<? extends IRedisPacket>, Map<String, CopyOnWriteArrayList<HandlerWrapper<?>>>> typeMap = handlers.get(channel);
        if (typeMap == null) return;
        Map<String, CopyOnWriteArrayList<HandlerWrapper<?>>> subjectMap = typeMap.get(packetType);
        if (subjectMap == null) return;
        CopyOnWriteArrayList<HandlerWrapper<?>> list = subjectMap.get(subject);
        if (list == null) return;
        list.removeIf(h -> h.handler.equals(handler));
    }

    /**
     * 消息入口：这里接收一个已经解码好的 Packet
     */
    public void handlePublishPacket(@NotNull String channel, @NotNull IRedisPacket packet) {
        Map<Class<? extends IRedisPacket>, Map<String, CopyOnWriteArrayList<HandlerWrapper<?>>>> typeMap = handlers.get(channel);
        if (typeMap == null) return;
        Map<String, CopyOnWriteArrayList<HandlerWrapper<?>>> subjectMap = typeMap.get(packet.getClass());
        if (subjectMap == null) return;
        String subject = packet.getSubject();
        CopyOnWriteArrayList<HandlerWrapper<?>> list = subjectMap.get(subject);
        if (list == null || list.isEmpty()) return;
        // 按 priority 顺序执行
        for (HandlerWrapper<?> wrapper : list) {
            dispatchExecutor.execute(() -> {
                try {
                    // 类型安全强转
                    @SuppressWarnings("unchecked")
                    Consumer<IRedisPacket> handler = (Consumer<IRedisPacket>) wrapper.handler;
                    handler.accept(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 关闭消息路由
     */
    public void shutdown() {
        dispatchExecutor.shutdown();
        handlers.clear();
    }
}
