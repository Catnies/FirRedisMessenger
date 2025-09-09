package top.catnies.firredismessenger.queue;

import top.catnies.firredismessenger.queue.message.RedisMessage;
import top.catnies.firredismessenger.queue.message.RedisMessageDecoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisMessageDecoderRegistry {

    // 序列化器映射
    private static final Map<String, RedisMessageDecoder<?>> DECODERS = new ConcurrentHashMap<>();

    // 新增序列化器 [全类名, 序列化器]
    public static <T extends RedisMessage> void register(String fullClassName, RedisMessageDecoder<T> decoder) {
        DECODERS.put(fullClassName, decoder);
    }

    // 根据类名获取 Decoder
    @SuppressWarnings("unchecked")
    public static <T extends RedisMessage> RedisMessageDecoder<T> getDecoder(String type) {
        return (RedisMessageDecoder<T>) DECODERS.get(type);
    }

}
