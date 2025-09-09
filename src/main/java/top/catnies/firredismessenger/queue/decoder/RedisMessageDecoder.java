package top.catnies.firredismessenger.queue.decoder;

import top.catnies.firredismessenger.queue.message.RedisMessage;

import java.util.Map;

public interface RedisMessageDecoder<T extends RedisMessage> {

    /**
     * 序列化消息
     * @return 消息
     */
    Map<String, String> serialize(T message);

    /**
     * 消息反序列化方法
     * @param map Stream的内容
     * @return 消息对象
     */
    T deserialize(Map<String, String> map);

}