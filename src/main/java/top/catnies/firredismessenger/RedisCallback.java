package top.catnies.firredismessenger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class RedisCallback {
    @Getter private final Map<UUID, CallbackEntry> pendingCallbacks = new ConcurrentHashMap<>(); // 正在等待回调的任务

    // TODO 优化回调处理的线程池? 不懂线程池捏
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // 执行器

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
     * 关闭回调管理器，清理资源
     */
    public void shutdown() {
        // 取消所有待处理的超时任务
        for (CallbackEntry entry : pendingCallbacks.values()) {
            if (entry.timeOutFuture != null) {
                entry.timeOutFuture.cancel(false);
            }
        }
        pendingCallbacks.clear();

        // 等待最多5秒完成剩余任务
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                scheduler.shutdownNow();
        } catch (InterruptedException e) { scheduler.shutdownNow(); }
    }


    @Data
    @AllArgsConstructor
    public static class CallbackEntry {
        @NotNull private RedisPacket packet;
        @Nullable private ScheduledFuture<?> timeOutFuture;
    }
}
