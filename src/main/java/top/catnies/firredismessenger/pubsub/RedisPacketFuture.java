package top.catnies.firredismessenger.pubsub;

import top.catnies.firredismessenger.pubsub.packet.IRedisPacket;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RedisPacketFuture {

    private final CompletableFuture<Void> ackFuture = new CompletableFuture<>();
    private final CompletableFuture<IRedisPacket<?>> responseFuture = new CompletableFuture<>();

    public void completeAck() {
        ackFuture.complete(null);
    }

    public <T extends IRedisPacket<?>> void completeResponse(T response) {
        responseFuture.complete(response);
    }

    public void onAck(Runnable action) {
        ackFuture.thenRun(action);
    }

    public void onResponse(Consumer<IRedisPacket<?>> action) {
        responseFuture.thenAccept(action);
    }

    public boolean isAckDone() {
        return ackFuture.isDone();
    }

    public boolean isResponseDone() {
        return responseFuture.isDone();
    }

    public void completeException(Throwable t) {
        ackFuture.completeExceptionally(t);
        responseFuture.completeExceptionally(t);
    }

}
