package top.catnies.firredismessenger.example;

import lombok.SneakyThrows;
import top.catnies.firredismessenger.RedisManager;
import top.catnies.firredismessenger.RedisUri;
import top.catnies.firredismessenger.pubsub.RedisListener;
import top.catnies.firredismessenger.pubsub.RedisPacket;
import top.catnies.firredismessenger.pubsub.RedisSubject;

public class PubSubReceiver {

    @SneakyThrows
    public static void main(String[] args) {
        RedisUri redisUri = RedisUri.builder().ip("127.0.0.1").host(6379).build();
        RedisManager redisManager = new RedisManager(redisUri, "Lobby");
        redisManager.getPubSubManager().registerListeners(new MyRedisListener());

        Thread.sleep(1000);

        while (true) { }
    }

    static class MyRedisListener implements RedisListener {

        @RedisSubject(channel = "kafe", subject = "egg", priority = 1, autoSubscribe = true)
        @SneakyThrows
        public void onEgg(RedisPacket packet) {
            String payload = packet.getPayload();
            System.out.println(" Lobby 收到了消息: " + payload);
            Thread.sleep(1000);

            RedisPacket response = RedisPacket.ofResponse(packet, "[回复消息] Hi, 你发的源消息是: " + payload);
            response.publish("kafe");
        }
    }

}
