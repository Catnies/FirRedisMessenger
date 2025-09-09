package top.catnies.firredismessenger.queue.message;

import java.util.HashMap;
import java.util.Map;

public record RedisMessageMetadata(
        String className, // 消息全类名
        String callbackId, // 回调ID
        String origin, // 消息发送者
        long timestamp  // 消息发出时间
) {

    // 转成Map对象
    public static Map<String, String> toMap(RedisMessageMetadata metadata) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("_className", metadata.className);
        hashMap.put("_callbackId", metadata.callbackId);
        hashMap.put("_origin", metadata.origin);
        hashMap.put("_timestamp", String.valueOf(metadata.timestamp));
        return hashMap;
    }

    // 从Map中获取
    public static RedisMessageMetadata fromMap(Map<String, String> map) {
        return new RedisMessageMetadata(
                map.get("_className"),
                map.get("_callbackId"),
                map.get("_origin"),
                Long.parseLong(map.get("_timestamp"))
        );
    }

}