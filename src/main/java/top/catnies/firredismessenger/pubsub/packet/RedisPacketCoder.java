package top.catnies.firredismessenger.pubsub.packet;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.util.function.Function;

public class RedisPacketCoder {
    public static <P extends IRedisPacket> IRedisPacketCoder<P> of(
            Function<P, byte[]> writer,
            Function<byte[], P> reader
    ) {
        return new IRedisPacketCoder<>() {
            @Override
            public byte[] encode(P packet) throws EncoderException {
                return writer.apply(packet);
            }

            @Override
            public P decode(byte[] data) throws DecoderException {
                return reader.apply(data);
            }
        };
    }

    public interface IRedisPacketCoder<P extends IRedisPacket> {
        /**
         * 序列化
         * @param packet 数据包
         * @return 序列化完成的对象
         * @throws EncoderException 错误
         */
        byte[] encode(P packet) throws EncoderException;

        /**
         * 反序列化
         * @param data 输入数据
         * @return 反序列化完成的对象
         * @throws DecoderException 错误
         */
        P decode(byte[] data) throws DecoderException;
    }
}
