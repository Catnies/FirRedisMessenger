package top.catnies.firredismessenger.pubsub;

import org.jetbrains.annotations.NotNull;
import top.catnies.firredismessenger.RedisManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;


public class RedisPubSubRouter {

    // [频道: [主题: [处理器(自带权重)] ]]
    private final Map<String, Map<String, CopyOnWriteArraySet<SubjectHandler>>> subjectHandlers = new ConcurrentHashMap<>();

    // 回调管理器
    private final RedisPubSubCallbackManager callbackManager;
    // 接收消息处理的线程池
    private final ExecutorService dispatchExecutor = Executors.newVirtualThreadPerTaskExecutor();


    public RedisPubSubRouter(RedisPubSubCallbackManager callbackManager) {
        this.callbackManager = callbackManager;
    }

    /**
     * 消息处理器对象
     * @param channel 处理的频道
     * @param subject 处理的主题
     * @param handler 处理器回调逻辑
     * @param priority 权重, 越大的越先处理
     */
    public record SubjectHandler(
            String channel,
            String subject,
            Consumer<RedisPacket> handler,
            int priority
    ) { }

    /**
     * 注册频道消息处理器
     * @param handler 处理器对象
     */
    public synchronized void registerHandler(SubjectHandler handler) {
        Map<String, CopyOnWriteArraySet<SubjectHandler>> subjectMap = subjectHandlers.getOrDefault(handler.channel, new HashMap<>());

        // 添加新处理器时, 根据处理器权重重排序
        CopyOnWriteArraySet<SubjectHandler> handlers = subjectMap.getOrDefault(handler.subject, new CopyOnWriteArraySet<>());
        handlers.add(handler);
        subjectMap.put(
                handler.subject,
                handlers.stream().sorted(Comparator.comparingInt(SubjectHandler::priority).reversed()).collect(CopyOnWriteArraySet::new, CopyOnWriteArraySet::add, CopyOnWriteArraySet::addAll)
        );

        // 放回处理器
        subjectHandlers.put(handler.channel, subjectMap);
    }

    /**
     * 取消注册频道消息处理器
     * @param handler 处理器对象
     */
    public synchronized void unregisterHandler(SubjectHandler handler) {
        Map<String, CopyOnWriteArraySet<SubjectHandler>> subjectMap = subjectHandlers.get(handler.channel);
        if (subjectMap == null) return;
        CopyOnWriteArraySet<SubjectHandler> handlers = subjectMap.get(handler.subject);
        if (handlers == null) return;
        handlers.removeIf(h -> h.handler.equals(handler));
    }

    // 处理消息主入口, 根据消息的频道分发消息到相应的处理器
    public void handleMessage(@NotNull String channel, @NotNull String message) {
        RedisPacket packet = RedisPacket.fromJson(message);
        String currentServerId = RedisManager.getInstance().getServerId();
        String receiver = packet.getReceiver();

        // 忽略非当前服务器的消息
        if (!RedisPubSubManager.ALL_RECEIVERS.equals(receiver) && !currentServerId.equals(receiver)) return;

        // 如果是响应消息, 先处理可能存在的回调
        if (packet.getResponseId() != null) {
            handleResponse(packet);
            return; // 响应消息不需要继续常规处理
        }

        // 如果是普通消息, 只处理发给所有人或者特定发给当前服务器的消息
        dispatchToSubjectHandlers(channel, packet);
    }

    // 获取处理器
    public CopyOnWriteArraySet<SubjectHandler> getSubjectHandlers(String channel, String subject) {
        Map<String, CopyOnWriteArraySet<SubjectHandler>> subjectMap = subjectHandlers.get(channel);
        if (subjectMap == null) {
            subjectMap = new HashMap<>();
            subjectMap.put(subject, new CopyOnWriteArraySet<>());
            subjectHandlers.putIfAbsent(channel, subjectMap);
        }
        return subjectMap.get(subject);
    }

    // 处理响应消息的回调
    private void handleResponse(RedisPacket responsePacket) {
        // 获取回调数据
        UUID originalMessageId = responsePacket.getResponseId();
        RedisPubSubCallbackManager.CallbackEntry entry = callbackManager.getPendingCallbacks().remove(originalMessageId);
        assert entry != null;
        Consumer<String> callback = entry.packet().getCallback();
        // 如果有超时回调
        if (entry.timeOutFuture() != null && !entry.timeOutFuture().isDone()) {
            boolean cancel = entry.timeOutFuture().cancel(false);
            // 如果超时回调取消成功, 并且有原始回调, 就执行原始回调;
            if (cancel && callback != null) {
                dispatchExecutor.execute(() -> {
                    callback.accept(responsePacket.getPayload()); // 调用原始回调
                });
            }
        }
        // 如果没有超时回调
        else if (callback != null) {
            dispatchExecutor.execute(() -> {
                callback.accept(responsePacket.getPayload()); // 调用原始回调
            });
        }
    }

    // 将消息分发到主题处理器
    private void dispatchToSubjectHandlers(@NotNull String channel, @NotNull RedisPacket packet) {
        // 根据频道和主题, 寻找对应的 Handler Set;
        String subject = packet.getSubject();
        Map<String, CopyOnWriteArraySet<SubjectHandler>> subjectHandlerMap = subjectHandlers.get(channel);
        if (subjectHandlerMap == null || subjectHandlerMap.isEmpty()) return;
        CopyOnWriteArraySet<SubjectHandler> handlers = subjectHandlerMap.get(subject);
        if (handlers == null || handlers.isEmpty()) return;

        // 执行 Handler
        handlers.forEach(subjectHandler -> {
            dispatchExecutor.execute(() -> {
                try {
                    subjectHandler.handler.accept(packet);
                } catch (Exception e) {
                    // 简单记录异常，避免处理器异常影响其他处理器
                    System.err.println("Error in Redis message handler: " + e.getMessage());
                }
            });
        });
    }

    /**
     * 关闭消息路由
     */
    public void shutdown() {
        dispatchExecutor.shutdown();
        subjectHandlers.clear();
    }
}
