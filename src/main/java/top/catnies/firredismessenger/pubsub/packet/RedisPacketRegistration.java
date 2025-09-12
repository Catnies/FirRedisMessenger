package top.catnies.firredismessenger.pubsub.packet;

public record RedisPacketRegistration<P extends IRedisPacket>(
        int id,
        Class<? extends P> clazz,
        RedisPacketCoder.IRedisPacketCoder<? extends P> coder
) {}