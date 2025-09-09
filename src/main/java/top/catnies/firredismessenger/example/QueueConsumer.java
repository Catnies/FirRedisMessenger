package top.catnies.firredismessenger.example;

import com.google.common.eventbus.Subscribe;
import top.catnies.firredismessenger.RedisManager;
import top.catnies.firredismessenger.RedisUri;
import top.catnies.firredismessenger.queue.RedisStreamManager;
import top.catnies.firredismessenger.queue.message.RedisEnvelope;
import top.catnies.firredismessenger.queue.message.RedisStringMessage;

import java.util.Map;

public class QueueConsumer {
    public static void main(String[] args) {
        RedisUri redisUri = RedisUri.builder().ip("127.0.0.1").host(6379).build();
        RedisManager redisManager = new RedisManager(redisUri, "Spawn");
        RedisStreamManager streamManager = redisManager.getStreamManager();

        // 接收处理消息
        streamManager.getEventBus().register(new Object() {
            @Subscribe
            public void onStringMessage(RedisEnvelope<RedisStringMessage> msg) {
                System.out.println("收到消息啦: " + msg.payload().getPayload());
                streamManager.xaddResponseMessage(msg, Map.of("payload", "嘎嘎嘎"));
            }
        });

        while (true) { }
    }
}
