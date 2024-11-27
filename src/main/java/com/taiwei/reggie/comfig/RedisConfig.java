package com.taiwei.reggie.comfig;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        //defsult serializer is：JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key serializing
        //redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // value serializing

        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
