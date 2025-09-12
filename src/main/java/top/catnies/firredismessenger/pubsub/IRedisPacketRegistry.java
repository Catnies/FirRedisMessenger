package top.catnies.firredismessenger.pubsub;

import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.RedisPacketCoder;
import top.catnies.firredismessenger.pubsub.packet.RedisPacketRegistration;

public interface IRedisPacketRegistry {

    /**
     * 将数据包类型和ID进行映射
     * @param typeId 数据包ID
     * @param packetClass 数据包类
     */
    void register(int typeId, Class<? extends IRedisPacket> packetClass, RedisPacketCoder.IRedisPacketCoder<? extends IRedisPacket> coder);

    /**
     * 根据ID查询数据包注册实例
     * @param typeId 数据包类型ID
     * @return 数据包注册实例
     */
    RedisPacketRegistration<? extends IRedisPacket> getRegistration(int typeId);

    /**
     * 根据ID查询数据包类型
     * @param typeId 数据包类型ID
     * @return 数据包类
     */
    Class<? extends IRedisPacket> getPacketClass(int typeId);

    /**
     * 获取数据包的序列化器
     * @param typeId 数据包类型ID
     * @return 序列化器
     */
    RedisPacketCoder.IRedisPacketCoder<?> getPacketCoder(int typeId);

    /**
     * 根据数据包类查询ID
     * @param packetClass 数据包类
     * @return 数据包类型ID
     */
    int getPacketId(Class<?> packetClass);
}
