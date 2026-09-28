package com.ralonsoc.backend.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withPassword(redisPassword.toCharArray())
                .build();
        return RedisClient.create(redisUri);
    }

    // Keys are byte[] (Bucket4j's Lettuce integration works at the raw-bytes level);
    // RateLimitFilter encodes its string keys before calling the proxy manager.
    @Bean
    public ProxyManager<byte[]> rateLimitProxyManager(RedisClient redisClient) {
        return Bucket4jLettuce.casBasedBuilder(redisClient).build();
    }
}
