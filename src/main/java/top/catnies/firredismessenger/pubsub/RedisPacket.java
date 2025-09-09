package top.catnies.firredismessenger.pubsub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;


@Data
public class RedisPacket {
    public static final Gson SHARED_GSON = new GsonBuilder().create();

    /** Message Data **/
    @NotNull private String sender; // 消息发送者
    @NotNull private String receiver; // 消息接收者
    @NotNull private String subject; // 消息主题
    @NotNull private String payload; // 消息内容
    @Nullable private transient Consumer<String> callback; // 接收到目标服务器回复消息后的回调, 泛型携带的是目标服务器回复的消息;
    @Nullable private transient Consumer<RedisPacket> onTimeout; // 超时未回复时触发的回调, 泛型携带的是发送的包;

    /** Message MetaData **/
    private final UUID messageId = UUID.randomUUID();
    private final long createTimestamp = System.currentTimeMillis();
    private UUID responseId; // 当数据包是回复数据包时, 需要携带回复数据包目标的原始ID;

    /** Message Additional Data **/
    private Long publishTimestamp;  // 广播时间
    private String channel;         // 广播频道

    /**
     * 构造 Reids 广播数据包
     * @param sender 发送者, 填写发送数据包的服务器唯一标识符
     * @param receiver 接收者, 填写接收数据包的服务器的唯一标识符
     * @param subject 主题, 为消息进行分类
     * @param payload 内容, 消息内容
     */
    public RedisPacket(@NotNull String sender, @NotNull String receiver, @NotNull String subject, @NotNull String payload) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.payload = payload;
    }

    /**
     * 创建 Redis 广播数据包
     * @param sender 发送者, 填写发送数据包的服务器唯一标识符
     * @param receiver 接收者, 填写接收数据包的服务器的唯一标识符
     * @param subject 主题, 为消息进行分类
     * @param payload 内容, 消息内容
     * @return 数据包
     */
    public static RedisPacket of(@NotNull String sender, @NotNull String receiver, @NotNull String subject, @NotNull String payload) {
        return new RedisPacket(sender, receiver, subject, payload);
    }

    /**
     * 创建一个可广播全部监听者的 Redis 数据包
     * @param sender 发送者, 填写发送数据包的服务器唯一标识符
     * @param subject 主题, 为消息进行分类
     * @param payload 内容, 消息内容
     * @return 数据包
     */
    public static RedisPacket ofBoardCast(@NotNull String sender, @NotNull String subject, @NotNull String payload) {
        return new RedisPacket(sender, RedisPubSubManager.ALL_RECEIVERS, subject, payload);
    }

    /**
     * 创建一个响应回复的 Redis 数据包
     * @param original 接收到的准备回复的数据包
     * @param responsePayload 回复的消息内容
     * @return 数据包
     */
    public static RedisPacket ofResponse(@NotNull RedisPacket original, @NotNull String responsePayload) {
        RedisPacket responsePacket = new RedisPacket(original.receiver, original.sender, original.subject, responsePayload);
        responsePacket.responseId = original.messageId;
        return responsePacket;
    }

    /**
     * 设置数据包的回调函数, 流程为: 发送数据包 -> 接收到响应数据包 -> 执行回调函数;
     * @param callback 回调函数
     * @return 数据包
     */
    public RedisPacket withCallback(@Nullable Consumer<String> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 设置数据包的超时回调函数, 流程为: 发送数据包 -> 达到超时时间未收到响应包 -> 执行超时函数;
     * @param onTimeout 超时函数
     * @return 数据包
     */
    public RedisPacket withOnTimeout(@Nullable Consumer<RedisPacket> onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    /** 序列化和反序列化方法 **/
    public String toJson() {
        return SHARED_GSON.toJson(this);
    }
    public static RedisPacket fromJson(String json) {
        return SHARED_GSON.fromJson(json, RedisPacket.class);
    }

    /** 拓展便携方法 **/
    public void publish(@NotNull String channel) {
        RedisPubSubManager.getInstance().publish(channel, this);
    }
}
