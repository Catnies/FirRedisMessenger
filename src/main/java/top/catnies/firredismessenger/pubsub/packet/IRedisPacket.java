package top.catnies.firredismessenger.pubsub.packet;


public interface IRedisPacket<T> {

    /**
     * 获取数据包的元数据信息
     * @return 元数据信息
     */
    IRedisPacketMetadata getMetadata();
    void setMetadata(IRedisPacketMetadata metadata);

    /**
     * 获取消息主题
     * @return 消息主题
     */
    String getSubject();
    void setSubject(String subject);

    /**
     * 获取消息内容
     * @return 消息内容
     */
    T getPayload();
    void setPayload(T payload);

}
