package top.catnies.firredismessenger.pubsub.packet.impl;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.Getter;
import lombok.Setter;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacketCoder;
import top.catnies.firredismessenger.pubsub.packet.IRedisPacketMetadata;
import top.catnies.firredismessenger.util.ByteUtils;

import java.io.*;

@Getter
@Setter
public class StringRedisPacket implements IRedisPacket<String> {

    public static final IRedisPacketCoder CODER = new PacketCoder();

    private IRedisPacketMetadata metadata;
    private String subject;
    private String payload;

    public StringRedisPacket(
        String subject,
        String payload
    ) {
        this.metadata = null;
        this.subject = subject;
        this.payload = payload;
    }

    public static class PacketCoder implements IRedisPacketCoder {

        @Override
        public byte[] encode(IRedisPacket<?> packet) throws EncoderException {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(bos)) {
                StringRedisPacket p = (StringRedisPacket) packet;
                // 写 subject
                ByteUtils.writeString(dos, p.subject);
                // 写 payload
                ByteUtils.writeString(dos, p.payload);
                return bos.toByteArray();
            } catch (IOException e) {
                throw new EncoderException("Failed to encode StringRedisPacket", e);
            }
        }

        @Override
        public IRedisPacket<?> decode(byte[] data) throws DecoderException {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bis)) {
                // 读 subject
                String subject = ByteUtils.readString(dis);
                // 读 payload
                String payload = ByteUtils.readString(dis);
                return new StringRedisPacket(subject, payload);
            } catch (IOException e) {
                throw new DecoderException("Failed to decode StringRedisPacket", e);
            }
        }

    }
}
