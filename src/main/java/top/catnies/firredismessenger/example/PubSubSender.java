package top.catnies.firredismessenger.example;

import lombok.SneakyThrows;
import top.catnies.firredismessenger.RedisManager;
import top.catnies.firredismessenger.RedisUri;
import top.catnies.firredismessenger.pubsub.packet.impl.StringRedisPacket;

import static top.catnies.firredismessenger.RedisUri.*;

public class PubSubSender {

    @SneakyThrows
    public static void main(String[] args) {
        RedisUri redisUri = builder().ip("127.0.0.1").host(6379).build();
        RedisManager redisManager = new RedisManager(redisUri, "Spawn");

        redisManager.getPubSubManager().subscribeChannel("qwq");
        redisManager.getPubSubManager().getMessageRouter().registerHandler("qwq", StringRedisPacket.class, "aaa", stringRedisPacket -> {
            System.out.println(stringRedisPacket.getPayload());
        }, 1);

        while (true) {
            Thread.sleep(1000);

            StringRedisPacket stringRedisPacket = new StringRedisPacket("666", "777");
            redisManager.getPubSubManager().publishPacket("qwq", new String[]{"Lobby"}, stringRedisPacket, () -> {
                System.out.println("我收到了ACK!");
            }, null, 0, null);
        }
    }

}
