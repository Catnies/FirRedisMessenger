package top.catnies.firredismessenger.pubsub.packet.impl;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacketCoder;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacketMetadata;

public class AckRedisPacket implements IRedisPacket<Void> {

    public static final IRedisPacketCoder CODER = new PacketCoder();

    private IRedisPacketMetadata metadata;

    @Override
    public IRedisPacketMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(IRedisPacketMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getSubject() {
        return null;
    }

    @Override
    public void setSubject(String subject) {
        throw new UnsupportedOperationException("AckRedisPacket can not set subject");
    }

    @Override
    public Void getPayload() {
        return null;
    }

    @Override
    public void setPayload(Void payload) {
        throw new UnsupportedOperationException("AckRedisPacket can not set payload");
    }

    /**
     * 序列化器
     */
    public static class PacketCoder implements IRedisPacketCoder {

        @Override
        public byte[] encode(IRedisPacket<?> packet) throws EncoderException {
            return new byte[0];
        }

        @Override
        public IRedisPacket<?> decode(byte[] data) throws DecoderException {
            return new AckRedisPacket();
        }
    }
}
