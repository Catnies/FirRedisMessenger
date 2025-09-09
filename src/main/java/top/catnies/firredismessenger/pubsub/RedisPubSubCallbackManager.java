package top.catnies.firredismessenger.pubsub;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class RedisPubSubCallbackManager {
    @Getter private final Map<UUID, CallbackEntry> pendingCallbacks = new ConcurrentHashMap<>(); // 正在等待回调的任务
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // 回调执行器

    /**
     * 封装数据包和超时回调计时器的键
     * @param packet 发送出去的数据包
     * @param timeOutFuture 超时回调计时器
     */
    public record CallbackEntry(
            @NotNull RedisPacket packet,
            ScheduledFuture<?> timeOutFuture
    ) { }

    /**
     * 注册Redis数据包的回调任务
     * @param packet 数据包
     * @param timeoutMs 触发超时回调的时间
     */
    public void registerCallbackTask(RedisPacket packet, long timeoutMs) {
        // 开启超时回调任务
        ScheduledFuture<?> timeOutFuture = null;
        if (packet.getOnTimeout() != null) {
            timeOutFuture = scheduler.schedule(() -> {
                // 从待处理集合中移除
                CallbackEntry removed = pendingCallbacks.remove(packet.getMessageId());
                if (removed != null) {
                    packet.getOnTimeout().accept(packet); // 执行超时处理
                }
            }, timeoutMs, TimeUnit.MILLISECONDS);
        }
        // 将回调任务注册到执行任务中.
        pendingCallbacks.put(packet.getMessageId(), new CallbackEntry(packet, timeOutFuture));
    }

    /**
     * 关闭回调管理器，清理资源.
     */
    public void shutdown() {
        // 取消所有待处理的超时任务
        for (CallbackEntry entry : pendingCallbacks.values()) {
            if (entry.timeOutFuture != null) {
                entry.timeOutFuture.cancel(false);
            }
        }

        pendingCallbacks.clear();
        scheduler.shutdown();

        // 等待最多5秒完成剩余任务
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                scheduler.shutdownNow();
        } catch (InterruptedException e) { scheduler.shutdownNow(); }
    }
}
