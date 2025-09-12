package top.catnies.firredismessenger.pubsub.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

public record RedisMessageType(Integer id) {

    // 包类型
    public static final RedisMessageType PUBLISH = new RedisMessageType(1);
    public static final RedisMessageType ACK = new RedisMessageType(2);
    public static final RedisMessageType RESPONSE = new RedisMessageType(3);

    // 映射表
    public static final RedisMessageType[] VALUES = new RedisMessageType[] {PUBLISH, ACK, RESPONSE};
    public static final BiMap<Integer, RedisMessageType> INDEX =
            ImmutableBiMap.<Integer, RedisMessageType>builder()
                    .put(PUBLISH.id(), PUBLISH)
                    .put(ACK.id(), ACK)
                    .put(RESPONSE.id(), RESPONSE)
                    .build();

    /**
     * 根据ID获取数据包类型
     * @param id 数据包类型ID
     * @return 数据包类型
     */
    public static RedisMessageType byId(int id) {
        return INDEX.get(id);
    }

    /**
     * 根据数据包类型获取数据包ID
     * @param type 数据包类型
     * @return 数据包类型ID
     */
    public static int getId(RedisMessageType type) {
        return INDEX.inverse().get(type);
    }

}
