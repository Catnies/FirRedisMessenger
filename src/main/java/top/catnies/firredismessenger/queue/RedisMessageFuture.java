package top.catnies.firredismessenger.queue;

import top.catnies.firredismessenger.queue.message.RedisMessage;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class RedisMessageFuture {
    private Consumer<Map<String, String>> responseCallback; // 回复消息内容回调
    private BiConsumer<RedisMessage, Boolean> timeoutCallback; // 超时回调
    private long timeoutAt; // 超时时间点
    private boolean cancelOnTimeout; // 是否在超时时取消任务

    /**
     * 当发出去的消息收到回复内容时, 执行回调
     * @param callback 回调逻辑
     * @return this
     */
    public RedisMessageFuture onResponse(Consumer<Map<String, String>> callback) {
        this.responseCallback = callback;
        return this;
    }

    /**
     * 当发出去的消息一直没有收到回复时, 执行回调
     * @param duration 超时时间
     * @param unit 时间单位
     * @param cancelMessage 是否取消队列里的消息
     * @param callback 回调逻辑
     * @return this
     */
    public RedisMessageFuture onTimeOut(long duration, TimeUnit unit, boolean cancelMessage, Consumer<RedisMessage> callback) {
        this.timeoutAt = System.currentTimeMillis() + unit.toMillis(duration);
        this.cancelOnTimeout = cancelMessage;
        this.timeoutCallback = (msg, canceled) -> callback.accept(msg);
        return this;
    }

    void triggerResponse(Map<String, String> response) {
        if (responseCallback != null)
            responseCallback.accept(response);
    }

    void triggerTimeout(RedisMessage originalMessage) {
        if (timeoutCallback != null)
            timeoutCallback.accept(originalMessage, cancelOnTimeout);
    }

}
