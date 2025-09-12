package top.catnies.firredismessenger.pubsub.packet;

import top.catnies.firredismessenger.util.ByteUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 基础元数据实现类;
 * 元数据将会在消息发送时自动生成;
 *
 * @param packetId 数据包ID, 每条数据包唯一;
 * @param messageTypeId 消息类型, 例如是ACK, PUBLISH, 或者是RESPONSE;
 * @param messageId 消息ID, 是UUID的字符串化;
 * @param sender 数据包的发送人
 * @param receivers 数据包的接收者们
 * @param requiresAck 是否需要收到方自动ACK
 * @param requiresResponse 数据包的发送人
 * @param publishTime 发布数据包的时间
 */
public record RedisPacketMetadata(
        int packetId,
        int messageTypeId,
        String messageId,
        String sender,
        String[] receivers,
        boolean requiresAck,
        boolean requiresResponse,
        String callbackId,
        long publishTime
) implements IRedisPacketMetadata {

    public byte[] toBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            dos.writeInt(packetId);
            dos.writeInt(messageTypeId);
            ByteUtils.writeString(dos, messageId);
            ByteUtils.writeString(dos, sender);
            ByteUtils.writeStringArray(dos, receivers);
            dos.writeBoolean(requiresAck);
            dos.writeBoolean(requiresResponse);
            ByteUtils.writeString(dos, callbackId);
            dos.writeLong(publishTime);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static RedisPacketMetadata fromBytes(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(bis)) {
            int packetId = dis.readInt();
            int messageTypeId = dis.readInt();
            String messageId = ByteUtils.readString(dis);
            String sender = ByteUtils.readString(dis);
            String[] receivers = ByteUtils.readStringArray(dis);
            boolean requiresAck = dis.readBoolean();
            boolean requiresResponse = dis.readBoolean();
            String callbackId = ByteUtils.readString(dis);
            long publishTime = dis.readLong();
            return new RedisPacketMetadata(packetId, messageTypeId, messageId, sender, receivers, requiresAck, requiresResponse, callbackId, publishTime);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
