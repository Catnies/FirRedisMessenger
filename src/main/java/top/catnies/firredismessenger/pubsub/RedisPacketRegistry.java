package top.catnies.firredismessenger.pubsub;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.Nullable;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacketCoder;
import top.catnies.firredismessenger.pubsub.packet.impl.AckRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.impl.StringRedisPacket;

public class RedisPacketRegistry implements IRedisPacketRegistry {

    // 维护的数据包ID -> [数据包类, 序列化器] 注册表
    private final BiMap<Integer, Class<? extends IRedisPacket<?>>> classBiMap = HashBiMap.create();
    private final BiMap<Integer, IRedisPacketCoder> coderBiMap = HashBiMap.create();

    public RedisPacketRegistry() {
        register(0, AckRedisPacket.class, AckRedisPacket.CODER); // 回复数据包
        register(1, StringRedisPacket.class, StringRedisPacket.CODER); // 基于String的RedisPacket.
    }

    // 注册
    @Override
    public void register(int typeId, Class<? extends IRedisPacket<?>> clazz, IRedisPacketCoder coder) {
        if (classBiMap.containsKey(typeId)) {
            throw new IllegalArgumentException("Packet typeId " + typeId + " is already registered");
        }
        classBiMap.put(typeId, clazz);
        coderBiMap.put(typeId, coder);
    }

    // 查询
    @Override
    @Nullable
    public Class<? extends IRedisPacket<?>> getPacketClass(int typeId) {
        return classBiMap.get(typeId);
    }

    // 查询
    @Override
    @Nullable
    public IRedisPacketCoder getPacketCoder(int typeId) {
        return coderBiMap.get(typeId);
    }

    // 查询
    @Override
    public int getPacketId(Class<?> packetClass) {
        return classBiMap.inverse().get(packetClass);
    }

}
