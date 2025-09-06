package top.catnies.firredismessenger;

public record RedisUri(
        String redisUri // Redis 链接URL
) {

    /**
     * 获取 URI 的构造器
     *
     * @return 构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Uri 建造者
     */
    public static class Builder {

        private String userName;
        private String password;
        private String ip = "localhost";
        private int host = 6379;
        private int database = 0;

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder host(int host) {
            this.host = host;
            return this;
        }

        public Builder database(int database) {
            this.database = database;
            return this;
        }

        public RedisUri build() {
            if (userName == null || password == null) {
                return new RedisUri("redis://" + ip + ":" + host + "/" + database);
            } else {
                return new RedisUri("redis://" + password + "@" + ip + ":" + host + "/" + database);
            }
        }

    }

}
