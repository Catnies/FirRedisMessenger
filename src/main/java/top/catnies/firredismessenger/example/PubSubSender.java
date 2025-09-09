package top.catnies.firredismessenger.example;

import lombok.SneakyThrows;
import top.catnies.firredismessenger.RedisManager;
import top.catnies.firredismessenger.RedisUri;
import top.catnies.firredismessenger.pubsub.RedisPacket;

import static top.catnies.firredismessenger.RedisUri.*;

public class PubSubSender {

    @SneakyThrows
    public static void main(String[] args) {
        RedisUri redisUri = builder().ip("127.0.0.1").host(6379).build();
        RedisManager redisManager = new RedisManager(redisUri, "Spawn");
        redisManager.getPubSubManager().subscribeChannel("kafe");

        Thread.sleep(1000);

        RedisPacket redisPacket = RedisPacket.of(
                redisManager.getServerId(),
                "Lobby",
                "egg",
                "Hello, World!"
        );
        redisPacket.withCallback(payload -> {
            System.out.println(" Spawn 收到了回复消息: " + payload);
        });
        redisPacket.publish("kafe");

        while (true) { }
    }

}
