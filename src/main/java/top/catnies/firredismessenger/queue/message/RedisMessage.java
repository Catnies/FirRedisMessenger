package top.catnies.firredismessenger.queue.message;

public interface RedisMessage {

    /**
     * @return 消息生存时间(毫秒)，<=0 表示无限制
     */
    default long ttlMillis() {
        return 1000 * 10;
    }

}
