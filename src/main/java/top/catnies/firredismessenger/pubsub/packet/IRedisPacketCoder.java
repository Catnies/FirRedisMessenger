package top.catnies.firredismessenger.pubsub.packet;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

public interface IRedisPacketCoder {

    /**
     * 序列化
     * @param packet 数据包
     * @return 序列化完成的对象
     * @throws EncoderException 错误
     */
    byte[] encode(IRedisPacket<?> packet) throws EncoderException;

    /**
     * 反序列化
     * @param data 输入数据
     * @return 反序列化完成的对象
     * @throws DecoderException 错误
     */
    IRedisPacket<?> decode(byte[] data) throws DecoderException;

}
