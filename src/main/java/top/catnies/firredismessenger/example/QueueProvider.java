package top.catnies.firredismessenger.example;

import top.catnies.firredismessenger.RedisManager;
import top.catnies.firredismessenger.RedisUri;
import top.catnies.firredismessenger.queue.RedisMessageFuture;
import top.catnies.firredismessenger.queue.RedisStreamManager;
import top.catnies.firredismessenger.queue.message.RedisStringMessage;

import java.util.concurrent.TimeUnit;

public class QueueProvider {
    public static void main(String[] args) {
        RedisUri redisUri = RedisUri.builder().ip("127.0.0.1").host(6379).build();
        RedisManager redisManager = new RedisManager(redisUri, "Lobby");
        RedisStreamManager streamManager = redisManager.getStreamManager();

        // 发送消息
        RedisMessageFuture messageFuture = streamManager.xaddMessage("_message-Spawn", new RedisStringMessage("Hello World!"));
        messageFuture.onResponse(responseMsg -> {
            System.out.println("接收到了消息回复, 回复的内容是: " + responseMsg + " !");
        });
        messageFuture.onTimeOut(5, TimeUnit.SECONDS, true, msg -> {
            System.out.println("怎么回事喵, 怎么没有回复喵? 对方可能出现了问题喵!");
        });

        while (true) { }
    }
}
