package top.catnies.firredismessenger.queue.message;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class RedisStringMessage implements RedisMessage {

    @Getter @Setter private String payload;

    public RedisStringMessage(String payload) {
        this.payload = payload;
    }

    // 序列化器
    public final static class RedisStringMessageDecoder implements RedisMessageDecoder<RedisStringMessage> {

        @Override
        public Map<String, String> serialize(RedisStringMessage message) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("payload", message.payload);
            return hashMap;
        }

        @Override
        public RedisStringMessage deserialize(Map<String, String> map) {
            return new RedisStringMessage(map.get("payload"));
        }

    }
}
