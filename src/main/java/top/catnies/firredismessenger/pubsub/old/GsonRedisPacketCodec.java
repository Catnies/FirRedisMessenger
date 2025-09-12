//package top.catnies.firredismessenger.pubsub.packet;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import io.netty.handler.codec.DecoderException;
//import io.netty.handler.codec.EncoderException;
//import top.catnies.firredismessenger.RedisManager;
//
//import java.nio.charset.StandardCharsets;
//
//// TODO 需要修改
//public class GsonRedisPacketCodec implements IRedisPacketCoder {
//
//    public static final Gson SHARED_GSON = new GsonBuilder().create();
//
//    @Override
//    public byte[] encode(IRedisPacket<?> packet) throws EncoderException {
//        try {
//            String json = SHARED_GSON.toJson(packet);
//            return json.getBytes(StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new EncoderException("Failed to encode packet", e);
//        }
//    }
//
//    @Override
//    public IRedisPacket<?> decode(byte[] data) throws DecoderException {
//        try {
//            String json = new String(data, StandardCharsets.UTF_8);
//            // 先解析 metadata 获取 typeId
//            RedisPacketWrapper wrapper = SHARED_GSON.fromJson(json, RedisPacketWrapper.class);
//            int typeId = wrapper.metadata.typeId;
//            Class<? extends IRedisPacket<?>> clazz = RedisManager.getInstance().getPubSubManager().getPacketRegistry().getPacketClass(typeId);
//            if (clazz == null) {
//                throw new DecoderException("Unknown typeId: " + typeId);
//            }
//            // 第二次反序列化成真正的包类型
//            return SHARED_GSON.fromJson(json, clazz);
//        } catch (Exception e) {
//            throw new DecoderException("Failed to decode packet", e);
//        }
//    }
//
//    /**
//     * 临时中间类，用来解析出 metadata.typeId
//     */
//    private static class RedisPacketWrapper {
//        public Metadata metadata;
//        public static class Metadata {
//            public int typeId;
//        }
//    }
//}
