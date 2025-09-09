package top.catnies.firredismessenger.queue.message;

import top.catnies.firredismessenger.queue.RedisStreamManager;

import java.util.Map;

public record RedisEnvelope<T extends RedisMessage>(
    RedisMessageMetadata metadata,
    T payload
) {

    /**
     * 回复消息
     * @param responsePayload 回复内容
     */
    public void response(Map<String, String> responsePayload) {
        RedisStreamManager.getInstance().xaddResponseMessage(this, responsePayload);
    }

}
