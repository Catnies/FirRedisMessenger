package top.catnies.firredismessenger.pubsub;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.Nullable;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.RedisPacketCoder;
import top.catnies.firredismessenger.pubsub.packet.RedisPacketRegistration;
import top.catnies.firredismessenger.pubsub.packet.impl.AckRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.impl.StringRedisPacket;

public class RedisPacketRegistry implements IRedisPacketRegistry {

    private final BiMap<Integer, RedisPacketRegistration<?>> map = HashBiMap.create();

    public RedisPacketRegistry() {
        this.register(0, AckRedisPacket.class, AckRedisPacket.CODER); // 回复数据包
        this.register(1, StringRedisPacket.class, StringRedisPacket.CODER); // 基于String的RedisPacket.
    }

    // 注册
    @Override
    public void register(int typeId, Class<? extends IRedisPacket> clazz, RedisPacketCoder.IRedisPacketCoder<? extends IRedisPacket> coder) {
        if (this.map.containsKey(typeId)) {
            throw new IllegalArgumentException("Packet typeId " + typeId + " is already registered");
        }
        this.map.put(typeId, new RedisPacketRegistration<>(typeId, clazz, coder));
    }

    // 查询
    @Override
    @Nullable
    public RedisPacketRegistration<? extends IRedisPacket> getRegistration(int typeId) {
        return this.map.get(typeId);
    }

    // 查询
    @Override
    @Nullable
    public Class<? extends IRedisPacket> getPacketClass(int typeId) {
        RedisPacketRegistration<? extends IRedisPacket> registration = this.map.get(typeId);
        if (registration == null) return null;
        return registration.clazz();
    }

    // 查询
    @Override
    @Nullable
    public RedisPacketCoder.IRedisPacketCoder<? extends IRedisPacket> getPacketCoder(int typeId) {
        RedisPacketRegistration<? extends IRedisPacket> registration = this.map.get(typeId);
        if (registration == null) return null;
        return registration.coder();
    }

    // 查询
    @Override
    public int getPacketId(Class<?> packetClass) {
        for (RedisPacketRegistration<?> value : this.map.values()) {
            if (value.clazz() != packetClass) continue;
            return value.id();
        }

        throw new NullPointerException("Packet class " + packetClass + " is not registered");
    }

}
